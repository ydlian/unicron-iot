package com.me.unicron.iot.gateway.service.impl;

import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.me.epower.component.ChargeConstant;
import com.me.epower.component.TRequest;
import com.me.epower.direct.entity.chargecloud.ConnectorStatusInfo;
import com.me.epower.direct.entity.chargecloud.NChargeOrderInfoParam;
import com.me.epower.direct.entity.chargecloud.NStartChargeResultParam;
import com.me.epower.direct.entity.constant.RedisConstant;
import com.me.epower.direct.entity.constant.UrlConstant;
import com.me.epower.direct.entity.upward.HistoryOrderInfoResponse_402;
import com.me.epower.direct.entity.upward.OrderInfoResponse_202;
import com.me.epower.direct.entity.upward.StationStatInfoResponse_104;
import com.me.epower.direct.enums.ChargeSeqStatEnum;
import com.me.unicron.HttpClientUtil;
import com.me.unicron.Enum.errorcode.ChargeStageEnum;
import com.me.unicron.constant.OperatorEnvConstant;
import com.me.unicron.date.DateUtils;
import com.me.unicron.helper.ErrcodeUtils;
import com.me.unicron.iot.gateway.service.IConnectorService;
import com.me.unicron.iot.gateway.service.util.GsonUtil;
import com.me.unicron.iot.gateway.service.util.PubNotifyTool;

import lombok.extern.slf4j.Slf4j;

/**
 * 回调
 * 
 * @author fzl
 *
 */
@Slf4j
@Component
public class NotifyServiceImpl {

    private static final String NOTIFYED = "notified";
    
    private static final String CANCEL_CHARGE = "3019";
    
    @Autowired
    private IConnectorService iConnectorService;

    public static String encodeNotifyData(String data,String operatorId) {
        String encodeData = PubNotifyTool.genDDEncodeData(OperatorEnvConstant.MEME_OPERETOR_ID, data);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStamp = dateFormat.format(System.currentTimeMillis());
        String sig = PubNotifyTool.genDDSig(OperatorEnvConstant.MEME_OPERETOR_ID, encodeData, timeStamp);
        TRequest<String> req = new TRequest<String>();
        req.setSig(sig);
        req.setSeq("0001");
        if(StringUtils.isNotBlank(operatorId)){
            req.setOperatorID(operatorId);
        }else{
            req.setOperatorID(OperatorEnvConstant.MEME_OPERETOR_ID);
        }
        req.setTimeStamp(timeStamp);
        req.setData(encodeData);
        return GsonUtil.getInstance().toJson(req);
    }

    /**
     * 根据8号命令启动回调
     * 
     * @param resp_8
     * @param connectorId
     * @param stringRedisTemplate
     */
    public void notifyStart(String query_result, String charge_seq, String connectorId, StringRedisTemplate stringRedisTemplate) {
        NStartChargeResultParam nStartChargeResultParam = new NStartChargeResultParam();
        log.info("notification_start_charge_result charge_seq:{},connectorId:{},query_result:{}", charge_seq, connectorId, query_result);
        
        if (StringUtils.isNotBlank(charge_seq) && ("0".equals(query_result))) {
            nStartChargeResultParam.setStartChargeSeq(charge_seq);
            nStartChargeResultParam.setStartChargeSeqStat(2);
            nStartChargeResultParam.setIdentCode("0");
        } else if (StringUtils.isNotBlank(charge_seq)) {
            if (CANCEL_CHARGE.equals(query_result)) {
                log.info("notify start result: cancel_charge(code {},already started but NOT connect charging hub.) charge_seq:{}", CANCEL_CHARGE, charge_seq);
            }
            nStartChargeResultParam.setStartChargeSeqStat(4);
            nStartChargeResultParam.setStartChargeSeq(charge_seq);
            String unicode=ErrcodeUtils.formatStringErrcode(ChargeStageEnum.STARTING_ASYN,query_result);
            nStartChargeResultParam.setIdentCode(unicode);
        } else {
            log.info("cant't find chargeseq,connectorId:{}", connectorId);
            return;
        }
        String operatorId = iConnectorService.getOperatorId(connectorId);
        nStartChargeResultParam.setOperatorID(operatorId);
        String startTime = stringRedisTemplate.opsForValue().get(RedisConstant.UNICRON_CHARGE_START + charge_seq);
        nStartChargeResultParam.setStartTime(startTime);
        nStartChargeResultParam.setConnectorID(connectorId);
        String data = NotifyServiceImpl.encodeNotifyData(GsonUtil.getInstance().toJson(nStartChargeResultParam),operatorId);
        log.info("send notification_start_charge_result:{}", data);
        String ret = HttpClientUtil.postJson(ChargeConstant.NOTIFY_DOMAIN + UrlConstant.N_START_CHARGE_RESULT, data, null);
        if (StringUtils.isNotBlank(ret)) {
            stringRedisTemplate.opsForValue().set(RedisConstant.UNICRON_IS_NOTIFY_START + charge_seq, NOTIFYED);
            stringRedisTemplate.opsForHash().delete(RedisConstant.UNICRON_CHARGE_START_TIME, charge_seq);
        }
    }

