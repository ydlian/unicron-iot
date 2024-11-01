package com.me.unicron.iot.gateway.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.me.epower.component.TResponse;
import com.me.epower.direct.entity.chargecloud.DelayFeeSetParam;
import com.me.epower.direct.entity.chargecloud.FixPolicySetParam;
import com.me.epower.direct.entity.chargecloud.PolicySetParam;
import com.me.epower.direct.entity.chargecloud.ServiceFeeParam;
import com.me.epower.direct.entity.downward.DelayFeePolicyQuery_1307;
import com.me.epower.direct.entity.downward.FixPolicyQuery_1301;
import com.me.epower.direct.entity.downward.PolicyQuery_1303;
import com.me.epower.direct.entity.downward.ServiceFeeQuery_1305;
import com.me.epower.direct.enums.StationClusterCmd;
import com.me.unicron.iot.bootstrap.ChannelService;
import com.me.unicron.iot.bootstrap.channel.StationManagementService;
import com.me.unicron.iot.gateway.EnvConst;
import com.me.unicron.iot.gateway.service.PolicyService;
import com.me.unicron.iot.gateway.service.util.PubChannelUtil;
import com.me.unicron.iot.gateway.worker.impl.CommandWorker;
import com.me.unicron.iot.kafka.KafkaProducer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PolicyServiceImpl implements PolicyService {

    @Autowired
    private StationManagementService stationManagementService;

    @Autowired
    KafkaProducer kafkaProducer;

    @Autowired
	CommandWorker commandWorker;
    
    private static final String POLICT_TYPE_SET = "1";

    private static final String POLICT_TYPE_QUERY = "0";

    /**
     * 后台下发固定电费计价策略设置
     */
    @Override
    public TResponse<String> setFixPolicy(FixPolicySetParam fixPolicySetParam) {
        fixPolicySetParam.setPolicyType(POLICT_TYPE_SET);
        return sendFixPolicyRequest(fixPolicySetParam);
    }

    /**
     * 后台下发固定电费计价策略查询
     */
    @Override
    public TResponse<String> queryFixPolicy(FixPolicySetParam fixPolicySetParam) {
        fixPolicySetParam.setPolicyType(POLICT_TYPE_QUERY);
        return sendFixPolicyRequest(fixPolicySetParam);
    }

    public TResponse<String> sendFixPolicyRequest(FixPolicySetParam fixPolicySetParam) {
        String equipmentId = fixPolicySetParam.getEquipmentId();
        /*
         * ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService); if (channelService == null) { return
         * TResponse.valueOf(TResponse.Status.GENERAL_ERROR); }
         */
        FixPolicyQuery_1301 fixPolicyQuery_1301 = new FixPolicyQuery_1301();
        fixPolicyQuery_1301.setPricePerKwh(fixPolicySetParam.getPricePerKwh());
        fixPolicyQuery_1301.setPolicyType(fixPolicySetParam.getPolicyType());
        /*
         * CMD_1301 cmd_1301 = new CMD_1301(); byte[] byte1301 = cmd_1301.getPayload(fixPolicyQuery_1301); try { String msg = new String(byte1301, CharsetDef.CHARSET); byte[] sendbytes =
         * msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8); log.info("发送固定价格命令:"); EncodeUtil.printHex(byte1301); channelService.push(equipmentId, logicServerQos.getQos(), sendbytes); } catch
         * (UnsupportedEncodingException e) { return TResponse.valueOf(TResponse.Status.GENERAL_ERROR); }
         */
        long timestamp = System.currentTimeMillis();
        fixPolicyQuery_1301.setTimestamp(timestamp);
        fixPolicyQuery_1301.setBaseEquipmentId(equipmentId);
        fixPolicyQuery_1301.setCmdNo(StationClusterCmd.RW_FIXED_POWER_TARIFF_POLICY);
        ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
		if (channelService != null) {
			log.info("本节点处理启动命令！");
			boolean result=commandWorker.doRWFixedPowerTariffPolicy(channelService, fixPolicyQuery_1301);
			if(!result){
				return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
			}
			
		} else {
			String msg = fixPolicyQuery_1301.toString();
	        kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
	        log.info("命令投递成功={}", msg);
		}
        
        return TResponse.valueOf(TResponse.Status.OK);
    }

    /**
     * 后台下发峰平谷电费计价策略设置
     */
    public TResponse<String> setPolicy(PolicySetParam policySetParam) {
        policySetParam.setPolicyType(POLICT_TYPE_SET);
        return sendPolicyRequest(policySetParam);
    }

    /**
     * 后台下发峰平谷电费计价策略设置
     */
    public TResponse<String> queryPolicy(PolicySetParam policySetParam) {
        policySetParam.setPolicyType(POLICT_TYPE_QUERY);
        return sendPolicyRequest(policySetParam);
    }

    public TResponse<String> sendPolicyRequest(PolicySetParam policySetParam) {
        String equipmentId = policySetParam.getEquipmentId();
        /*
         * ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService); if (channelService == null) { return
         * TResponse.valueOf(TResponse.Status.GENERAL_ERROR); }
         */
        PolicyQuery_1303 policyQuery_1303 = new PolicyQuery_1303();
        policyQuery_1303.setDurPrices(policySetParam.getDurPrices());
        policyQuery_1303.setPolicyType(policySetParam.getPolicyType());
        /*
         * CMD_1303 cmd_1303 = new CMD_1303(); byte[] byte1303 = cmd_1303.getPayload(policyQuery_1303); try { String msg = new String(byte1303, CharsetDef.CHARSET); byte[] sendbytes =
         * msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8); log.info("发送峰平谷价格命令:"); EncodeUtil.printHex(byte1303); channelService.push(equipmentId, logicServerQos.getQos(), sendbytes); } catch
         * (UnsupportedEncodingException e) { return TResponse.valueOf(TResponse.Status.GENERAL_ERROR); }
         */
        long timestamp = System.currentTimeMillis();
        policyQuery_1303.setTimestamp(timestamp);
        policyQuery_1303.setBaseEquipmentId(equipmentId);
        policyQuery_1303.setCmdNo(StationClusterCmd.RW_POWER_TARIFF_POLICY);
        ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
		if (channelService != null) {
			log.info("本节点处理启动命令！");
			boolean result=commandWorker.doRWPowerTariffPolicy(channelService, policyQuery_1303);
			if(!result){
				return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
			}
			
		} else {
			String msg = policyQuery_1303.toString();
	        kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
	        log.info("命令投递成功={}", msg);
		}
        
        return TResponse.valueOf(TResponse.Status.OK);
    }

    @Override
    public TResponse<String> setServiceFee(ServiceFeeParam serviceFeeParam) {
        serviceFeeParam.setPolicyType(POLICT_TYPE_SET);
        return sendServiceFeeRequest(serviceFeeParam);
    }

    @Override
    public TResponse<String> queryServiceFee(ServiceFeeParam serviceFeeParam) {
        serviceFeeParam.setPolicyType(POLICT_TYPE_QUERY);
        return sendServiceFeeRequest(serviceFeeParam);
    }

    public TResponse<String> sendServiceFeeRequest(ServiceFeeParam serviceFeeParam) {
        String equipmentId = serviceFeeParam.getEquipmentId();
        /*
         * ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService); if (channelService == null) { return
         * TResponse.valueOf(TResponse.Status.GENERAL_ERROR); }
         */
        ServiceFeeQuery_1305 serviceFeeQuery_1305 = new ServiceFeeQuery_1305();
        serviceFeeQuery_1305.setPolicyType(serviceFeeParam.getPolicyType());
        serviceFeeQuery_1305.setPortId(serviceFeeParam.getPortId());
        serviceFeeQuery_1305.setDurPrices(serviceFeeParam.getDurPrices());
        /*
         * CMD_1305 cmd_1305 = new CMD_1305(); byte[] byte1305 = cmd_1305.getPayload(serviceFeeQuery_1305); try { String msg = new String(byte1305, CharsetDef.CHARSET); byte[] sendbytes =
         * msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8); log.info("发送服务费价格命令:"); EncodeUtil.printHex(byte1305); channelService.push(equipmentId, logicServerQos.getQos(), sendbytes); } catch
         * (UnsupportedEncodingException e) { return TResponse.valueOf(TResponse.Status.GENERAL_ERROR); }
         */
        long timestamp = System.currentTimeMillis();
        serviceFeeQuery_1305.setTimestamp(timestamp);
        serviceFeeQuery_1305.setBaseEquipmentId(equipmentId);
        serviceFeeQuery_1305.setCmdNo(StationClusterCmd.RW_SERVICE_FEE_POLICY);
        ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
		if (channelService != null) {
			log.info("本节点处理启动命令！");
			boolean result=commandWorker.doRWServiceFeePolicy(channelService, serviceFeeQuery_1305);
			if(!result){
				return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
			}
			
		} else {
			String msg = serviceFeeQuery_1305.toString();

	        kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
	        log.info("命令投递成功={}", msg);
		}
        
        return TResponse.valueOf(TResponse.Status.OK);
    }

    @Override
    public TResponse<String> setDelayFee(DelayFeeSetParam delayFeeParam) {
        String equipmentId = delayFeeParam.getEquipmentId();
        DelayFeePolicyQuery_1307 delayFeePolicyQuery_1307 = new DelayFeePolicyQuery_1307();
        delayFeePolicyQuery_1307.setDurPrices(delayFeeParam.getDurPrices());
        delayFeePolicyQuery_1307.setPolicyType(POLICT_TYPE_SET);
        delayFeePolicyQuery_1307.setEquipmentId(delayFeeParam.getEquipmentId());
        delayFeePolicyQuery_1307.setBaseEquipmentId(equipmentId);
        delayFeePolicyQuery_1307.setCmdNo(StationClusterCmd.RW_DELAY_FEE_POLICY);
        long timestamp = System.currentTimeMillis();
        delayFeePolicyQuery_1307.setTimestamp(timestamp);
        ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
		if (channelService != null) {
			log.info("本节点处理启动命令！");
			boolean result=commandWorker.doRWDelayFeePolicy(channelService, delayFeePolicyQuery_1307);
			if(!result){
				return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
			}
			
		} else {
			String msg = delayFeePolicyQuery_1307.toString();
	        kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
	        log.info("设置延时费命令投递成功={}", msg);
		}
        
        return TResponse.valueOf(TResponse.Status.OK);

    }

    @Override
    public TResponse<String> queryDelayeFee(DelayFeeSetParam delayFeeParam) {
    		String equipmentId = delayFeeParam.getEquipmentId();
        DelayFeePolicyQuery_1307 delayFeePolicyQuery_1307 = new DelayFeePolicyQuery_1307();
        delayFeePolicyQuery_1307.setDurPrices(delayFeeParam.getDurPrices());
        delayFeePolicyQuery_1307.setPolicyType(POLICT_TYPE_QUERY);
        delayFeePolicyQuery_1307.setEquipmentId(delayFeeParam.getEquipmentId());
        delayFeePolicyQuery_1307.setCmdNo(StationClusterCmd.RW_DELAY_FEE_POLICY);
        delayFeePolicyQuery_1307.setBaseEquipmentId(delayFeeParam.getEquipmentId());
        long timestamp = System.currentTimeMillis();
        delayFeePolicyQuery_1307.setTimestamp(timestamp);
		
        ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
		if (channelService != null) {
			log.info("本节点处理启动命令！");
			boolean result=commandWorker.doRWDelayFeePolicy(channelService, delayFeePolicyQuery_1307);
			if(!result){
				return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
			}
			
		} else {
			String msg = delayFeePolicyQuery_1307.toString();
	        kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
	        log.info("查询延时费命令投递成功={}", msg);
			
		}
        
        return TResponse.valueOf(TResponse.Status.OK);
    }

}
