package com.me.unicron.iot.gateway.worker.impl;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.me.epower.direct.entity.CmdProcessRecord;
import com.me.epower.direct.entity.chargecloud.QueryStopChargeParam;
import com.me.epower.direct.entity.chargecloud.monitor.UserChargeOperation;
import com.me.epower.direct.entity.constant.RedisConstant;
import com.me.epower.direct.entity.downward.AuthRequest_801;
import com.me.epower.direct.entity.downward.ChargeControlQuery_5;
import com.me.epower.direct.entity.downward.ChargeStatus_115;
import com.me.epower.direct.entity.downward.ChargeStopQuery_11;
import com.me.epower.direct.entity.downward.ChargeUnitQuery_13;
import com.me.epower.direct.entity.downward.CleanRequest_19;
import com.me.epower.direct.entity.downward.CleanRequest_21;
import com.me.epower.direct.entity.downward.DelayFeePolicyQuery_1307;
import com.me.epower.direct.entity.downward.FileVersionQuery_1107;
import com.me.epower.direct.entity.downward.FixPolicyQuery_1301;
import com.me.epower.direct.entity.downward.HistoryOrderQuery_403;
import com.me.epower.direct.entity.downward.Httpupdate_1101;
import com.me.epower.direct.entity.downward.LockRequest_23;
import com.me.epower.direct.entity.downward.LogRequest_409;
import com.me.epower.direct.entity.downward.PolicyQuery_1303;
import com.me.epower.direct.entity.downward.PowerStrategyDispatchQuery_17;
import com.me.epower.direct.entity.downward.PowerStrategyDispatch_15;
import com.me.epower.direct.entity.downward.RecentOrderQuery_205;
import com.me.epower.direct.entity.downward.ServiceFeeQuery_1305;
import com.me.epower.direct.entity.downward.StartChargeQuery_7;
import com.me.epower.direct.entity.downward.StationIntPara_1;
import com.me.epower.direct.entity.downward.StationStringPara_3;
import com.me.epower.direct.enums.ChargeSeqStatEnum;
import com.me.epower.direct.enums.CmdProcessStatus;
import com.me.epower.direct.enums.ConnectorStatusEnum;
import com.me.epower.direct.repositories.CmdProcessRecordRepository;
import com.me.epower.direct.repositories.chargecloud.monitor.UserChargeOperationRepository;
import com.me.epower.entity.ChargeResultInfo;
import com.me.epower.repositories.ChargeResultInfoRepository;
import com.me.unicron.EncodeUtil;
import com.me.unicron.Enum.errorcode.ChargeStageEnum;
import com.me.unicron.connector.ConnectorUtils;
import com.me.unicron.date.DateUtils;
import com.me.unicron.helper.ErrcodeUtils;
import com.me.unicron.iot.bootstrap.ChannelService;
import com.me.unicron.iot.gateway.service.IConnectorService;
import com.me.unicron.iot.gateway.worker.ICommandWorkerService;
import com.me.unicron.iot.ip.IpUtils;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.impl.CMD_1;
import com.me.unicron.iot.message.packer.impl.CMD_11;
import com.me.unicron.iot.message.packer.impl.CMD_1101;
import com.me.unicron.iot.message.packer.impl.CMD_1107;
import com.me.unicron.iot.message.packer.impl.CMD_115;
import com.me.unicron.iot.message.packer.impl.CMD_13;
import com.me.unicron.iot.message.packer.impl.CMD_1301;
import com.me.unicron.iot.message.packer.impl.CMD_1303;
import com.me.unicron.iot.message.packer.impl.CMD_1305;
import com.me.unicron.iot.message.packer.impl.CMD_1307;
import com.me.unicron.iot.message.packer.impl.CMD_15;
import com.me.unicron.iot.message.packer.impl.CMD_17;
import com.me.unicron.iot.message.packer.impl.CMD_19;
import com.me.unicron.iot.message.packer.impl.CMD_205;
import com.me.unicron.iot.message.packer.impl.CMD_21;
import com.me.unicron.iot.message.packer.impl.CMD_23;
import com.me.unicron.iot.message.packer.impl.CMD_3;
import com.me.unicron.iot.message.packer.impl.CMD_403;
import com.me.unicron.iot.message.packer.impl.CMD_409;
import com.me.unicron.iot.message.packer.impl.CMD_5;
import com.me.unicron.iot.message.packer.impl.CMD_509;
import com.me.unicron.iot.message.packer.impl.CMD_7;
import com.me.unicron.iot.message.packer.impl.CMD_801;
import com.me.unicron.iot.mq.service.DynamicMQCategoryService;
import com.me.unicron.iot.serverQos.CurrentQos;
import com.me.unicron.protocol.CharsetDef;
import com.me.unicron.server.share.SharedDataUtils;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