    public void handleNotifyStart(String charge_seq, String connectorId, StationStatInfoResponse_104 s104, StringRedisTemplate stringRedisTemplate) {
        log.info("enter handleNotifyStart charge_seq:{},connectorId:{},s104:{}", charge_seq, connectorId, JSONObject.toJSONString(s104));
        String work_stat = s104.getWork_stat();
        if (String.valueOf(ChargeSeqStatEnum.CHARGING.getStat()).equals(work_stat)) {
            String isNotify = stringRedisTemplate.opsForValue().get(RedisConstant.UNICRON_IS_NOTIFY_START + charge_seq);
            if (StringUtils.isBlank(isNotify)) {
                NStartChargeResultParam nStartChargeResultParam = new NStartChargeResultParam();
                nStartChargeResultParam.setConnectorID(connectorId);
                String operatorId = iConnectorService.getOperatorId(connectorId);
                nStartChargeResultParam.setOperatorID(operatorId);
                nStartChargeResultParam.setStartChargeSeq(charge_seq);
                nStartChargeResultParam.setStartChargeSeqStat(ChargeSeqStatEnum.CHARGING.getStat());
                String jsonData = GsonUtil.getInstance().toJson(nStartChargeResultParam);
                String startTime = stringRedisTemplate.opsForValue().get(RedisConstant.UNICRON_CHARGE_START + charge_seq);
                nStartChargeResultParam.setStartTime(startTime);
                String data = NotifyServiceImpl.encodeNotifyData(jsonData,operatorId);
                log.info("send nStart info :{}", data);
                String ret = HttpClientUtil.postJson(ChargeConstant.NOTIFY_DOMAIN + UrlConstant.N_START_CHARGE_RESULT, data, null);
                log.info("return nStart info :{}", ret);
                stringRedisTemplate.opsForValue().set(RedisConstant.UNICRON_IS_NOTIFY_START + charge_seq, NOTIFYED);
                stringRedisTemplate.opsForHash().delete(RedisConstant.UNICRON_CHARGE_START_TIME, charge_seq);
            }
        }
    }

    /**
     * 回传枪状态信息
     * 
     * @param connectorId
     * @param status
     */
    public void notifyConnectorStatus(String connectorId, String status) {
        ConnectorStatusInfo connectorStatusInfo = new ConnectorStatusInfo();
        connectorStatusInfo.setFaultStatus(0);//
        connectorStatusInfo.setConnectorID(connectorId);
        if (StringUtils.isNotBlank(status)) {
            connectorStatusInfo.setStatus(Integer.valueOf(status));
        }
        String operatorId = iConnectorService.getOperatorId(connectorId);
        String jsonData = GsonUtil.getInstance().toJson(connectorStatusInfo);
        log.info("send notifyConnectorStatus data encode:{}", jsonData);
        String data = NotifyServiceImpl.encodeNotifyData(jsonData,operatorId);
        log.info("send notifyConnectorStatus data:{}", data);
        String ret = HttpClientUtil.postJson(ChargeConstant.NOTIFY_DOMAIN + UrlConstant.N_STATION_STATUS, data, null);
        log.info("return notifyConnectorStatus :{}", ret);
    }
    
    /**
     * 回传枪故障信息
     * 
     * @param connectorId
     * @param status
     */
    public void notifyConnectorFaultStatus(String connectorId, ConnectorStatusInfo connectorStatusInfo) {
        
        connectorStatusInfo.setConnectorID(connectorId);
        String operatorId = iConnectorService.getOperatorId(connectorId);
        String jsonData = GsonUtil.getInstance().toJson(connectorStatusInfo);
        log.info("send notifyConnectorFaultStatus data encode:{}", jsonData);
        String data = NotifyServiceImpl.encodeNotifyData(jsonData,operatorId);
        log.info("send notifyConnectorFaultStatus data:{}", data);
        String ret = HttpClientUtil.postJson(ChargeConstant.NOTIFY_DOMAIN + UrlConstant.N_STATION_STATUS, data, null);
        log.info("return notifyConnectorFaultStatus :{}", ret);
    }

