package com.me.unicron.iot.gateway.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.me.epower.component.TResponse;
import com.me.epower.direct.entity.CmdProcessRecord;
import com.me.epower.direct.entity.chargecloud.ChargeStatusParam;
import com.me.epower.direct.entity.chargecloud.LockRequestParam;
import com.me.epower.direct.entity.chargecloud.QueryStartChargeParam;
import com.me.epower.direct.entity.chargecloud.QueryStopChargeParam;
import com.me.epower.direct.entity.constant.RedisConstant;
import com.me.epower.direct.entity.downward.ChargeControlQuery_5;
import com.me.epower.direct.entity.downward.ChargeStatus_115;
import com.me.epower.direct.entity.downward.ChargeStopQuery_11;
import com.me.epower.direct.entity.downward.HistoryOrderQuery_403;
import com.me.epower.direct.entity.downward.LockRequest_23;
import com.me.epower.direct.entity.downward.RecentOrderQuery_205;
import com.me.epower.direct.entity.downward.StartChargeQuery_7;
import com.me.epower.direct.enums.StationClusterCmd;
import com.me.epower.direct.repositories.CmdProcessRecordRepository;
import com.me.unicron.EncodeUtil;
import com.me.unicron.HMacMD5;
import com.me.unicron.connector.ConnectorUtils;
import com.me.unicron.iot.bootstrap.ChannelService;
import com.me.unicron.iot.bootstrap.channel.StationManagementService;
import com.me.unicron.iot.gateway.EnvConst;
import com.me.unicron.iot.gateway.service.OperatorCharge;
import com.me.unicron.iot.gateway.service.util.PubChannelUtil;
import com.me.unicron.iot.gateway.worker.impl.CommandWorker;
import com.me.unicron.iot.ip.IpUtils;
import com.me.unicron.iot.kafka.KafkaProducer;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.mq.service.DynamicMQCategoryService;
import com.me.unicron.string.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OperatorChargeImpl implements OperatorCharge {

	@Autowired
	KafkaProducer kafkaProducer;
	@Autowired
	CommandWorker commandWorker;
	@Autowired
	CmdProcessRecordRepository cmdProcessRecordRepository;

	@Autowired
	private StationManagementService stationManagementService;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	/**
	 * 7 发起启动充电
	 */
	@Override
	public TResponse<String> queryStartCharge(QueryStartChargeParam queryStartChargeParam) {
		String key = RedisConstant.UNICRON_START_CHARGE_CHECK + queryStartChargeParam.getUser_id();
		String checkValue = HMacMD5.getHmacMd5Str(key, queryStartChargeParam.getOperatorId()
				+ queryStartChargeParam.getUser_id() + queryStartChargeParam.getTimestamp());
		String destValue = stringRedisTemplate.opsForValue().get(key);
		log.info("queryStartCharge Auth check,key={} ,checkValue= {},destValue= {}", key, checkValue, destValue);
		if (destValue == null || !destValue.equals(checkValue)) {
			log.info("queryStartCharge Auth check FAIL!");
		} else {
			log.info("queryStartCharge Auth check SUCCEED!");
		}

		String equipmentId = queryStartChargeParam.getEquipmentId();

		StartChargeQuery_7 startChargeQuery_7 = new StartChargeQuery_7();
		startChargeQuery_7.setPortId(queryStartChargeParam.getPortId());
		startChargeQuery_7.setCharge_policy(queryStartChargeParam.getCharge_policy());
		startChargeQuery_7.setCharge_policy_para(queryStartChargeParam.getCharge_policy_para());
		startChargeQuery_7.setCharge_valid_type(queryStartChargeParam.getCharge_valid_type());
		startChargeQuery_7.setBook_start_time(queryStartChargeParam.getBook_start_time());
		startChargeQuery_7.setBook_timeout_time(queryStartChargeParam.getBook_timeout_time());
		startChargeQuery_7.setUser_id(queryStartChargeParam.getUser_id());
		startChargeQuery_7.setOffline_charge_amount(queryStartChargeParam.getOffline_charge_amount());
		startChargeQuery_7.setOffline_charge_flag(queryStartChargeParam.getOffline_charge_flag());
		startChargeQuery_7.setNeed_delayfee(queryStartChargeParam.getNeed_delayfee());
		startChargeQuery_7.setDelayfee_wait_time(queryStartChargeParam.getDelayfee_wait_time());
		startChargeQuery_7.setBaseEquipmentId(equipmentId);
		startChargeQuery_7.setCmdNo(StationClusterCmd.START_CHARGE);
		long timestamp = System.currentTimeMillis();
		startChargeQuery_7.setTimestamp(timestamp);
		log.info("OperatorChargeImpl:queryStartCharge:服务器收到启动充电命令:traceid={}||portid={}||chargeorderid={}||{}",
				equipmentId, queryStartChargeParam.getPortId(), queryStartChargeParam.getUser_id(),
				JSONObject.toJSONString(queryStartChargeParam));

		String cg_logic_server = System.getProperty(DynamicMQCategoryService.DYN_STATION_LOCAL_TOPIC);

		CmdProcessRecord cmdProcessRecord = new CmdProcessRecord();
		cmdProcessRecord.setCmd_no(startChargeQuery_7.getCmdNo().getCmdNo() + "");
		cmdProcessRecord.setDetail_msg(JSONObject.toJSONString(startChargeQuery_7));
		cmdProcessRecord.setLogic_server_ip(IpUtils.getHost());
		cmdProcessRecord.setStage("收到请求：queryStartCharge," + cg_logic_server);
		cmdProcessRecord.setStatus((short) 0);
		cmdProcessRecord.setToken("" + startChargeQuery_7.getTimestamp());
		cmdProcessRecord.setLog_time(new Date());
		cmdProcessRecordRepository.save(cmdProcessRecord);

		// 枪号
		String connectorId = equipmentId + EncodeUtil.formatGunNo(queryStartChargeParam.getPortId());
		// 查询枪状态，是否已经插枪
		String status = stringRedisTemplate.opsForValue().get(RedisConstant.UNICRON_CONNECTTOR_STATUS + connectorId);

		if (Integer.parseInt(status) == 0) {
			log.info("用户启动充电,但检测到未插枪，请插枪后再试，status={}", status);
		}
		//
		ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
		if (channelService != null) {
			log.info("本节点处理启动命令！");
			boolean result = commandWorker.startCharge(channelService, startChargeQuery_7);
			if (!result) {
				return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
			}
		} else {
			String msg = startChargeQuery_7.toString();
			kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
			log.info("发起启动充电命令投递成功={}", msg);
		}
		return TResponse.valueOf(TResponse.Status.OK);

	}

	/**
	 * 104 查询充电状态
	 */
	@Override
	public TResponse<String> queryEquipChargeStatus(ChargeStatusParam chargeStatusParam) {
		log.info("服务器收到充电状态查询命令:{}", JSONObject.toJSONString(chargeStatusParam));

		String equipmentId = chargeStatusParam.getEquipmentId();
		ChargeStatus_115 chargeStatus_115 = new ChargeStatus_115();
		chargeStatus_115.setBaseEquipmentId(equipmentId);
		chargeStatus_115.setEquipmentId(equipmentId);
		chargeStatus_115.setCmdNo(StationClusterCmd.QUERY_CHARGE_STATUS);
		chargeStatus_115.setGun_no(chargeStatusParam.getGun_no());
		long timestamp = System.currentTimeMillis();
		chargeStatus_115.setTimestamp(timestamp);

		ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
		if (channelService != null) {
			log.info("本节点处理命令！");
			boolean result = commandWorker.queryChargeStatus(channelService, chargeStatus_115);
			if (!result) {
				return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
			}
		} else {
			String msg = chargeStatus_115.toString();
			kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
			log.info("发起查询充电状态命令投递成功={}", msg);
		}

		return TResponse.valueOf(TResponse.Status.OK);
	}

	/**
	 * 5 发起停止充电
	 */
	@Override
	public TResponse<String> queryStopCharge(QueryStopChargeParam queryStopChargeParam) {
		log.info("服务器收到停止充电命令:{}", JSONObject.toJSONString(queryStopChargeParam));

		String equipmentId = queryStopChargeParam.getEquipmentId();
		// ChannelService channelService =
		// PubChannelUtil.getChannelService(equipmentId,
		// stationManagementService);
		// if(channelService==null){
		// return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
		// }

		ChargeControlQuery_5 chargeControlQuery_5 = new ChargeControlQuery_5();
		chargeControlQuery_5.setGun_no(queryStopChargeParam.getPortId());
		// 命令启始标志为停止
		String start_no = "2";
		chargeControlQuery_5.setStart_no(start_no);
		chargeControlQuery_5.setCmd_size("1");
		chargeControlQuery_5.setCmd_length("4");
		chargeControlQuery_5.setData_body(MqttNetMsg.getDefaultControlParam());
		chargeControlQuery_5.setBaseEquipmentId(equipmentId);
		StationClusterCmd cmdNo = StationClusterCmd.STOP_CHARGE;
		chargeControlQuery_5.setCmdNo(cmdNo);
		long timestamp = System.currentTimeMillis();
		chargeControlQuery_5.setTimestamp(timestamp);
		String connectorId = "";
		// String chargeSeq = "";
		String lastStartChargeSeq = "";
		try {
			connectorId = ConnectorUtils.getConnectorId(equipmentId, queryStopChargeParam.getPortId() + "");

			// chargeSeq =
			// stringRedisTemplate.opsForValue().get(RedisConstant.UNICRON_CONNECTTOR_CHARGESEQ_REL
			// + connectorId);
			lastStartChargeSeq = stringRedisTemplate.opsForValue()
					.get(RedisConstant.UNICRON_CONNECTTOR_CHARGESEQ_REL + connectorId);
		} catch (Exception e) {
			e.printStackTrace();
		}

		log.info(
				"OperatorChargeImpl:queryStopCharge:服务器收到终止充电命令:traceid={}||"
						+ "connectorId={}||lastStartChargeSeq={}||data={}",
				equipmentId, connectorId, lastStartChargeSeq, JSONObject.toJSONString(queryStopChargeParam));

		String cg_logic_server = System.getProperty(DynamicMQCategoryService.DYN_STATION_LOCAL_TOPIC);

		CmdProcessRecord cmdProcessRecord = new CmdProcessRecord();
		cmdProcessRecord.setCmd_no(chargeControlQuery_5.getCmdNo().getCmdNo() + "");
		cmdProcessRecord.setDetail_msg(JSONObject.toJSONString(chargeControlQuery_5));
		cmdProcessRecord.setLogic_server_ip(IpUtils.getHost());
		cmdProcessRecord.setStage("收到请求：queryStopCharge," + cg_logic_server);
		cmdProcessRecord.setStatus((short) 0);
		cmdProcessRecord.setToken("" + chargeControlQuery_5.getTimestamp());
		cmdProcessRecord.setLog_time(new Date());
		cmdProcessRecordRepository.save(cmdProcessRecord);

		ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
		if (channelService != null) {
			log.info("本节点处理停止充电命令！");
			boolean result = commandWorker.stopCharge(channelService, chargeControlQuery_5);
			if (!result) {
				return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
			}
		} else {

			String msg = chargeControlQuery_5.toString();
			kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
			log.info("发起停止充电命令投递成功={}", msg);
		}

		return TResponse.valueOf(TResponse.Status.OK);
	}

	/**
	 * 查询历史账单
	 */
	public TResponse<String> queryHistoryOrder(HistoryOrderQuery_403 historyOrderQuery_403) {
		log.info("服务器收到查询历史账单命令:{}", JSONObject.toJSONString(historyOrderQuery_403));

		String equipmentId = historyOrderQuery_403.getEquipmentId();
		historyOrderQuery_403.setBaseEquipmentId(equipmentId);
		StationClusterCmd cmdNo = StationClusterCmd.QUERY_HISTORY_ORDER;
		historyOrderQuery_403.setCmdNo(cmdNo);

		long timestamp = System.currentTimeMillis();
		historyOrderQuery_403.setTimestamp(timestamp);

		ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
		if (channelService != null) {
			log.info("本节点处理查询历史账单命令！");
			boolean result = commandWorker.queryHistoryOrder(channelService, historyOrderQuery_403);
			if (!result) {
				return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
			}
		} else {
			String msg = historyOrderQuery_403.toString();
			kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);

			log.info("查询历史账单命令投递成功={}", msg);
		}

		return TResponse.valueOf(TResponse.Status.OK);
	}

	@Override
	public TResponse<String> queryRecentOrder(RecentOrderQuery_205 recentOrderQuery_205) {
		log.info("服务器收查询最近账单命令:{}", JSONObject.toJSONString(recentOrderQuery_205));

		String startChargeSeq = recentOrderQuery_205.getCardNum();
		String connectorId = stringRedisTemplate.opsForValue()
				.get(RedisConstant.UNICRON_CONNECTTOR_REL + startChargeSeq);
		if (StringUtils.isBlank(connectorId)) {
			return TResponse.valueOf(TResponse.Status.ILLEGAL_PARAMETER);
		}

		int length = connectorId.length();
		String equipmentId = connectorId.substring(0, length - 2);
		String portId = connectorId.substring(length - 1, length);

		recentOrderQuery_205.setBaseEquipmentId(equipmentId);
		recentOrderQuery_205.setPortId(portId);
		StationClusterCmd cmdNo = StationClusterCmd.QUERY_RECENT_ORDER;
		recentOrderQuery_205.setCmdNo(cmdNo);
		long timestamp = System.currentTimeMillis();
		recentOrderQuery_205.setTimestamp(timestamp);

		ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
		if (channelService != null) {
			log.info("本节点处理查询最近账单命令！");
			boolean result = commandWorker.queryRecentOrder(channelService, recentOrderQuery_205);
			if (!result) {
				return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
			}
		} else {
			String msg = recentOrderQuery_205.toString();
			kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
			log.info("查询最近账单命令投递成功={}", msg);
		}

		return TResponse.valueOf(TResponse.Status.OK);
	}

	// 地锁控制
	public TResponse<String> lockRequest(LockRequestParam lockRequestParam) {
		log.info("服务器收到电子锁控制命令:{}", JSONObject.toJSONString(lockRequestParam));

		String equipmentId = lockRequestParam.getEquipmentId();
		LockRequest_23 lockRequest_23 = new LockRequest_23();
		lockRequest_23.setEquipmentId(equipmentId);
		lockRequest_23.setGunNo(lockRequestParam.getGunNo());
		lockRequest_23.setBaseEquipmentId(equipmentId);
		lockRequest_23.setCmdNo(StationClusterCmd.LOCK_REQUEST);
		lockRequest_23.setLockType(lockRequestParam.getLockType());
		long timestamp = System.currentTimeMillis();
		lockRequest_23.setTimestamp(timestamp);

		ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
		if (channelService != null) {
			log.info("本节点处理启动命令！");
			boolean result = commandWorker.lockControl(channelService, lockRequest_23);
			if (!result) {
				return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
			}

		} else {
			String msg = lockRequest_23.toString();
			kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
			log.info("地锁控制命令投递成功={}", msg);
		}

		return TResponse.valueOf(TResponse.Status.OK);
	}

	@Override
	public TResponse<String> queryStopChargeByChargeSeq(QueryStopChargeParam queryStopChargeParam) {
		log.info("服务器收到停止充电命令:{}", JSONObject.toJSONString(queryStopChargeParam));

		String equipmentId = queryStopChargeParam.getEquipmentId();

		ChargeStopQuery_11 chargeStopQuery_11 = new ChargeStopQuery_11();
		chargeStopQuery_11.setGun_no(queryStopChargeParam.getPortId());

		// 命令启始标志为停止
		String start_no = "2";
		chargeStopQuery_11.setCharge_seq(queryStopChargeParam.getStartChargeSeq());;
		chargeStopQuery_11.setEquipment_id(queryStopChargeParam.getBaseEquipmentId());;
		chargeStopQuery_11.setBaseEquipmentId(equipmentId);
		StationClusterCmd cmdNo = StationClusterCmd.STOP_CHARGEBYSEQ;
		chargeStopQuery_11.setCmdNo(cmdNo);
		long timestamp = System.currentTimeMillis();
		chargeStopQuery_11.setTimestamp(timestamp);
		
		
		
		String connectorId = "";
		// String chargeSeq = "";
		String lastStartChargeSeq = "";
		try {
			connectorId = ConnectorUtils.getConnectorId(equipmentId, queryStopChargeParam.getPortId() + "");

			// chargeSeq =
			// stringRedisTemplate.opsForValue().get(RedisConstant.UNICRON_CONNECTTOR_CHARGESEQ_REL
			// + connectorId);
			lastStartChargeSeq = stringRedisTemplate.opsForValue()
					.get(RedisConstant.UNICRON_CONNECTTOR_CHARGESEQ_REL + connectorId);
		} catch (Exception e) {
			e.printStackTrace();
		}

		log.info(
				"OperatorChargeImpl:queryStopCharge:服务器收到终止充电命令:traceid={}||"
						+ "connectorId={}||lastStartChargeSeq={}||data={}",
				equipmentId, connectorId, lastStartChargeSeq, JSONObject.toJSONString(queryStopChargeParam));

		String cg_logic_server = System.getProperty(DynamicMQCategoryService.DYN_STATION_LOCAL_TOPIC);

		CmdProcessRecord cmdProcessRecord = new CmdProcessRecord();
		cmdProcessRecord.setCmd_no(chargeStopQuery_11.getCmdNo().getCmdNo() + "");
		cmdProcessRecord.setDetail_msg(JSONObject.toJSONString(queryStopChargeParam));
		cmdProcessRecord.setLogic_server_ip(IpUtils.getHost());
		cmdProcessRecord.setStage("收到请求：queryStopCharge," + cg_logic_server);
		cmdProcessRecord.setStatus((short) 0);
		cmdProcessRecord.setToken("" + chargeStopQuery_11.getTimestamp());
		cmdProcessRecord.setLog_time(new Date());
		cmdProcessRecordRepository.save(cmdProcessRecord);

		ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
		if (channelService != null) {
			log.info("本节点处理停止充电命令！");
			boolean result = commandWorker.stopChargeByOrder(channelService, chargeStopQuery_11);
			if (!result) {
				return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
			}
		} else {

			String msg = queryStopChargeParam.toString();
			kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
			log.info("发起停止充电命令投递成功={}", msg);
		}

		return TResponse.valueOf(TResponse.Status.OK);
	}

}