@Component
@Slf4j
public class CommandWorker implements ICommandWorkerService {

	private static final String TYPE_SET = "1";
	private static final String TYPE_QUERY = "0";

	@Autowired
	private IConnectorService iConnectorService;

	@Autowired
	CmdProcessRecordRepository cmdProcessRecordRepository;

	@Autowired
	private StringRedisTemplate redis;
	// 协议版本的入参，base
	private MqttNetMsgBase base = new MqttNetMsg();
	@Autowired
	UserChargeOperationRepository userChargeOperationRepository;

	@Autowired
	ChargeResultInfoRepository chargeResultInfoRepository;

	@Override
	public boolean startCharge(ChannelService channelService, StartChargeQuery_7 startChargeQuery_7) {
		if (channelService == null || startChargeQuery_7 == null
				|| StringUtils.isEmpty(startChargeQuery_7.getBaseEquipmentId())) {
			log.info("启动充电命令参数错误！");
			return false;
		}
		CMD_7 cmd_7 = new CMD_7();

		byte[] byte7 = cmd_7.getPayload(startChargeQuery_7, base);
		

		String equipmentId = startChargeQuery_7.getBaseEquipmentId();
		try {
			String msg = new String(byte7, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.info("traceid={}||chargeorderid={},S->C:发送启动充电命令={},databytes={}", equipmentId, startChargeQuery_7.getUser_id(),
					JSONObject.toJSONString(startChargeQuery_7),EncodeUtil.printHex(byte7));

			ConnectorStatusEnum portStatus = iConnectorService.getConnectorCurrentStatus(
					ConnectorUtils.getConnectorId(equipmentId, startChargeQuery_7.getPortId()));
			if (portStatus.getCode() != ConnectorStatusEnum.UNCHARGE.getCode()) {
				log.info("[插枪检查]发送启动充电命令时:traceid={}||orderid={},插枪状态检查={}", equipmentId,
						startChargeQuery_7.getUser_id(), JSONObject.toJSONString(portStatus));
			} else {
				log.info("[插枪检查]发送启动充电命令时:traceid={}||orderid={},插枪状态检查={}", equipmentId,
						startChargeQuery_7.getUser_id(), JSONObject.toJSONString(portStatus));
			}

			channelService.push(equipmentId, CurrentQos.getQos(), sendbytes);
			String cg_logic_server = System.getProperty(DynamicMQCategoryService.DYN_STATION_LOCAL_TOPIC);
			// 枪
			String connectorId = equipmentId + EncodeUtil.formatGunNo(startChargeQuery_7.getPortId());
			// 启动成功后，维护枪和订单的关系
			String startChargeSeq = startChargeQuery_7.getUser_id();
			ChargeStageEnum statusEnum = ChargeStageEnum.STARTING_SYN;
			String errcode = "" + ErrcodeUtils.formatIntErrcode(ChargeStageEnum.STARTING_SYN, 0);
			saveStartStopChargeInfo(equipmentId, startChargeQuery_7.getPortId(), startChargeSeq, statusEnum, "下发到桩",
					errcode);
			saveStartChargeResult(startChargeQuery_7);

			// redis.opsForValue().set(RedisConstant.UNICRON_CONNECTTOR_CHARGESEQ_REL
			// + connectorId, startChargeSeq);
			SharedDataUtils.setConnectorIdOrderidMapping(redis, connectorId, startChargeSeq);
			// 维护订单和枪的关系
			redis.opsForValue().set(RedisConstant.UNICRON_CONNECTTOR_REL + startChargeSeq, connectorId);
			// 维护启动时间
			redis.opsForHash().put(RedisConstant.UNICRON_CHARGE_START_TIME, startChargeSeq,
					String.valueOf(startChargeQuery_7.getTimestamp()));
			// 保存记录
			saveStartChargeRecord(startChargeQuery_7, cg_logic_server);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 存储启动充电信息
	 * 
	 * @param startChargeQuery_7
	 */
	private void saveStartChargeResult(StartChargeQuery_7 startChargeQuery_7) {

		// TODO Auto-generated method stub
		ChargeResultInfo chargeResultInfo = new ChargeResultInfo();
		chargeResultInfo.setEquipmentid(startChargeQuery_7.getBaseEquipmentId());
		chargeResultInfo.setOrdernum(startChargeQuery_7.getUser_id());
		chargeResultInfo.setConnectorid(startChargeQuery_7.getPortId());
		chargeResultInfo.setStarttime(DateUtils.getCurDateTime());

		chargeResultInfoRepository.save(chargeResultInfo);
	}

	@Override
	public boolean stopCharge(ChannelService channelService, ChargeControlQuery_5 chargeControlQuery_5) {
		if (channelService == null || chargeControlQuery_5 == null
				|| StringUtils.isEmpty(chargeControlQuery_5.getBaseEquipmentId())) {
			log.info("停止充电命令参数错误！");
			return false;
		}
		CMD_5 cmd_5 = new CMD_5();

		String equipmentId = chargeControlQuery_5.getBaseEquipmentId();
		String portId = chargeControlQuery_5.getGun_no();
		String connectorId = ConnectorUtils.getConnectorId(equipmentId, portId);
		// 查询订单号
		String charge_seq = redis.opsForValue().get(RedisConstant.UNICRON_CONNECTTOR_CHARGESEQ_REL + connectorId);

		byte[] byte5 = cmd_5.getPayload(chargeControlQuery_5, base);
		try {
			String msg = new String(byte5, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.info("S->C:发送停止充电命令={}", JSONObject.toJSONString(chargeControlQuery_5));
			EncodeUtil.printHex(byte5);
			channelService.push(equipmentId, CurrentQos.QoS1, sendbytes);
			String cg_logic_server = System.getProperty(DynamicMQCategoryService.DYN_STATION_LOCAL_TOPIC);

			ChargeStageEnum statusEnum = ChargeStageEnum.STOPPING_SYN;
			String errcode = "" + ErrcodeUtils.formatIntErrcode(statusEnum, 0);
			saveStartStopChargeInfo(equipmentId, portId, charge_seq, statusEnum, "下发到桩", errcode);

			CmdProcessRecord cmdProcessRecord = new CmdProcessRecord();
			cmdProcessRecord.setCmd_no(chargeControlQuery_5.getCmdNo().getCmdNo() + "");
			cmdProcessRecord.setDetail_msg(JSONObject.toJSONString(chargeControlQuery_5));
			cmdProcessRecord.setLogic_server_ip(IpUtils.getHost());
			cmdProcessRecord.setStage("下发到桩：stopCharge," + cg_logic_server);
			cmdProcessRecord.setStatus((short) CmdProcessStatus.DISPATCH_TO_STATION.getStatusNo());
			cmdProcessRecord.setToken("" + chargeControlQuery_5.getTimestamp());
			cmdProcessRecord.setLog_time(new Date());

			cmdProcessRecordRepository.save(cmdProcessRecord);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			log.info("下发停止充电命令出现异常");
			return false;
		}
		return true;
	}

	@Override
	public boolean queryHistoryOrder(ChannelService channelService, HistoryOrderQuery_403 historyOrderQuery_403) {
		if (channelService == null || historyOrderQuery_403 == null
				|| StringUtils.isEmpty(historyOrderQuery_403.getBaseEquipmentId())) {
			log.info("命令参数错误！");
			return false;
		}
		CMD_403 cmd_403 = new CMD_403();
		byte[] byte403 = cmd_403.getPayload(historyOrderQuery_403, base);
		try {
			String msg = new String(byte403, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.info("发送查询历史账单命令={}", JSONObject.toJSON(historyOrderQuery_403));
			EncodeUtil.printHex(byte403);
			channelService.push(historyOrderQuery_403.getBaseEquipmentId(), CurrentQos.QoS, sendbytes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean queryRecentOrder(ChannelService channelService, RecentOrderQuery_205 recentOrderQuery_205) {
		if (channelService == null || recentOrderQuery_205 == null
				|| StringUtils.isEmpty(recentOrderQuery_205.getBaseEquipmentId())) {
			log.info("命令参数错误！");
			return false;
		}
		CMD_205 cmd_205 = new CMD_205();
		byte[] byte205 = cmd_205.getPayload(recentOrderQuery_205, base);
		try {
			String msg = new String(byte205, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.info("发送查询账单命令={}", JSONObject.toJSON(recentOrderQuery_205));
			EncodeUtil.printHex(byte205);
			channelService.push(recentOrderQuery_205.getBaseEquipmentId(), CurrentQos.QoS, sendbytes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public boolean queryChargeStatus(ChannelService channelService, ChargeStatus_115 chargeStatus_115) {
		if (channelService == null || chargeStatus_115 == null
				|| StringUtils.isEmpty(chargeStatus_115.getBaseEquipmentId())) {
			log.info("命令参数错误！");
			return false;
		}

		CMD_115 cmd_115 = new CMD_115();
		byte[] byte115 = cmd_115.getPayload(chargeStatus_115, base);
		try {
			String msg = new String(byte115, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.debug("发送查询历史账单命令={}", JSONObject.toJSON(chargeStatus_115));
			EncodeUtil.printHex(byte115);
			channelService.push(chargeStatus_115.getBaseEquipmentId(), CurrentQos.QoS, sendbytes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public boolean lockControl(ChannelService channelService, LockRequest_23 lockRequest_23) {
		if (channelService == null || lockRequest_23 == null
				|| StringUtils.isEmpty(lockRequest_23.getBaseEquipmentId())) {
			log.info("命令参数错误！");
			return false;
		}

		CMD_23 cmd_23 = new CMD_23();
		log.info("handleLockRequest_23={}", JSONObject.toJSON(lockRequest_23));
		byte[] byteResult = cmd_23.getPayload(lockRequest_23, base);
		try {
			String msg = new String(byteResult, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.debug("发送锁控制命令:");
			EncodeUtil.printHex(byteResult);
			channelService.push(lockRequest_23.getBaseEquipmentId(), CurrentQos.QoS, sendbytes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public boolean doRWIntPara(ChannelService channelService, StationIntPara_1 stationIntPara_1) {
		if (channelService == null || stationIntPara_1 == null
				|| StringUtils.isEmpty(stationIntPara_1.getBaseEquipmentId())) {
			log.info("命令参数错误！");
			return false;
		}

		CMD_1 cmd_1 = new CMD_1();
		byte[] byte1 = cmd_1.getPayload(stationIntPara_1, base);
		try {
			String msg = new String(byte1, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.debug("发送设置充电桩整形工作参数:");
			EncodeUtil.printHex(byte1);
			channelService.push(stationIntPara_1.getEquipmentId(), CurrentQos.QoS, sendbytes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public boolean doRWStringPara(ChannelService channelService, StationStringPara_3 stationStringPara_3) {
		if (channelService == null || stationStringPara_3 == null
				|| StringUtils.isEmpty(stationStringPara_3.getBaseEquipmentId())) {
			log.info("命令参数错误！");
			return false;
		}

		try {
			String cmd_type = stationStringPara_3.getCmd_type();
			if (TYPE_SET.equals(cmd_type)) {
				String str = stationStringPara_3.getData_body();
				byte[] byteStr = str.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
				String strISO = new String(byteStr, CharsetDef.CHARSET);
				stationStringPara_3.setData_body(strISO);
			}
			CMD_3 cmd_3 = new CMD_3();
			byte[] byte3 = cmd_3.getPayload(stationStringPara_3, base);
			String msg = new String(byte3, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.info("发送设置充电桩字符串形工作参数,编码前:byte3={},traceid={}", EncodeUtil.printHex(byte3),stationStringPara_3.getBaseEquipmentId());
			//log.info("发送设置充电桩字符串形工作参数,编码后,实际发送:sendbytes={},traceid={}", EncodeUtil.printHex(sendbytes)
			//		,stationStringPara_3.getBaseEquipmentId());

			// EncodeUtil.printHex(byte3);
			channelService.push(stationStringPara_3.getEquipmentId(), CurrentQos.QoS, sendbytes);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public boolean doStationControl(ChannelService channelService, ChargeControlQuery_5 chargeControlQuery_5) {
		if (channelService == null || chargeControlQuery_5 == null
				|| StringUtils.isEmpty(chargeControlQuery_5.getBaseEquipmentId())) {
			log.info("命令参数错误！");
			return false;
		}
		CMD_5 cmd_5 = new CMD_5();
		log.info("chargeControlQuery_5={}", JSONObject.toJSON(chargeControlQuery_5));
		byte[] byteResult = cmd_5.getPayload(chargeControlQuery_5, base);
		try {
			String msg = new String(byteResult, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.debug("发送控制命令:");
			EncodeUtil.printHex(byteResult);
			channelService.push(chargeControlQuery_5.getBaseEquipmentId(), CurrentQos.QoS, sendbytes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	@Override
	public boolean doCleanIntPara(ChannelService channelService, CleanRequest_19 cleanRequest_19) {
		if (channelService == null || cleanRequest_19 == null
				|| StringUtils.isEmpty(cleanRequest_19.getBaseEquipmentId())) {
			log.debug("命令参数错误！");
			return false;
		}
		CMD_19 cmd_19 = new CMD_19();
		log.info("CleanRequest_19={}", JSONObject.toJSON(cleanRequest_19));
		byte[] byteResult = cmd_19.getPayload(cleanRequest_19, base);
		try {
			String msg = new String(byteResult, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.debug("发送清除命令:");
			EncodeUtil.printHex(byteResult);
			channelService.push(cleanRequest_19.getBaseEquipmentId(), CurrentQos.QoS, sendbytes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean doCleanStringPara(ChannelService channelService, CleanRequest_21 cleanRequest_21) {
		if (channelService == null || cleanRequest_21 == null
				|| StringUtils.isEmpty(cleanRequest_21.getBaseEquipmentId())) {
			log.info("命令参数错误！");
			return false;
		}

		CMD_21 cmd_21 = new CMD_21();
		log.debug("CleanRequest_21={}", JSONObject.toJSON(cleanRequest_21));
		byte[] byteResult = cmd_21.getPayload(cleanRequest_21, base);
		try {
			String msg = new String(byteResult, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.debug("发送清除字符串参数命令:");
			EncodeUtil.printHex(byteResult);
			channelService.push(cleanRequest_21.getBaseEquipmentId(), CurrentQos.QoS, sendbytes);
		} catch (UnsupportedEncodingException e) {
			log.error("handleIntCleanRequestCmd error", e);
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public boolean queryLog(ChannelService channelService, LogRequest_409 logRequest_409) {
		if (channelService == null || logRequest_409 == null
				|| StringUtils.isEmpty(logRequest_409.getBaseEquipmentId())) {
			log.debug("命令参数错误！");
			return false;
		}
		CMD_409 cmd_409 = new CMD_409();
		log.info("handleLogRequestCmd={}", JSONObject.toJSON(logRequest_409));
		byte[] byteResult = cmd_409.getPayload(logRequest_409, base);
		try {
			String msg = new String(byteResult, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.debug("发送日志查询命令:");
			EncodeUtil.printHex(byteResult);
			channelService.push(logRequest_409.getBaseEquipmentId(), CurrentQos.QoS, sendbytes);
		} catch (UnsupportedEncodingException e) {
			log.error("handleStationControlCmd error", e);
			e.printStackTrace();
			return false;

		}
		return true;
	}

	@Override
	public boolean doHttpupdate(ChannelService channelService, Httpupdate_1101 httpupdate_1101) {
		if (channelService == null || httpupdate_1101 == null
				|| StringUtils.isEmpty(httpupdate_1101.getBaseEquipmentId())) {
			log.debug("命令参数错误！");
			return false;
		}

		CMD_1101 cmd_1101 = new CMD_1101();

		byte[] byteResult = cmd_1101.getPayload(httpupdate_1101, base);
		try {
			String msg = new String(byteResult, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.debug("发送HTTP升级命令:");
			EncodeUtil.printHex(byteResult);
			channelService.push(httpupdate_1101.getBaseEquipmentId(), CurrentQos.QoS, sendbytes);

		} catch (UnsupportedEncodingException e) {
			log.error("handleStationHttpUpdate error", e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean queryFileVersion(ChannelService channelService, FileVersionQuery_1107 fileVersionQuery_1107) {
		if (channelService == null || fileVersionQuery_1107 == null
				|| StringUtils.isEmpty(fileVersionQuery_1107.getBaseEquipmentId())) {
			log.info("命令参数错误！");
			return false;
		}

		CMD_1107 cmd_1107 = new CMD_1107();

		byte[] byteResult = cmd_1107.getPayload(fileVersionQuery_1107, base);
		try {
			String msg = new String(byteResult, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.debug("发送文件版本查询:");
			EncodeUtil.printHex(byteResult);
			channelService.push(fileVersionQuery_1107.getBaseEquipmentId(), CurrentQos.QoS, sendbytes);

		} catch (UnsupportedEncodingException e) {
			log.error("handleFileVersionQuery error", e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean doRWFixedPowerTariffPolicy(ChannelService channelService, FixPolicyQuery_1301 fixPolicyQuery_1301) {
		if (channelService == null || fixPolicyQuery_1301 == null
				|| StringUtils.isEmpty(fixPolicyQuery_1301.getBaseEquipmentId())) {
			log.debug("命令参数错误！");
			return false;
		}

		CMD_1301 cmd_1301 = new CMD_1301();
		byte[] byte1301 = cmd_1301.getPayload(fixPolicyQuery_1301, base);
		try {
			String msg = new String(byte1301, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.debug("发送固定价格命令:");
			EncodeUtil.printHex(byte1301);
			channelService.push(fixPolicyQuery_1301.getBaseEquipmentId(), CurrentQos.QoS, sendbytes);
		} catch (UnsupportedEncodingException e) {
			log.error("handleRWFixedPowerTariffPolicy error={}", e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean doRWPowerTariffPolicy(ChannelService channelService, PolicyQuery_1303 policyQuery_1303) {
		if (channelService == null || policyQuery_1303 == null
				|| StringUtils.isEmpty(policyQuery_1303.getBaseEquipmentId())) {
			log.info("命令参数错误！");
			return false;
		}
		CMD_1303 cmd_1303 = new CMD_1303();
		byte[] byte1303 = cmd_1303.getPayload(policyQuery_1303, base);
		try {
			String msg = new String(byte1303, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.debug("查询峰平谷价格命令:");
			EncodeUtil.printHex(byte1303);
			channelService.push(policyQuery_1303.getBaseEquipmentId(), CurrentQos.QoS, sendbytes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;

		}

		return true;
	}

	@Override
	public boolean doRWServiceFeePolicy(ChannelService channelService, ServiceFeeQuery_1305 serviceFeeQuery_1305) {
		if (channelService == null || serviceFeeQuery_1305 == null
				|| StringUtils.isEmpty(serviceFeeQuery_1305.getBaseEquipmentId())) {
			log.info("命令参数错误！");
			return false;
		}

		CMD_1305 cmd_1305 = new CMD_1305();
		byte[] byte1305 = cmd_1305.getPayload(serviceFeeQuery_1305, base);
		try {
			String msg = new String(byte1305, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.info("发送服务费价格命令:");
			EncodeUtil.printHex(byte1305);
			channelService.push(serviceFeeQuery_1305.getBaseEquipmentId(), CurrentQos.QoS, sendbytes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean doRWDelayFeePolicy(ChannelService channelService,
			DelayFeePolicyQuery_1307 delayFeePolicyQuery_1307) {
		if (channelService == null || delayFeePolicyQuery_1307 == null
				|| StringUtils.isEmpty(delayFeePolicyQuery_1307.getBaseEquipmentId())) {
			log.info("命令参数错误！");
			return false;
		}

		CMD_1307 cmd_1307 = new CMD_1307();
		byte[] byte1307 = cmd_1307.getPayload(delayFeePolicyQuery_1307, base);
		try {
			String msg = new String(byte1307, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.debug("发送延时费价格命令:");
			EncodeUtil.printHex(byte1307);
			channelService.push(delayFeePolicyQuery_1307.getBaseEquipmentId(), CurrentQos.QoS, sendbytes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;

		}
		return true;
	}

	@Override
	public boolean authRequest(ChannelService channelService, AuthRequest_801 authRequest_801) {
		if (channelService == null || authRequest_801 == null
				|| StringUtils.isEmpty(authRequest_801.getEquipmentId())) {
			log.info("命令参数错误！");
			return false;
		}

		CMD_801 cmd_801 = new CMD_801();
		byte[] byte801 = cmd_801.getPayload(authRequest_801, base);
		try {
			String msg = new String(byte801, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.info("发送认证命令:");
			EncodeUtil.printHex(byte801);
			channelService.push(authRequest_801.getEquipmentId(), CurrentQos.QoS, sendbytes);
		} catch (UnsupportedEncodingException e) {
			log.error("authRequest error", e);
			return false;
		}
		return true;
	}

	private void saveStartStopChargeInfo(String destEquipmentId, String portId, String orderid,
			ChargeStageEnum statusEnum, String msg, String errorcode) {
		UserChargeOperation userChargeOperation = new UserChargeOperation();
		userChargeOperation.setEquipmentid(destEquipmentId);
		userChargeOperation.setLogicserver(IpUtils.getHost());
		userChargeOperation.setOrderid(orderid);
		try {
			userChargeOperation.setPortid(Integer.parseInt(portId));
		} catch (Exception e) {
			e.printStackTrace();
		}

		Date calltime = new Date();
		userChargeOperation.setCalltime(calltime);
		userChargeOperation.setOptype(statusEnum.getStage());
		userChargeOperation.setCallstatus(statusEnum.getDesc());
		userChargeOperation.setMsg(msg);
		userChargeOperation.setErrorcode(errorcode);
		userChargeOperationRepository.save(userChargeOperation);

	}

	private void saveStartChargeRecord(StartChargeQuery_7 startChargeQuery_7, String cg_logic_server) {
		CmdProcessRecord cmdProcessRecord = new CmdProcessRecord();
		cmdProcessRecord.setCmd_no(startChargeQuery_7.getCmdNo().getCmdNo() + "");
		cmdProcessRecord.setDetail_msg(JSONObject.toJSONString(startChargeQuery_7));
		cmdProcessRecord.setLogic_server_ip(IpUtils.getHost());
		cmdProcessRecord.setStage("下发到桩：startCharge," + cg_logic_server);
		cmdProcessRecord.setStatus((short) CmdProcessStatus.DISPATCH_TO_STATION.getStatusNo());
		cmdProcessRecord.setToken("" + startChargeQuery_7.getTimestamp());
		cmdProcessRecord.setLog_time(new Date());

		cmdProcessRecordRepository.save(cmdProcessRecord);
	}

	@Override
	public boolean queryLogBms(ChannelService channelService, LogRequest_409 logRequest_409) {
		// TODO Auto-generated method stub
		if (channelService == null || logRequest_409 == null
				|| StringUtils.isEmpty(logRequest_409.getBaseEquipmentId())) {
			log.debug("命令参数错误！");
			return false;
		}
		CMD_509 cmd_509 = new CMD_509();
		log.info("handleBmsLogRequestCmd={}", JSONObject.toJSON(logRequest_409));
		byte[] byteResult = cmd_509.getPayload(logRequest_409, base);
		try {
			String msg = new String(byteResult, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.info("发送BMS日志查询命令:");
			EncodeUtil.printHex(byteResult);
			channelService.push(logRequest_409.getBaseEquipmentId(), CurrentQos.QoS, sendbytes);
		} catch (UnsupportedEncodingException e) {
			log.error("handleStationControlCmd error", e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean stopChargeByOrder(ChannelService channelService, ChargeStopQuery_11 chargeStopQuery_11) {
		// TODO Auto-generated method stub
		if (channelService == null || chargeStopQuery_11 == null
				|| StringUtils.isEmpty(chargeStopQuery_11.getBaseEquipmentId())) {
			log.info("停止充电命令参数错误！");
			return false;
		}
		CMD_11 cmd_11 = new CMD_11();

		String equipmentId = chargeStopQuery_11.getBaseEquipmentId();
		String portId = chargeStopQuery_11.getGun_no();
		String connectorId = ConnectorUtils.getConnectorId(equipmentId, portId);
		// 查询订单号
		String charge_seq = chargeStopQuery_11.getCharge_seq();

		byte[] byte5 = cmd_11.getPayload(chargeStopQuery_11, base);
		try {
			String msg = new String(byte5, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.info("S->C:发送停止充电命令={}", JSONObject.toJSONString(chargeStopQuery_11));
			EncodeUtil.printHex(byte5);
			channelService.push(equipmentId, CurrentQos.QoS1, sendbytes);
			String cg_logic_server = System.getProperty(DynamicMQCategoryService.DYN_STATION_LOCAL_TOPIC);

			ChargeStageEnum statusEnum = ChargeStageEnum.STOPPING_SYN;
			String errcode = "" + ErrcodeUtils.formatIntErrcode(statusEnum, 0);
			saveStartStopChargeInfo(equipmentId, portId, charge_seq, statusEnum, "下发到桩", errcode);

			CmdProcessRecord cmdProcessRecord = new CmdProcessRecord();
			cmdProcessRecord.setCmd_no(chargeStopQuery_11.getCmdNo().getCmdNo() + "");
			cmdProcessRecord.setDetail_msg(JSONObject.toJSONString(chargeStopQuery_11));
			cmdProcessRecord.setLogic_server_ip(IpUtils.getHost());
			cmdProcessRecord.setStage("下发到桩：stopCharge," + cg_logic_server);
			cmdProcessRecord.setStatus((short) CmdProcessStatus.DISPATCH_TO_STATION.getStatusNo());
			cmdProcessRecord.setToken("" + chargeStopQuery_11.getTimestamp());
			cmdProcessRecord.setLog_time(new Date());

			cmdProcessRecordRepository.save(cmdProcessRecord);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			log.info("下发停止充电命令出现异常");
			return false;
		}
		return true;
	}

	@Override
	public boolean queryChargeUnitInfo(ChannelService channelService, ChargeUnitQuery_13 chargeUnitQuery_13) {
		// TODO Auto-generated method stub
		if (channelService == null || chargeUnitQuery_13 == null
				|| StringUtils.isEmpty(chargeUnitQuery_13.getBaseEquipmentId())) {
			log.info("命令参数错误！");
			return false;
		}
		CMD_13 cmd_13 = new CMD_13();
		String equipmentId = chargeUnitQuery_13.getBaseEquipmentId();
		byte[] byte13 = cmd_13.getPayload(chargeUnitQuery_13, base);
		try {
			String msg = new String(byte13, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.info("S->C:发送命令={}", JSONObject.toJSONString(chargeUnitQuery_13));
			EncodeUtil.printHex(byte13);

			channelService.push(equipmentId, CurrentQos.QoS1, sendbytes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			log.info("出现异常");
			return false;
		}
		return true;
	}

	@Override
	public boolean executeChargeUnitPowerDispatch(ChannelService channelService,
			PowerStrategyDispatch_15 powerStrategyDispatch_15) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		if (channelService == null || powerStrategyDispatch_15 == null
				|| StringUtils.isEmpty(powerStrategyDispatch_15.getBaseEquipmentId())) {
			log.info("命令参数错误！");
			return false;
		}
		CMD_15 cmd_15 = new CMD_15();
		String equipmentId = powerStrategyDispatch_15.getBaseEquipmentId();
		byte[] byte13 = cmd_15.getPayload(powerStrategyDispatch_15, base);
		try {
			String msg = new String(byte13, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.info("S->C:发送命令={}", JSONObject.toJSONString(powerStrategyDispatch_15));
			EncodeUtil.printHex(byte13);

			channelService.push(equipmentId, CurrentQos.QoS1, sendbytes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			log.info("出现异常");
			return false;
		}
		return true;
	}

	@Override
	public boolean queryPowerDispatchStategy(ChannelService channelService,
			PowerStrategyDispatchQuery_17 powerStrategyDispatchQuery_17) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		if (channelService == null || powerStrategyDispatchQuery_17 == null
				|| StringUtils.isEmpty(powerStrategyDispatchQuery_17.getBaseEquipmentId())) {
			log.info("命令参数错误！");
			return false;
		}
		CMD_17 cmd_17 = new CMD_17();
		String equipmentId = powerStrategyDispatchQuery_17.getBaseEquipmentId();
		byte[] byte13 = cmd_17.getPayload(powerStrategyDispatchQuery_17, base);
		try {
			String msg = new String(byte13, CharsetDef.CHARSET);
			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.info("S->C:发送命令={}", JSONObject.toJSONString(powerStrategyDispatchQuery_17));
			EncodeUtil.printHex(byte13);

			channelService.push(equipmentId, CurrentQos.QoS1, sendbytes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			log.info("出现异常");
			return false;
		}
		return true;
	}

}