    /**
     * 回传枪状态信息
     */
    public void notifyChargeOrder(OrderInfoResponse_202 order202) {
        try {
            NChargeOrderInfoParam nChargeOrderInfoParam = new NChargeOrderInfoParam();
            nChargeOrderInfoParam.setOperatorID(OperatorEnvConstant.MEME_OPERETOR_ID);
            String startTime = order202.getStartTime();
            nChargeOrderInfoParam.setStartTime(DateUtils.getFormatString(startTime));
            String endTime = order202.getEndTime();
            nChargeOrderInfoParam.setEndTime(DateUtils.getFormatString(endTime));
            String connectorID = order202.getEquipmentId() + "0" + order202.getPortId();
            nChargeOrderInfoParam.setConnectorID(connectorID);
            if (StringUtils.isBlank(order202.getStopReason())
            		|| "0".equals(order202.getStopReason()) ) {
                nChargeOrderInfoParam.setStopReason("0");
            } else {
            	String unicode=ErrcodeUtils.formatStringErrcode(ChargeStageEnum.ORDER_CALLBACK, order202.getStopReason());
                nChargeOrderInfoParam.setStopReason(unicode);
            }
            Float totalPower = Float.parseFloat(order202.getTotalPower()) / 100;
            if (totalPower >= 0) {
                nChargeOrderInfoParam.setTotalPower(totalPower);
                float totalMoney = Float.parseFloat(order202.getTotalMoney());
                nChargeOrderInfoParam.setTotalMoney(totalMoney);
                float serviceMoney = Float.parseFloat(order202.getServiceMoney());
                nChargeOrderInfoParam.setTotalSeviceMoney(serviceMoney);
                nChargeOrderInfoParam.setTotalElecMoney(totalMoney - serviceMoney);
                nChargeOrderInfoParam.setStartChargeSeq(order202.getCardNum());
                String jsonData = GsonUtil.getInstance().toJson(nChargeOrderInfoParam);
                String operatorId = iConnectorService.getOperatorId(connectorID);
                String data = NotifyServiceImpl.encodeNotifyData(jsonData,operatorId);
                log.info("send notifyChargeOrder data:{}", data);
                String ret = HttpClientUtil.postJson(ChargeConstant.NOTIFY_DOMAIN + UrlConstant.N_CHARGE_ORDER, data, null);
                log.info("return notifyChargeOrder :{}", ret);
            }
        } catch (Exception e) {
            log.error("handleChargeOrderResponse", e);
        }
    }

    public void notifyHistoryChargeOrder(HistoryOrderInfoResponse_402 order202) {
        try {
            NChargeOrderInfoParam nChargeOrderInfoParam = new NChargeOrderInfoParam();
            String startTime = order202.getStartTime();
            nChargeOrderInfoParam.setStartTime(DateUtils.getFormatString(startTime));
            String endTime = order202.getEndTime();
            nChargeOrderInfoParam.setEndTime(DateUtils.getFormatString(endTime));
            String connectorID = order202.getEquipmentId() + "0" + order202.getPortId();
            nChargeOrderInfoParam.setConnectorID(connectorID);
            if (StringUtils.isBlank(order202.getStopReason())) {
                nChargeOrderInfoParam.setStopReason("0");
            } else {
                nChargeOrderInfoParam.setStopReason(order202.getStopReason());
            }
            Float totalPower = Float.parseFloat(order202.getTotalPower()) / 100;
            if (totalPower >= 0) {
                nChargeOrderInfoParam.setTotalPower(totalPower);
                float totalMoney = Float.parseFloat(order202.getTotalMoney());
                nChargeOrderInfoParam.setTotalMoney(totalMoney);
                float serviceMoney = Float.parseFloat(order202.getServiceMoney());
                nChargeOrderInfoParam.setTotalSeviceMoney(serviceMoney);
                nChargeOrderInfoParam.setTotalElecMoney(totalMoney - serviceMoney);
                nChargeOrderInfoParam.setStartChargeSeq(order202.getCardNum());
                String jsonData = GsonUtil.getInstance().toJson(nChargeOrderInfoParam);
                String operatorId = iConnectorService.getOperatorId(connectorID);
                nChargeOrderInfoParam.setOperatorID(operatorId);
                String data = NotifyServiceImpl.encodeNotifyData(jsonData,operatorId);                
                log.info("send order info domain :{} data:{}", ChargeConstant.NOTIFY_DOMAIN + UrlConstant.N_CHARGE_ORDER, data);
                String ret = HttpClientUtil.postJson(ChargeConstant.NOTIFY_DOMAIN + UrlConstant.N_CHARGE_ORDER, data, null);
                log.info("return order info :{}", ret);
            }
        } catch (Exception e) {
            log.error("handleChargeOrderResponse", e);
        }
    }

}
