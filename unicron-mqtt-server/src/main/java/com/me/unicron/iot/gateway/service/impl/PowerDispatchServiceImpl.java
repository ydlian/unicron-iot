package com.me.unicron.iot.gateway.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.me.epower.component.TResponse;
import com.me.epower.direct.entity.chargecloud.ChargeUnitQueryParam;
import com.me.epower.direct.entity.chargecloud.PolicySetParam;
import com.me.epower.direct.entity.chargecloud.PowerDispatchStategyQueryParam;
import com.me.epower.direct.entity.chargecloud.StationPowerDispatchParam;
import com.me.epower.direct.entity.downward.ChargeUnitQuery_13;
import com.me.epower.direct.entity.downward.FixPolicyQuery_1301;
import com.me.epower.direct.entity.downward.PowerStrategyDispatchQuery_17;
import com.me.epower.direct.entity.downward.PowerStrategyDispatch_15;
import com.me.epower.direct.enums.StationClusterCmd;
import com.me.epower.repositories.OperatorDeviceRepository;
import com.me.unicron.iot.bootstrap.ChannelService;
import com.me.unicron.iot.bootstrap.channel.StationManagementService;
import com.me.unicron.iot.gateway.EnvConst;
import com.me.unicron.iot.gateway.service.IPowerDispatchService;
import com.me.unicron.iot.gateway.service.util.PubChannelUtil;
import com.me.unicron.iot.gateway.worker.impl.CommandWorker;
import com.me.unicron.iot.kafka.KafkaProducer;
import com.me.unicron.iot.message.packer.impl.CMD_13;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PowerDispatchServiceImpl implements IPowerDispatchService {

	@Autowired
	private StationManagementService stationManagementService;

	@Autowired
	private KafkaProducer kafkaProducer;

	@Autowired
	private CommandWorker commandWorker;

	@Override
	public TResponse<String> queryChargeUnitInfo(ChargeUnitQueryParam chargeUnitQueryParam) {
		// TODO Auto-generated method stub

		StationClusterCmd cmdNo = StationClusterCmd.QUERY_CHARGE_UNIT_INFO;
		chargeUnitQueryParam.setCmdNo(cmdNo);
		log.info("queryChargeUnitInfo||traceid={}||data={}", chargeUnitQueryParam.getEquipmentId(),
				JSONObject.toJSON(chargeUnitQueryParam));

		String baseEquipmentId = chargeUnitQueryParam.getEquipmentId();
		ChargeUnitQuery_13 chargeUnitQuery_13 = new ChargeUnitQuery_13();
		chargeUnitQuery_13.setEquipment_id(baseEquipmentId);
		chargeUnitQuery_13.setBaseEquipmentId(baseEquipmentId);
		chargeUnitQuery_13.setCmdNo(StationClusterCmd.QUERY_CHARGE_UNIT_INFO);
		long timestamp = System.currentTimeMillis();
		chargeUnitQuery_13.setTimestamp(timestamp);

		ChannelService channelService = PubChannelUtil.getChannelService(baseEquipmentId, stationManagementService);
		if (channelService != null) {
			log.info("本节点处理启动命令！");
			boolean result = commandWorker.queryChargeUnitInfo(channelService, chargeUnitQuery_13);
			if (!result) {
				return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
			}

		} else {
			String msg = baseEquipmentId.toString();
			kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
			log.info("命令投递成功={}", msg);
		}

		return TResponse.valueOf(TResponse.Status.OK);

	}

	@Override
	public TResponse<String> executeChargeUnitPowerDispatch(StationPowerDispatchParam stationPowerDispatchParam) {

		// TODO Auto-generated method stub
		stationPowerDispatchParam.setCmdNo(StationClusterCmd.DISPATCH_POWOER_STRATEGY);
		log.info("executeChargeUnitPowerDispatch||traceid={}||data={}", stationPowerDispatchParam.getEquipmentId(),
				JSONObject.toJSON(stationPowerDispatchParam));
		String baseEquipmentId = stationPowerDispatchParam.getEquipmentId();
		PowerStrategyDispatch_15 powerStrategyDispatch_15 = new PowerStrategyDispatch_15();
		powerStrategyDispatch_15.setEquipment_id(baseEquipmentId);
		powerStrategyDispatch_15.setBaseEquipmentId(baseEquipmentId);
		powerStrategyDispatch_15.setCmdNo(StationClusterCmd.DISPATCH_POWOER_STRATEGY);
		long timestamp = System.currentTimeMillis();
		powerStrategyDispatch_15.setTimestamp(timestamp);
		powerStrategyDispatch_15.setGun_no("" + stationPowerDispatchParam.getGunno());
		String stategy_type = "" + stationPowerDispatchParam.getStategyType();
		powerStrategyDispatch_15.setStategy_type(stategy_type);
		if (stationPowerDispatchParam.getUnitSize() > 0) {
			String unit_num = "" + stationPowerDispatchParam.getUnitSize();
			powerStrategyDispatch_15.setUnit_num(unit_num);

			String[] unitIndex = new String[stationPowerDispatchParam.getUnitSize()];
			for (int i = 0; i < unitIndex.length; i++) {
				unitIndex[i] = "" + stationPowerDispatchParam.getUnitIndexArray().get(i);
			}
			powerStrategyDispatch_15.setUnitIndex(unitIndex);
		} else {
			powerStrategyDispatch_15.setUnit_num("0");
		}

		ChannelService channelService = PubChannelUtil.getChannelService(baseEquipmentId, stationManagementService);
		if (channelService != null) {
			log.info("本节点处理启动命令！");
			boolean result = commandWorker.executeChargeUnitPowerDispatch(channelService, powerStrategyDispatch_15);
			if (!result) {
				return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
			}

		} else {
			String msg = baseEquipmentId.toString();
			kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
			log.info("命令投递成功={}", msg);
		}

		return TResponse.valueOf(TResponse.Status.OK);
	}

	@Override
	public TResponse<String> queryPowerDispatchStategy(PowerDispatchStategyQueryParam powerDispatchStategyQueryParam) {
		// TODO Auto-generated method stub

		StationClusterCmd cmdNo = StationClusterCmd.QUERY_DISPATCH_POWOER_STRATEGY;
		powerDispatchStategyQueryParam.setCmdNo(cmdNo);
		log.info("queryPowerDispatchStategy||traceid={}||data={}", powerDispatchStategyQueryParam.getEquipmentId(),
				JSONObject.toJSON(powerDispatchStategyQueryParam));
		String baseEquipmentId = powerDispatchStategyQueryParam.getEquipmentId();
		PowerStrategyDispatchQuery_17 powerStrategyDispatchQuery_17 = new PowerStrategyDispatchQuery_17();
		powerStrategyDispatchQuery_17.setEquipment_id(baseEquipmentId);
		powerStrategyDispatchQuery_17.setBaseEquipmentId(baseEquipmentId);
		powerStrategyDispatchQuery_17.setCmdNo(StationClusterCmd.QUERY_DISPATCH_POWOER_STRATEGY);
		powerStrategyDispatchQuery_17.setGun_no("" + powerDispatchStategyQueryParam.getGunno());
		long timestamp = System.currentTimeMillis();
		powerStrategyDispatchQuery_17.setTimestamp(timestamp);

		ChannelService channelService = PubChannelUtil.getChannelService(baseEquipmentId, stationManagementService);
		if (channelService != null) {
			log.info("本节点处理启动命令！");
			boolean result = commandWorker.queryPowerDispatchStategy(channelService, powerStrategyDispatchQuery_17);
			if (!result) {
				return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
			}

		} else {
			String msg = baseEquipmentId.toString();
			kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
			log.info("命令投递成功={}", msg);
		}

		return TResponse.valueOf(TResponse.Status.OK);
	}

}
