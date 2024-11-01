package com.me.unicron.iot.gateway.service.impl;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.me.epower.component.ChargeConstant;
import com.me.epower.component.TResponse;
import com.me.epower.direct.entity.chargecloud.ChargeOrderBMSHighTempInfo;
import com.me.epower.direct.entity.chargecloud.ConnectorAlarm;
import com.me.epower.direct.entity.chargecloud.ConnectorFaultInfo;
import com.me.epower.direct.entity.chargecloud.ConnectorStatusInfo;
import com.me.epower.direct.entity.chargecloud.QueryStopChargeParam;
import com.me.epower.direct.entity.chargecloud.monitor.HeartBeatRecord;
import com.me.epower.direct.entity.chargecloud.monitor.SignupRecord;
import com.me.epower.direct.entity.chargecloud.monitor.SystemStopChargeRecord;
import com.me.epower.direct.entity.chargecloud.monitor.UserChargeOperation;
import com.me.epower.direct.entity.constant.RedisConstant;
import com.me.epower.direct.entity.downward.BMSRequest_302;
import com.me.epower.direct.entity.downward.FileVersion_1103;
import com.me.epower.direct.entity.downward.HistoryOrderInfoQuery_401;
import com.me.epower.direct.entity.downward.OrderInfoQuery_201;
import com.me.epower.direct.entity.downward.StationEventReportResponse_107;
import com.me.epower.direct.entity.downward.StationFaultReportResponse_117;
import com.me.epower.direct.entity.downward.StationInfoQueryResponse_103;
import com.me.epower.direct.entity.downward.StationStringPara_3;
import com.me.epower.direct.entity.downward.UpdateProcess_1105;
import com.me.epower.direct.entity.upward.AuthResponse_802;
import com.me.epower.direct.entity.upward.BMSInfoResponse_301;
import com.me.epower.direct.entity.upward.BeatHeartResponse_102;
import com.me.epower.direct.entity.upward.CleanResponse_20;
import com.me.epower.direct.entity.upward.ControlCommandResponse_6;
import com.me.epower.direct.entity.upward.DelayFee_1308;
import com.me.epower.direct.entity.upward.FileVersion_1104;
import com.me.epower.direct.entity.upward.HistoryOrderInfoResponse_402;
import com.me.epower.direct.entity.upward.HttpUpdate_1102;
import com.me.epower.direct.entity.upward.IntParaSetResponse_2;
import com.me.epower.direct.entity.upward.LockResponse_24;
import com.me.epower.direct.entity.upward.LogReportResponse_408;
import com.me.epower.direct.entity.upward.OrderInfoResponse_202;
import com.me.epower.direct.entity.upward.ServiceFee_1306;
import com.me.epower.direct.entity.upward.SignupResponse_106;
import com.me.epower.direct.entity.upward.StartChargeResponse_8;
import com.me.epower.direct.entity.upward.StationChargeUnitQueryResponse_14;
import com.me.epower.direct.entity.upward.StationEventReport_108;
import com.me.epower.direct.entity.upward.StationFaultReport_118_ext;
import com.me.epower.direct.entity.upward.StationPowerDispatchResult_16;
import com.me.epower.direct.entity.upward.StationPowerStategyDispatchQueryResponse_18;
import com.me.epower.direct.entity.upward.StationStatInfoResponse_104;
import com.me.epower.direct.entity.upward.StopChargeCommandResponse_12;
import com.me.epower.direct.entity.upward.StringhParaSetResponse_4;
import com.me.epower.direct.entity.upward.UpdateProcess_1106;
import com.me.epower.direct.enums.ConnectorStatusEnum;
import com.me.epower.direct.enums.ConnectorWorkStatusEnum;
import com.me.epower.direct.enums.StationClusterCmd;
import com.me.epower.direct.repositories.chargecloud.monitor.HeartBeatRecordRepository;
import com.me.epower.direct.repositories.chargecloud.monitor.SignupRecordRepository;
import com.me.epower.direct.repositories.chargecloud.monitor.SystemStopChargeRecordRepository;
import com.me.epower.direct.repositories.chargecloud.monitor.UserChargeOperationRepository;
import com.me.epower.entity.ChargeElectricalInfo;
import com.me.epower.entity.ChargeResultInfo;
import com.me.epower.repositories.ChargeElectricalRepository;
import com.me.epower.repositories.ChargeErrorCodeRepository;
import com.me.epower.repositories.ChargeOrderBMSHighTempInfoRepository;
import com.me.epower.repositories.ChargeResultInfoRepository;
import com.me.epower.repositories.ConnectorAlarmRepository;
import com.me.epower.repositories.ConnectorFaultInfoRepository;
import com.me.unicron.EncodeUtil;
import com.me.unicron.RSACoder;
import com.me.unicron.Enum.errorcode.ChargeCloudErrCode;
import com.me.unicron.Enum.errorcode.ChargeStageEnum;
import com.me.unicron.Enum.errorcode.StationFaultCode;
import com.me.unicron.connector.ConnectorUtils;
import com.me.unicron.date.DateUtils;
import com.me.unicron.helper.ErrcodeUtils;
import com.me.unicron.iot.bean.ClientConnectionInfo;
import com.me.unicron.iot.bootstrap.ChannelService;
import com.me.unicron.iot.bootstrap.channel.StationManagementService;
import com.me.unicron.iot.gateway.service.IConnectorService;
import com.me.unicron.iot.gateway.service.OperatorCharge;
import com.me.unicron.iot.gateway.service.ResponseHandleService;
import com.me.unicron.iot.gateway.service.util.GsonUtil;
import com.me.unicron.iot.gateway.service.util.PubChannelUtil;
import com.me.unicron.iot.ip.IpUtils;
import com.me.unicron.iot.logicserver.LogicServerQos;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.impl.CMD_101;
import com.me.unicron.iot.message.packer.impl.CMD_103;
import com.me.unicron.iot.message.packer.impl.CMD_105;
import com.me.unicron.iot.message.packer.impl.CMD_107;
import com.me.unicron.iot.message.packer.impl.CMD_1103;
import com.me.unicron.iot.message.packer.impl.CMD_1105;
import com.me.unicron.iot.message.packer.impl.CMD_117;
import com.me.unicron.iot.message.packer.impl.CMD_201;
import com.me.unicron.iot.message.packer.impl.CMD_3;
import com.me.unicron.iot.message.packer.impl.CMD_301;
import com.me.unicron.iot.message.packer.impl.CMD_401;
import com.me.unicron.iot.message.packer.impl.CMD_407;
import com.me.unicron.iot.mqtt.ClientConnectionService;
import com.me.unicron.iot.serverQos.CurrentQos;
import com.me.unicron.protocol.CharsetDef;
import com.me.unicron.station.service.ISecurityService;
import com.me.unicron.station.service.IStationService;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ResponseHandleServiceImpl implements ResponseHandleService {
	
	@Autowired
	private OperatorCharge operatorCharge;
	@Autowired
	private SystemStopChargeRecordRepository systemStopChargeRecordRepository;
    @Autowired
    private ClientConnectionService clientConnectionService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private LogicServerQos logicServerQos;
    @Autowired
    private StationManagementService stationManagementService;
    @Autowired
    private IStationService stationService;
    @Autowired
    private IConnectorService iConnectorService;
    @Autowired
    private UserChargeOperationRepository userChargeOperationRepository;
    @Autowired
    private ConnectorFaultInfoRepository connectorFaultInfoRepository;
    @Autowired
    ConnectorAlarmRepository connectorAlarmRepository;
    
    @Autowired
    private ChargeOrderBMSHighTempInfoRepository chargeOrderBMSHighTempInfoRepository;
    
    @Autowired
    private IStationService iStationService;

    @Autowired
    ISecurityService iSecurityService;
    @Autowired
    private HeartBeatRecordRepository heartBeatRecordRepository;

    @Autowired
    SignupRecordRepository signupRecordRepository;
    @Autowired
    private NotifyServiceImpl notifyService;

    @Autowired
    ChargeElectricalRepository chargeElectricalRepository;
    
    @Autowired
    ChargeResultInfoRepository chargeResultInfoRepository;

    @Autowired
    ChargeErrorCodeRepository chargeErrorCodeRepository;

    private static final float CHECK_SOC=98.9999f;
    private static final float CHECK_CURRENT=15.0f;
    private static final long CONTINUE_SECONDS=15*60;
    
    private String[] eleFee = { "37", "37", "37", "37", "37", "37", "37", "37", "37", "37", "37", "37", "37", "37", "86", "86", "86", "86", "86", "86", "138", "138", "151", "151", "151", "151", "138",
            "138", "138", "138", "86", "86", "151", "151", "86", "86", "151", "151", "151", "151", "151", "151", "86", "86", "86", "86", "37", "37" };

    private String[] serviceFee = { "62", "62", "62", "62", "62", "62", "62", "62", "62", "62", "62", "62", "62", "62", "63", "63", "63", "63", "63", "63", "41", "41", "28", "28", "28", "28", "41",
            "41", "41", "41", "63", "63", "0", "0", "63", "63", "41", "41", "41", "41", "41", "41", "63", "63", "63", "63", "62", "62" };

    private long checkTimeOffset(SignupResponse_106 resp) {
        String connDeviceId = resp.getStationId();
        ClientConnectionInfo client = clientConnectionService.getThisNodeConnectInfo(connDeviceId);
        long msecondOffset = 0;
        if (client != null) {
            client.setStationSoftwareVersion(resp.getStation_ver());
            //20180717160700
            String clocalTime = resp.getStation_local_time();
            if (!StringUtils.isBlank(clocalTime) && clocalTime.length() == "yyyyMMddhhmmss".length() && !clocalTime.equals("00000000000000")) {
                //服务器时间
                Date startDate = DateUtils.getCurDateTime();
                //桩时间
                Date endDate = DateUtils.StringToDate(resp.getStation_local_time(), "yyyyMMddhhmmss");
                if (endDate != null) {
                    //提供给桩做时间修正：毫秒级偏差=桩时间 - 服务器时间
                    msecondOffset = DateUtils.getIntervalMicroSecond(startDate, endDate);
                    log.info("桩时间 - 服务器时间，毫秒级偏移量：msecondOffset={},bytes={}", msecondOffset, EncodeUtil.intToByte((int) (msecondOffset / 1.0)));
                    client.setMsecondOffset(msecondOffset);
                }

            }

        }
        return msecondOffset;
    }

    /**
     * 服务器应答充电桩签到信息
     */
    public void handleSignup(SignupResponse_106 resp_106, MqttNetMsgBase base) {
        if (resp_106 == null)
            return;
        String json = JSONObject.toJSONString(resp_106);
        
        String equipmentId = resp_106.getStationId();
        equipmentId = EncodeUtil.cutCharsequence(equipmentId);
        log.info("收到签到信息：{},{}",equipmentId, json);
        SignupRecord signupRecord = new SignupRecord();
        signupRecord.setEquipment_id(equipmentId);
        signupRecord.setMsg(json);
        signupRecord.setSighup_time(DateUtils.getCurDateTime());
        signupRecordRepository.save(signupRecord);

        
        ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
        log.info("handleSignup获取channelService，equipmentId={},map={},channelService={}", equipmentId, 
        		JSONObject.toJSON(StationManagementService.getCacheMap()),channelService);
        if (channelService == null) {
            log.info("服务器应答充电桩签到信息空指针异常！{}",equipmentId);
            return;
        }
        long msecondOffset = checkTimeOffset(resp_106);
        //充电桩签到后，重置枪状态为空闲 1112 ???
        //stationService.updateEquipmentStatus(equipmentId, ConnectorStatusEnum.IDLE.getCode());
        CMD_105 cmd_105 = new CMD_105();

        base.setResponse(true);

        byte[] result = cmd_105.getPayload(msecondOffset, base);

        EncodeUtil.print(result);
        EncodeUtil.printHex(result);
        String msg = "";
        byte[] sendbytes = null;
        try {
            msg = new String(result, CharsetDef.CHARSET);
            sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
            log.info("发送签到信息回复:{},{}",equipmentId, EncodeUtil.printHex(result));
            channelService.push(equipmentId, logicServerQos.getQos(), sendbytes);
        } catch (Exception e) {
            log.error("handleSignup", e);
            e.printStackTrace();
        }
        
        try {
            CheckStionRTCTime(resp_106, base);
        } catch (Exception e) {
            // TODO: handle exception
            log.info(e.getMessage());
        }
        
    }

    void CheckStionRTCTime(SignupResponse_106 signupResponse, MqttNetMsgBase base) throws UnsupportedEncodingException {
        if (signupResponse == null || base == null || signupResponse.getStation_local_time() == null) {
            return;
        }

        Date stationTime = DateUtils.StringToDate(signupResponse.getStation_local_time(), "yyyyMMddhhmmss");
        Date serverTime=new Date();
        long time_span = 0;
        if(stationTime == null){
        	stationTime=DateUtils.DEFAULT_DATE;
        }else{
        	time_span = DateUtils.getIntervalSecond(stationTime, serverTime);
        }
        if (Math.abs(time_span) > 30 ) {
            log.info("traceid={},stationTime={},serverTime={},time Interval={}s", 
            		signupResponse.getStationId(),
            		DateUtils.Date2yyyyMMddHHmmss(stationTime),
            		DateUtils.Date2yyyyMMddHHmmss(serverTime),
            		time_span);

            String now = DateUtils.DateToString(new Date(), "yyyyMMddHHmmss");
            if (StringUtils.isBlank(now)) {
                log.info("get time failed");
                return;
            }
            ChannelService channelService = PubChannelUtil.getChannelService(signupResponse.getStationId(), stationManagementService);
            if (channelService == null) {
                return;
            }
            StationStringPara_3 stationStringPara_3 = new StationStringPara_3();
            stationStringPara_3.setEquipmentId(signupResponse.getStationId());
            stationStringPara_3.setData_body(now);
            stationStringPara_3.setStart_addr("2");
            stationStringPara_3.setCmd_type("1");
            CMD_3 cmd_3 = new CMD_3();
            byte[] byte3 = cmd_3.getPayload(stationStringPara_3, base);
            String msg = new String(byte3, CharsetDef.CHARSET);
            byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
            log.info("发送时间同步报文,编码前:byte3={}", EncodeUtil.printHex(byte3));
            log.info("发送时间同步报文,编码后,实际发送:sendbytes={}", EncodeUtil.printHex(sendbytes));
            channelService.push(stationStringPara_3.getEquipmentId(), CurrentQos.QoS, sendbytes);
        }else{
        	log.info("时间已经同步，{}",signupResponse.getStationId());
        }
    }

    /**
     * 处理心跳上传
     */
    @Override
    public void handleBeatHeartResponse(Channel channel, BeatHeartResponse_102 beatHeartResponse_102, MqttNetMsgBase base) {
        if (beatHeartResponse_102 == null)
            return;
        String json = JSONObject.toJSONString(beatHeartResponse_102);

        String equipmentId = beatHeartResponse_102.getStationId();
        log.info("traceid={}||收到客户端心跳信息,msg={}", equipmentId, json);
        stringRedisTemplate.opsForValue().set(RedisConstant.UNICRON_EQUIPMENT_STATUS + equipmentId, String.valueOf(new Date().getTime()));
//        HeartBeatRecord heartBeatRecord = new HeartBeatRecord();
//        heartBeatRecord.setBeat_time(DateUtils.getCurDateTime());
//        heartBeatRecord.setEquipment_id(equipmentId);
//        heartBeatRecord.setMsg("102");
//        heartBeatRecord.setNodeserver(IpUtils.getHost());
//        heartBeatRecord.setClientip(channel.remoteAddress().toString());
//        //heartBeatRecordRepository.save(heartBeatRecord);
//        iStationService.saveHeartBeatData(heartBeatRecord);

        CMD_101 cmd_101 = new CMD_101();
        ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
        if (channelService == null) {
            return;
        }

        base.setResponse(true);
        byte[] result = cmd_101.getPayload("", base);

        log.info("traceid={}||服务器发送心跳回复,data={}", equipmentId, EncodeUtil.printHex(result));
        //clientConnectionService.printConnectionInfo();
        try {
            String msg = new String(result, CharsetDef.CHARSET);
            byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
            channelService.push(equipmentId, logicServerQos.getQos(), sendbytes);
            //维护心跳状态
            iStationService.updateStationConnectStatus(equipmentId, "1", 120);

        } catch (Exception e) {
            log.error("handleBeatHeartResponse", e);
        }
    }

    private boolean checkChargeCondition(String charge_seq,
    		String equipmentId,String portId,boolean resetContinueSeconds){
    	boolean result=false;
    	String key=RedisConstant.UNICRON_SYS_STOP_CHARGE_ORDER_KEY+charge_seq;
    	String value=stringRedisTemplate.opsForValue().get(key);
    	SystemStopChargeRecord para=null;
    	if(value==null){
    		para=new SystemStopChargeRecord();
    		para.setCharge_seq(charge_seq);
    		String connector_id=ConnectorUtils.getConnectorId(equipmentId, portId);
			para.setConnector_id(connector_id);
			para.setExe_status(0);
			para.setPurling_charge_continue_sec(0L);
			para.setStop_reason("");
			//第一次满足策略，记录涓流开始时间
			para.setUpdate_time_sec(System.currentTimeMillis()/1000);
			stringRedisTemplate.opsForValue().set(key, JSONObject.toJSONString(para),24,TimeUnit.HOURS);
			
			return false;
			
    	}else{
    		try{
    			para = JSONObject.parseObject(value, SystemStopChargeRecord.class);
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    		if(para!=null){
    			if(para.getExe_status()>0){
    				return false;
    			}
    			if(resetContinueSeconds){
    				//抖动，重置涓流开始时间
    				para.setPurling_charge_continue_sec(0L);
    				long nowSeconds=System.currentTimeMillis()/1000;
    				para.setUpdate_time_sec(nowSeconds);
    			}else{
    				//持续的涓流
    				long sec=para.getPurling_charge_continue_sec();
    				long lastUpdateTime=para.getUpdate_time_sec();
    				long nowSeconds=System.currentTimeMillis()/1000;
    				sec=Math.abs(nowSeconds-lastUpdateTime);
    				para.setPurling_charge_continue_sec(sec);
    				//para.setUpdate_time_sec(nowSeconds);
    				if(sec>=CONTINUE_SECONDS){
    					log.info("涓流充电时间超长，{},charge_seq={}",JSONObject.toJSONString(para),charge_seq);
    					result=true;
    				}
    				
    			}
    			log.info("handleChargeStatusUpload->checkChargeCondition:charge_seq={},Purling_charge_continue_sec={},para={}",
    					charge_seq,para.getPurling_charge_continue_sec(),JSONObject.toJSONString(para));
    			stringRedisTemplate.opsForValue().set(key, JSONObject.toJSONString(para),24,TimeUnit.HOURS);
    		}
    	}
		return result;
    }
    private boolean systemTryStopChargeOrder(String charge_seq,String equipmentId,String portId){
    	boolean result=true;
    	//String connectorId = ConnectorUtils.getConnectorId(equipmentId, portId);
    	String key=RedisConstant.UNICRON_SYS_STOP_CHARGE_ORDER_KEY+charge_seq;
    	String value=stringRedisTemplate.opsForValue().get(key);
    	
    	SystemStopChargeRecord para=new SystemStopChargeRecord();
    	if(value!=null){
    		try{
    			para = JSONObject.parseObject(value, SystemStopChargeRecord.class);
    		}catch(Exception e){
    			
    		}
    		if(para!=null){
    			if(para.getExe_status()>0){
    				return false;
    			}
    		}
    	}else{
    		return false;
    	}
    	
    	QueryStopChargeParam queryStopChargeParam=new QueryStopChargeParam();
    	queryStopChargeParam.setStartChargeSeq(charge_seq);
    	queryStopChargeParam.setBaseEquipmentId(equipmentId);
    	StationClusterCmd cmdNo=StationClusterCmd.STOP_CHARGE;
		queryStopChargeParam.setCmdNo(cmdNo);
		queryStopChargeParam.setPortId(portId);
		queryStopChargeParam.setTimestamp(System.currentTimeMillis());
		queryStopChargeParam.setCallerName("SYS");
		
    	TResponse<String> resp= operatorCharge.queryStopCharge(queryStopChargeParam);
    	log.info("系统判定充满，终止订单：traceid={},para={},queryStopChargeParam={}",equipmentId,JSONObject.toJSONString(para),queryStopChargeParam);
    	if(para==null){
    		para=new SystemStopChargeRecord(); 
    	}
    	para.setCharge_seq(charge_seq);
    	para.setExe_status(1);
    	String connector_id=ConnectorUtils.getConnectorId(equipmentId, portId);
		para.setConnector_id(connector_id);
    	String stopReason= ChargeCloudErrCode.SYS_JUDGE_CHARGE_FULL_STOP_ORDER.getValue()+"";//int
		para.setStop_reason(stopReason);
		para.setCreate_time(new Date());
		String stop_condition="SOC>="+CHECK_SOC+",CURRENT<"+CHECK_CURRENT+",CONTINUE_SECONDS="+CONTINUE_SECONDS;
		para.setStop_condition(stop_condition);
		para.setUpdate_time_sec(System.currentTimeMillis()/1000);
		para.setCharge_e_time(DateUtils.getCurrentDateyyyyMMddHHmmss());
    	stringRedisTemplate.opsForValue().set(key, JSONObject.toJSONString(para),24,TimeUnit.HOURS);
    	//存记录
    	try{
    		systemStopChargeRecordRepository.save(para);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	
    	return result;
    }
    /**
     * 将上传的充电状态信息保存到缓存中
     */
    public void handleChargeStatusUpload(StationStatInfoResponse_104 stationStatInfoResponse_104, MqttNetMsgBase base) {
        if (stationStatInfoResponse_104 == null)
            return;
        String json = JSONObject.toJSONString(stationStatInfoResponse_104);
        
        String equipmentId = stationStatInfoResponse_104.getEquipmentId();
        String charge_index_no = stationStatInfoResponse_104.getCharge_index_no();
        //枪号
        String connectorId = ConnectorUtils.getConnectorId(equipmentId, charge_index_no);
        //账单号
        String charge_seq = stationStatInfoResponse_104.getCharge_user_id();
        log.info("收到充电状态信息：{},时间戳:{},charge_seq={}", json, System.currentTimeMillis(),charge_seq);
        handleChargeStatus(connectorId, charge_seq, json, stationStatInfoResponse_104, base);
        
        boolean resetContinueSeconds=false;
        float current=Float.valueOf(stationStatInfoResponse_104.getDc_charge_current()) / 10.0f;
        float soc=Float.valueOf(stationStatInfoResponse_104.getSoc_percent());
        boolean condition=false;
        log.info("handleChargeStatusUpload:soc={},current={},charge_seq={}",soc,current,charge_seq);
        if(soc>=CHECK_SOC ){
        	if(current<=CHECK_CURRENT){
        		resetContinueSeconds=false;
        		condition=checkChargeCondition(charge_seq, equipmentId, charge_index_no,resetContinueSeconds);
        	}else{
        		//噪点或数据上报不准，或车辆需求又上升了
        		resetContinueSeconds=true;
        		condition=checkChargeCondition(charge_seq, equipmentId, charge_index_no,resetContinueSeconds);
        	}
        	
        }
        if(condition){
			systemTryStopChargeOrder(charge_seq, equipmentId, charge_index_no);
        }
       
        CMD_103 cmd_103 = new CMD_103();
        ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
        //
        if (channelService == null) {
            return;
        }
        
        StationInfoQueryResponse_103 stationInfoQueryResponse_103 = new StationInfoQueryResponse_103();
        stationInfoQueryResponse_103.setStationId(equipmentId);
        stationInfoQueryResponse_103.setGun_no(charge_index_no);
        stationInfoQueryResponse_103.setCard_no_user_id(charge_seq);
        stationInfoQueryResponse_103.setCard_balance("0");
        stationInfoQueryResponse_103.setBalance_sufficient("1");

        base.setResponse(true);
        byte[] result = cmd_103.getPayload(stationInfoQueryResponse_103, base);
        try {
            String msg = new String(result, CharsetDef.CHARSET);
            byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
            log.info("发送充电状态信息回复:{}", equipmentId);
            EncodeUtil.printHex(result);
            channelService.push(equipmentId, logicServerQos.getQos(), sendbytes);
        } catch (UnsupportedEncodingException e) {
            log.error("handleChargeStatusUpload={}", e);
        }

    }

    /**
     * 将上传的充电状态信息保存到缓存中，处理116回复的桩状态信息
     */
    @Override
    public void handleQueryChargeStatusUpload(StationStatInfoResponse_104 stationStatInfoResponse_104, MqttNetMsgBase base) {
        if (stationStatInfoResponse_104 == null)
            return;
        String json = JSONObject.toJSONString(stationStatInfoResponse_104);
        log.info("收到查询充电状态信息：{},时间戳:{}", json, System.currentTimeMillis());
        String equipmentId = stationStatInfoResponse_104.getEquipmentId();
        String portId = stationStatInfoResponse_104.getCharge_index_no();
        //枪号
        String connectorId = stationService.getConnectorId(equipmentId, portId);
        //账单号
        String charge_seq = stationStatInfoResponse_104.getCharge_user_id();
        handleChargeStatus(connectorId, charge_seq, json, stationStatInfoResponse_104, base);

    }

    /**
     * 处理104充电状态报文
     */
    public void handleChargeStatus(String connectorId, String charge_seq, String json, StationStatInfoResponse_104 s104, MqttNetMsgBase base) {
        if (s104 == null)
            return;
        log.info("handleChargeStatus:{},connectorId:{}", charge_seq, connectorId);
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        //当前充电的枪json
        valueOperations.set(RedisConstant.UNICRON_CHARGE_STATUS + connectorId, json);
        //当前充电的账单json
        //boolean bUpdateValue=false;
        //String lastVal=valueOperations.get(RedisConstant.UNICRON_CHARGE_STATUS + charge_seq);
        long newKwh=0;
        try{
        	newKwh=Long.parseLong(s104.getCum_charge_kwh_amount().trim());
        }catch(Exception e){
        	e.printStackTrace();
        }
        if(newKwh>0){
        	valueOperations.set(RedisConstant.UNICRON_CHARGE_STATUS + charge_seq, json);
        	//启动回调
            notifyService.handleNotifyStart(charge_seq, connectorId, s104, stringRedisTemplate);
            //更新电量信息
            updateMeter(connectorId, s104);
            handleChargeMessagePersist(s104);
        }
        //设置当前枪对应的账单
        valueOperations.set(RedisConstant.UNICRON_CONNECTTOR_REL + charge_seq, connectorId);
        
        //根据充电状态设定枪状态
        iConnectorService.handleConnectorStatus(connectorId, s104.getWork_stat());
        
        String connectorKey=RedisConstant.UNICRON_CHARGE_CLOUD_CONNECTOTR_EVENT_WORK_STATUS+connectorId;
        String workStatus=s104.getWork_stat();
        if(StringUtils.isBlank(workStatus)){
        	log.info("handleChargeStatus:充电枪104数据错误，枪异常：s104={},traceid={}",s104,connectorId);
        	stringRedisTemplate.opsForValue().set(connectorKey, ConnectorWorkStatusEnum.DAMAGE.getCode());
        }else{
        	workStatus=workStatus.trim();
        	if("6".equals(workStatus)){
        		log.info("handleChargeStatus:充电枪损坏：s104={},traceid={}",s104,connectorId);
        		stringRedisTemplate.opsForValue().set(connectorKey, ConnectorWorkStatusEnum.DAMAGE.getCode());
        	}
        }
       
    }
    /**
     * 存储104报文中的充电信息
     * @param statInfoResponse_104
     */
    private void handleChargeMessagePersist(StationStatInfoResponse_104 statInfoResponse_104)
	{
		if (!"2".equals(statInfoResponse_104.getWork_stat())) {
			return;
		}

		ChargeElectricalInfo chargeElectricalInfo = new ChargeElectricalInfo();

		chargeElectricalInfo.setEquipmentid(statInfoResponse_104.getEquipmentId());
		chargeElectricalInfo.setConnectorid(statInfoResponse_104.getCharge_index_no());
		chargeElectricalInfo.setOrdernum(statInfoResponse_104.getCharge_user_id());
		chargeElectricalInfo.setBmschargetype(statInfoResponse_104.getBms_charge_mode());
		chargeElectricalInfo.setBmsneedcur(statInfoResponse_104.getBms_need_current());
		chargeElectricalInfo.setBmsneedvol(statInfoResponse_104.getBms_need_voltage());
		chargeElectricalInfo.setRelcur(statInfoResponse_104.getDc_charge_current());
		chargeElectricalInfo.setRelvol(statInfoResponse_104.getDc_charge_voltage());
		chargeElectricalInfo.setTime(DateUtils.getCurDateTime());
		chargeElectricalInfo.setSoc(statInfoResponse_104.getSoc_percent());

		chargeElectricalRepository.save(chargeElectricalInfo);
	}

    
    private void updateMeter(String connectorId, StationStatInfoResponse_104 s104) {
        try {
            if (StringUtils.isNotBlank(s104.getNow_meter_kwh_num())) {
                String now_meter_kwh_num = s104.getNow_meter_kwh_num();
                if (StringUtils.isNotBlank(now_meter_kwh_num)) {
                    Long longNum = Long.parseLong(now_meter_kwh_num);
                    if(longNum==0){
                        return;
                    }
                    //记录当前电表读数
                    String operatorId = iConnectorService.getOperatorId(connectorId);
                    HashOperations<String, String, String> hashOperations = stringRedisTemplate.opsForHash();
                    String beforeNum = hashOperations.get(RedisConstant.UNICRON_METER + operatorId, connectorId);
                    if(StringUtils.isBlank(beforeNum)){
                        hashOperations.put(RedisConstant.UNICRON_METER + operatorId, connectorId, longNum.toString());
                    }else{
                        Long beforeNumLong = Long.parseLong(beforeNum);
                        if (longNum != 0 && longNum > beforeNumLong) {
                            hashOperations.put(RedisConstant.UNICRON_METER + operatorId, connectorId, longNum.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.info("updateMeter exception", e);
        }
    }

    /**
     * 设置整型命令响应
     */
    public void handleIntParaSet(IntParaSetResponse_2 resp_2, MqttNetMsgBase base) {
        if (resp_2 == null)
            return;
        if ("0".equals(resp_2.getQuery_result())) {
            log.info("收到设置整型命令响应:{}", JSONObject.toJSONString(resp_2));
        } else {
            log.error("handleIntParaSet error:{}", JSONObject.toJSONString(resp_2));
        }
    }

    /**
     * 设置字符型命令响应
     */
    public void handleStringParaSet(StringhParaSetResponse_4 resp_4, MqttNetMsgBase base) {
        if (resp_4 == null)
            return;
        if ("0".equals(resp_4.getQuery_result())) {
            log.info("收到设置字符型命令响应:{}", JSONObject.toJSONString(resp_4));
        } else {
            log.error("handleIntParaSet error:{}", JSONObject.toJSONString(resp_4));
        }
    }

    
    private void saveStopChargeOperationInfo(String destEquipmentId,String portId,
    		String orderid,ChargeStageEnum statusEnum,String msg,String errorcode){
    	UserChargeOperation userChargeOperation=new UserChargeOperation();
     	userChargeOperation.setEquipmentid(destEquipmentId);
     	userChargeOperation.setLogicserver(IpUtils.getHost());
     	userChargeOperation.setOrderid(orderid);
     	try{
     		userChargeOperation.setPortid(Integer.parseInt(portId));
     	}catch(Exception e){
     		e.printStackTrace();
     	}
     	userChargeOperation.setErrorcode(errorcode);
     	userChargeOperation.setErrorcode(errorcode);
     	Date calltime=new Date();
 		userChargeOperation.setCalltime(calltime);
     	userChargeOperation.setOptype(statusEnum.getStage());
     	userChargeOperation.setCallstatus(statusEnum.getDesc());
     	userChargeOperation.setMsg(msg);
     	userChargeOperationRepository.save(userChargeOperation);
     	
    }
    
    
    /**
     * 设置控制命令响应,5号命令的响应
     */
    public void handleControlCmd(ControlCommandResponse_6 resp_6, MqttNetMsgBase base) {
        if (resp_6 == null)
            return;
        String destEquipmentId=resp_6.getStationId();
		String portId=resp_6.getGun_no();
        String connectorId = ConnectorUtils.getConnectorId(resp_6.getStationId(), resp_6.getGun_no());
        
        String orderid = stringRedisTemplate.opsForValue().get(RedisConstant.UNICRON_CONNECTTOR_CHARGESEQ_REL + connectorId);
       
        String errorcode=ErrcodeUtils.formatStringErrcode(ChargeStageEnum.STOPPING_ASYN, resp_6.getQuery_result());
        if ("0".equals(resp_6.getQuery_result())) {
            log.info("收到控制命令响应:{}", JSONObject.toJSONString(resp_6));
            ChargeStageEnum statusEnum=ChargeStageEnum.STOPPING_ASYN;
            //暂不存储
			//saveStopChargeOperationInfo(destEquipmentId, portId, orderid, statusEnum, "用户成功终止充电", errorcode);
            
        } else {
        	ChargeStageEnum statusEnum=ChargeStageEnum.STOPPING_ASYN;
			//saveStopChargeOperationInfo(destEquipmentId, portId, orderid, statusEnum, "用户终止充电异常", errorcode);
            log.error("handleIntParaSet error:{}", JSONObject.toJSONString(resp_6));
        }
    }

    private void saveStartStageConnectAlarm(String connectorid,String charge_seq,String orinCode){
    	ConnectorAlarm entity=new ConnectorAlarm();
    	entity.setConnectorid(connectorid);
		entity.setCreattime(DateUtils.getCurDateTime());
		entity.setEventdate(DateUtils.getCurrentDateyyyyMMddHHmmss());
		entity.setOrderid(charge_seq);
		entity.setStage(ChargeStageEnum.STARTING_ASYN.getStage());
		entity.setErrorcode(orinCode);
		entity.setLevel(1);
		connectorAlarmRepository.save(entity);
    }
    /**
     * 处理启动充电响应
     */
    @Override
    public void handleStartChargeResponse(StartChargeResponse_8 resp_8, MqttNetMsgBase base) {
        if (resp_8 == null)
            return;
        String gun_no = getGunNo(resp_8);
        String connectorId = stationService.getConnectorId(resp_8.getEquipmentId(), gun_no);
        //发送启动回调
        String charge_seq = stringRedisTemplate.opsForValue().get(RedisConstant.UNICRON_CONNECTTOR_CHARGESEQ_REL + connectorId);
        log.info("traceid={}||[桩端回应]收到启动充电响应:{},chargeorderid={},connectorId={}", resp_8.getEquipmentId(), JSONObject.toJSONString(resp_8), charge_seq, connectorId);
        //
        String destEquipmentId=resp_8.getEquipmentId();
        UserChargeOperation userChargeOperation=new UserChargeOperation();
		userChargeOperation.setEquipmentid(destEquipmentId);
    	userChargeOperation.setLogicserver(IpUtils.getHost());
    	userChargeOperation.setOrderid(charge_seq);
    	userChargeOperation.setPortid(Integer.parseInt(resp_8.getGun_no()));
    	Date calltime=new Date();
		userChargeOperation.setCalltime(calltime);
		String reserve1=resp_8.getReserved1();
		userChargeOperation.setReserve1(reserve1);
    	String reserve2=resp_8.getReserved2();
		userChargeOperation.setReserve2(reserve2);
		String orinCode=resp_8.getQuery_result();
		if(orinCode==null) orinCode="0";
		
		if("0".equals(orinCode)){
			userChargeOperation.setCallstatus(ChargeStageEnum.CHARGING.getDesc());
			userChargeOperation.setOptype(ChargeStageEnum.CHARGING.getStage());
			String errcode=ErrcodeUtils.formatStringErrcode(ChargeStageEnum.CHARGING, orinCode);
			userChargeOperation.setErrorcode(errcode);
			userChargeOperation.setMsg("充电中");
		}else{
			userChargeOperation.setOptype(ChargeStageEnum.STARTING_ASYN.getStage());
			userChargeOperation.setCallstatus(ChargeStageEnum.STARTING_ASYN.getDesc());
			if("1013".equals(orinCode.toUpperCase())){
				//启动时枪未正确连接
				ConnectorStatusEnum portStatus=iConnectorService.getConnectorCurrentStatus(
						ConnectorUtils.getConnectorId(resp_8.getEquipmentId(), gun_no));
				if(portStatus.getCode()==ConnectorStatusEnum.IDLE.getCode()
	            		){
	            	//枪口状态空闲,错误码修正为用户拔枪终止
	            	userChargeOperation.setSecondcode(ChargeCloudErrCode.BIZ_CUSTOM_UNPLUG_CHARGE_GUN_STOP.getName());
	            }else{
	            	//枪口非空闲,	充电枪有故障
	            	userChargeOperation.setSecondcode(ChargeCloudErrCode.BIZ_CONNECTOR_FAULT.getName());
	            }
				this.saveStartStageConnectAlarm(connectorId, charge_seq, orinCode);
	            
			}
			if( "2004".equals(orinCode.toUpperCase()) || "2003".equals(orinCode.toUpperCase())){
				
				Date endDate=DateUtils.addSecond(DateUtils.getCurDateTime(),2);
				Date startDate=DateUtils.addSecond(endDate, -80);
				//是否离线了
				List<HeartBeatRecord> list=heartBeatRecordRepository.findAllPongRecordsByEquipmentIdAndTime(destEquipmentId, startDate, endDate);
				if(list==null || list.isEmpty()){
					orinCode=ChargeCloudErrCode.CHARGE_STATION_DISCONNECT.getValue()+"";
					resp_8.setQuery_result(orinCode);
				}
				this.saveStartStageConnectAlarm(connectorId, charge_seq, orinCode);
				//
				
			}
			String errcode=ErrcodeUtils.formatStringErrcode(ChargeStageEnum.STARTING_ASYN,orinCode);
			userChargeOperation.setErrorcode(errcode);
			userChargeOperation.setMsg(resp_8.getReserved1());
		}
		ConnectorFaultInfo connectorFaultInfo=new ConnectorFaultInfo();
		connectorFaultInfo.setConnectorid(connectorId);
		connectorFaultInfo.setErrorcode(orinCode);
		connectorFaultInfo.setCreattime(DateUtils.getCurDateTime());
		connectorFaultInfo.setEventdate(DateUtils.getCurrentDateyyyyMMddHHmmss());
		double faultrate=calculateConnectorFaultRate(connectorId);
		connectorFaultInfo.setFaultrate(faultrate);
		connectorFaultInfo.setOrderid(charge_seq);
		connectorFaultInfo.setStage(ChargeStageEnum.STARTING_ASYN.getStage());
		connectorFaultInfo.setPhone("");
		connectorFaultInfoRepository.save(connectorFaultInfo);
    	userChargeOperationRepository.save(userChargeOperation);
    	saveStartChargeResult(resp_8);
        String isNotify = stringRedisTemplate.opsForValue().get(RedisConstant.UNICRON_IS_NOTIFY_START + charge_seq);
        if (StringUtils.isBlank(isNotify)) {
        	/*
        	if("2003".equals(resp_8.getQuery_result())){
        		OrderInfoResponse_202 resp_202=new OrderInfoResponse_202();
        		resp_202.setStartTime(DateUtils.getCurrentDateyyyyMMddHHmmss());
        		resp_202.setCardNum(charge_seq);
        		resp_202.setChargeTime("0");
        		resp_202.setEndTime(DateUtils.getCurrentDateyyyyMMddHHmmss());
        		resp_202.setEquipmentId(resp_8.getEquipmentId());
        		resp_202.setPortId(resp_8.getGun_no());
        		resp_202.setStopReason("0");
        		notifyService.notifyStart("0", charge_seq, connectorId, stringRedisTemplate);
    			handleAdjustChargeOrderResponse(resp_202, "0", "上报账单", base);
    			
        	}else{
        		notifyService.notifyStart(resp_8.getQuery_result(), charge_seq, connectorId, stringRedisTemplate);
        	}*/
        	
        	notifyService.notifyStart(resp_8.getQuery_result(), charge_seq, connectorId, stringRedisTemplate);
            
        }
    }

    private double calculateConnectorFaultRate(String connectorid) {
    	Date stopTime=DateUtils.getCurDateTime();
		Date startTime=DateUtils.addDay(stopTime, -1);
		
		// TODO Auto-generated method stub
    	List<ConnectorFaultInfo> list=connectorFaultInfoRepository.findByConnectoridAndCreatetime(connectorid, startTime, stopTime);
		if(list==null || list.isEmpty()) return 0;
		int size=list.size();
		int faultCnt=0;
		double rate=0.0f;
		for(ConnectorFaultInfo e:list){
			if("0".equals(e.getErrorcode()) 
					|| "0000".equals(e.getErrorcode())){
				
			}else{
				faultCnt++;
			}
		}
		rate=faultCnt/(double)size;
		return rate;
	}

	private String getGunNo(StartChargeResponse_8 resp_8) {
        return resp_8.getGun_no();
    }

    private void saveStartChargeResult(StartChargeResponse_8 startChargeResponse_8)
   	{
   		ChargeResultInfo chargeResultInfo = chargeResultInfoRepository.findChargeResultInfoByEquipmentidAndConnectorid(
   				startChargeResponse_8.getEquipmentId(), startChargeResponse_8.getGun_no());
   		if (chargeResultInfo == null) {
   			log.info("can not find chargeResultInfo message,equipmentId:{},gun_no:{}",
   					startChargeResponse_8.getEquipmentId(), startChargeResponse_8.getGun_no());
   			return;
   		}
   		chargeResultInfo.setIdentcode(startChargeResponse_8.getQuery_result());
   		String errCode = startChargeResponse_8.getQuery_result();

   		if (! "0".equals(errCode)) {
   			// 启动失败,写入结束时间
   			chargeResultInfo.setStoptime(DateUtils.getCurDateTime());
   		}
   		String errorDescribe = chargeErrorCodeRepository.findErrordescribeByErrorde(errCode);

   		if (StringUtils.isBlank(errorDescribe)) {
   			log.info("can not find identCodeDescribe message,errCode:{}", errCode);
   		} else {
   			chargeResultInfo.setIdentcodedescribe(errorDescribe);
   		}

   		chargeResultInfoRepository.save(chargeResultInfo);
   	}

    /**
     * 设置控制命令响应
     */
    public void handleCleanResponse(CleanResponse_20 cleanResponse_20, MqttNetMsgBase base) {
        if (cleanResponse_20 == null)
            return;
        if ("0".equals(cleanResponse_20.getQuery_result())) {
            log.info("收到清除命令响应:{}", JSONObject.toJSONString(cleanResponse_20));
        } else {
            log.error("handleIntParaSet error:{}", JSONObject.toJSONString(cleanResponse_20));
        }
    }

    /**
     * 设置电子锁命令响应
     */
    public void handleLockResponse(LockResponse_24 lockResponse_24, MqttNetMsgBase base) {
        if (lockResponse_24 == null)
            return;
        if ("0".equals(lockResponse_24.getResult())) {
            log.info("收到电子锁命令响应:{}", JSONObject.toJSONString(lockResponse_24));
        } else {
            log.error("handleIntParaSet error:{}", JSONObject.toJSONString(lockResponse_24));
        }
    }

    private void saveChargeCompleteInfo(String destEquipmentId,String portId,
    		String orderid,ChargeStageEnum statusEnum,String msg,OrderInfoResponse_202 orderInfoResponse_202){
    	String errorcode=ErrcodeUtils.formatStringErrcode(ChargeStageEnum.ORDER_CALLBACK, orderInfoResponse_202.getStopReason());
    	
    	UserChargeOperation userChargeOperation=new UserChargeOperation();
    	String reserve1=orderInfoResponse_202.getReserved1();
		userChargeOperation.setReserve1(reserve1);
		
    	String reserve2=orderInfoResponse_202.getReserved2();
		userChargeOperation.setReserve2(reserve2);
		
     	userChargeOperation.setEquipmentid(destEquipmentId);
     	userChargeOperation.setLogicserver(IpUtils.getHost());
     	userChargeOperation.setOrderid(orderid);
     	try{
     		userChargeOperation.setPortid(Integer.parseInt(portId));
     	}catch(Exception e){
     		e.printStackTrace();
     	}
     	String orinCode=orderInfoResponse_202.getStopReason();
     	if(orinCode==null){
     		orinCode="0";
     	}
     	
     	ConnectorFaultInfo connectorFaultInfo=new ConnectorFaultInfo();
		String connectorId =ConnectorUtils.getConnectorId(destEquipmentId, portId);
		connectorFaultInfo.setConnectorid(connectorId );
		connectorFaultInfo.setErrorcode(orinCode);
		connectorFaultInfo.setCreattime(DateUtils.getCurDateTime());
		connectorFaultInfo.setEventdate(DateUtils.getCurrentDateyyyyMMddHHmmss());
		double faultrate=calculateConnectorFaultRate(connectorId);
		connectorFaultInfo.setFaultrate(faultrate);
		connectorFaultInfo.setOrderid(orderid);
		connectorFaultInfo.setStage(ChargeStageEnum.ORDER_CALLBACK.getStage());
		connectorFaultInfo.setPhone("");
		connectorFaultInfoRepository.save(connectorFaultInfo);
		
		if("1013".equals(orinCode.toUpperCase())){
			//账单阶段是否是拔枪终止的
			ConnectorStatusEnum portStatus=iConnectorService.getConnectorCurrentStatus(
					ConnectorUtils.getConnectorId(destEquipmentId, portId));
            if(portStatus.getCode()==ConnectorStatusEnum.IDLE.getCode()
            		){
            	//枪口已经空闲,错误码修正为用户拔枪终止
            	userChargeOperation.setSecondcode(ChargeCloudErrCode.BIZ_CUSTOM_UNPLUG_CHARGE_GUN_STOP.getName());
            }else{
            	//枪口非空闲,	充电枪有故障
            	userChargeOperation.setSecondcode(ChargeCloudErrCode.BIZ_CONNECTOR_FAULT.getName());
            }	
            
		}
		
     	userChargeOperation.setErrorcode(errorcode);
     	Date calltime=new Date();
 		userChargeOperation.setCalltime(calltime);
     	userChargeOperation.setOptype(statusEnum.getStage());
     	userChargeOperation.setCallstatus(statusEnum.getDesc());
     	userChargeOperation.setMsg(msg);
     	userChargeOperationRepository.save(userChargeOperation);
     	
    }
    
    
    private void saveChargeCompleteInfoByHistoryOrder(String destEquipmentId,String portId,
    		String orderid,ChargeStageEnum statusEnum,String msg,HistoryOrderInfoResponse_402 historyOrderInfoResponse_402){
    	String errorcode=ErrcodeUtils.formatStringErrcode(ChargeStageEnum.ORDER_CALLBACK, historyOrderInfoResponse_402.getStopReason());
    	
    	UserChargeOperation userChargeOperation=new UserChargeOperation();
    	String reserve1=historyOrderInfoResponse_402.getReserved1();
		userChargeOperation.setReserve1(reserve1);
		
    	String reserve2=historyOrderInfoResponse_402.getReserved2();
		userChargeOperation.setReserve2(reserve2);
		
     	userChargeOperation.setEquipmentid(destEquipmentId);
     	userChargeOperation.setLogicserver(IpUtils.getHost());
     	userChargeOperation.setOrderid(orderid);
     	try{
     		userChargeOperation.setPortid(Integer.parseInt(portId));
     	}catch(Exception e){
     		e.printStackTrace();
     	}
     	String orinCode=historyOrderInfoResponse_402.getStopReason();
     	if(orinCode==null){
     		orinCode="0";
     	}
     	
     	ConnectorFaultInfo connectorFaultInfo=new ConnectorFaultInfo();
		String connectorId =ConnectorUtils.getConnectorId(destEquipmentId, portId);
		connectorFaultInfo.setConnectorid(connectorId );
		connectorFaultInfo.setErrorcode(orinCode);
		connectorFaultInfo.setCreattime(DateUtils.getCurDateTime());
		connectorFaultInfo.setEventdate(DateUtils.getCurrentDateyyyyMMddHHmmss());
		double faultrate=calculateConnectorFaultRate(connectorId);
		connectorFaultInfo.setFaultrate(faultrate);
		connectorFaultInfo.setOrderid(orderid);
		connectorFaultInfo.setStage(ChargeStageEnum.ORDER_CALLBACK.getStage());
		connectorFaultInfo.setPhone("");
		connectorFaultInfoRepository.save(connectorFaultInfo);
		
		if("1013".equals(orinCode.toUpperCase())){
			//账单阶段是否是拔枪终止的
			ConnectorStatusEnum portStatus=iConnectorService.getConnectorCurrentStatus(
					ConnectorUtils.getConnectorId(destEquipmentId, portId));
            if(portStatus.getCode()==ConnectorStatusEnum.IDLE.getCode()
            		){
            	//枪口已经空闲,错误码修正为用户拔枪终止
            	userChargeOperation.setSecondcode(ChargeCloudErrCode.BIZ_CUSTOM_UNPLUG_CHARGE_GUN_STOP.getName());
            }else{
            	//枪口非空闲,	充电枪有故障
            	userChargeOperation.setSecondcode(ChargeCloudErrCode.BIZ_CONNECTOR_FAULT.getName());
            }	
            
		}
		
     	userChargeOperation.setErrorcode(errorcode);
     	Date calltime=new Date();
 		userChargeOperation.setCalltime(calltime);
     	userChargeOperation.setOptype(statusEnum.getStage());
     	userChargeOperation.setCallstatus(statusEnum.getDesc());
     	userChargeOperation.setMsg(msg);
     	userChargeOperationRepository.save(userChargeOperation);
     	
    }
    @Override
    public void handleAdjustChargeOrderResponse(OrderInfoResponse_202 orderInfoResponse_202, 
    		String code,String logInfo, MqttNetMsgBase base) {
        if (orderInfoResponse_202 == null)
            return;
        //矫正
        orderInfoResponse_202.setStopReason(code);
        String json = JSONObject.toJSONString(orderInfoResponse_202);
        log.info(logInfo + ":{}", JSONObject.toJSONString(orderInfoResponse_202));
        String equipmentId = orderInfoResponse_202.getEquipmentId();
        String portId = orderInfoResponse_202.getPortId();
        //订单号
        String chargeSeq = orderInfoResponse_202.getCardNum();
        //暂存上报的订单
        stringRedisTemplate.opsForValue().set(RedisConstant.UNICRON_ORDER_INFO + chargeSeq, json);
        calFee(orderInfoResponse_202);
        //触发回调
        notifyService.notifyChargeOrder(orderInfoResponse_202);
        
        //ChargeStageEnum statusEnum=ChargeStageEnum.ORDER_CALLBACK;
        //String errcode=ErrcodeUtils.formatStringErrcode(ChargeStageEnum.ORDER_CALLBACK, orderInfoResponse_202.getStopReason());
        //saveChargeCompleteInfo(equipmentId, portId, chargeSeq,
        //		statusEnum, "自动上报账单",orderInfoResponse_202);
        
    }
    
    @Override
    public void handleChargeOrderResponse(OrderInfoResponse_202 orderInfoResponse_202, String logInfo, MqttNetMsgBase base) {
        if (orderInfoResponse_202 == null)
            return;
       
        String equipmentId = orderInfoResponse_202.getEquipmentId();
        String portId = orderInfoResponse_202.getPortId();
        //订单号
        String chargeSeq = orderInfoResponse_202.getCardNum();
        
        String lastVal=stringRedisTemplate.opsForValue().get(RedisConstant.UNICRON_CHARGE_STATUS + chargeSeq);
        if(lastVal!=null){
        	Float newKwh=0.0f;
        	Float oldKwh=0.0f;
        	StationStatInfoResponse_104 old_s104=null;
        	try{
        		old_s104=JSONObject.parseObject(lastVal, StationStatInfoResponse_104.class);
        		oldKwh=Float.parseFloat(old_s104.getCum_charge_kwh_amount().trim());
        		newKwh=Float.parseFloat(orderInfoResponse_202.getTotalPower().trim());
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        	if(old_s104!=null && (newKwh<0.0001)){
        		orderInfoResponse_202.setTotalPower(old_s104.getCum_charge_kwh_amount());
        	}
        }
        //
        String key=RedisConstant.UNICRON_SYS_STOP_CHARGE_ORDER_KEY+chargeSeq;
    	String value=stringRedisTemplate.opsForValue().get(key);
    	SystemStopChargeRecord para=null;
    	if(value!=null){
    		try{
    			para = JSONObject.parseObject(value, SystemStopChargeRecord.class);
    		}catch(Exception e){
    			
    		}
    		if(para!=null){
    			if(para.getExe_status()>0){
    				//结束原因
    				orderInfoResponse_202.setStopReason(para.getStop_reason());
    				log.info("重置订单结束原因：equipmentId={},chargeSeq={},para.getStopReason={}",equipmentId,chargeSeq,para.getStop_reason());
    			}
    		}
    	}
    	String connectorID=ConnectorUtils.getConnectorId(equipmentId, portId);
    	//判定是否用户拔枪
    	String unplugKey=RedisConstant.UNICRON_CHARGE_CLOUD_CONNECTOTR_UNPLUGIN_TIMESTAMP+connectorID;
    	String unplugKeyValue=stringRedisTemplate.opsForValue().get(unplugKey);
    	if(unplugKeyValue!=null){
    		long unpluginTime=Long.parseLong(unplugKeyValue);
    		if(unpluginTime>0 && (System.currentTimeMillis()-unpluginTime)>=0 && (System.currentTimeMillis()-unpluginTime)<=130*1000){
    			//N秒前有拔枪动作
    			String stopReason=orderInfoResponse_202.getStopReason();
    			if(StringUtils.isNotEmpty(stopReason)
    					&& (ChargeCloudErrCode.INCORRECT_CONNECTION_STOP.getValue()+"").equals(stopReason)){
    				orderInfoResponse_202.setStopReason(ChargeCloudErrCode.BIZ_CUSTOM_UNPLUG_CHARGE_GUN_STOP.getValue()+"");
    			}
    		}
    	}
    	
		//充电结束，更新插枪状态检查，需要重新插枪
        String connectorKey=RedisConstant.UNICRON_CHARGE_CLOUD_CONNECTOTR_EVENT_WORK_STATUS+connectorID;
        stringRedisTemplate.opsForValue().set(connectorKey, ConnectorWorkStatusEnum.UNPLUGIN.getCode());
        
    	String json = JSONObject.toJSONString(orderInfoResponse_202);
        log.info("处理账单上报:traceid={},logInfo={}", orderInfoResponse_202.getEquipmentId(),JSONObject.toJSONString(orderInfoResponse_202));
        //暂存上报的订单
        stringRedisTemplate.opsForValue().set(RedisConstant.UNICRON_ORDER_INFO + chargeSeq, json);
        calFee(orderInfoResponse_202);
        
    	
        //触发回调
        notifyService.notifyChargeOrder(orderInfoResponse_202);
        ChargeStageEnum statusEnum=ChargeStageEnum.ORDER_CALLBACK;
		
        saveChargeCompleteInfo(equipmentId, portId, chargeSeq,
        		statusEnum, "桩上报账单",orderInfoResponse_202);
		
        ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
        if (channelService == null) {
            return;
        }
        saveStopChargeResult(orderInfoResponse_202);
        CMD_201 cmd_201 = new CMD_201();
        OrderInfoQuery_201 orderInfoQuery_201 = new OrderInfoQuery_201();
        orderInfoQuery_201.setPortId(portId);
        orderInfoQuery_201.setCardNum(orderInfoResponse_202.getCardNum());
        log.info("handleChargeOrderResponse orderInfoQuery_201:{}", JSONObject.toJSONString(orderInfoResponse_202));
        base.setResponse(true);
        byte[] result = cmd_201.getPayload(orderInfoQuery_201, base);
        try {
            String msg = new String(result, CharsetDef.CHARSET);
            byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
            log.info("发送账单回复:");
            EncodeUtil.printHex(result);
            channelService.push(equipmentId, logicServerQos.getQos(), sendbytes);
        } catch (UnsupportedEncodingException e) {
            log.error("handleChargeOrderResponse", e);
        }
    }
    private void saveStopChargeResult(OrderInfoResponse_202 orderInfoResponse_202)
   	{
   		ChargeResultInfo chargeResultInfo = chargeResultInfoRepository.findChargeResultInfoByEquipmentidAndConnectorid(
   				orderInfoResponse_202.getEquipmentId(), orderInfoResponse_202.getPortId());
   		if (chargeResultInfo == null) {
   			log.info("can not find chargeResultInfo message,equipmentId:{},gun_no:{}",
   					orderInfoResponse_202.getEquipmentId(), orderInfoResponse_202.getPortId());
   			return;
   		}
   		chargeResultInfo.setStopcode(orderInfoResponse_202.getStopReason());
   		chargeResultInfo.setStoptime(DateUtils.getCurDateTime());

   		String stopReasonDescribe = chargeErrorCodeRepository
   				.findErrordescribeByErrorde(orderInfoResponse_202.getStopReason());
   		
   		if (StringUtils.isBlank(stopReasonDescribe)) {
   			log.info("can not find stopReasonDescribe message,stopReasonDescribe:{}", stopReasonDescribe);
   		}
   		else {
   			chargeResultInfo.setStopcodedescribe(stopReasonDescribe);
   		}
   		chargeResultInfoRepository.save(chargeResultInfo);
   	}

    private void saveStopChargeResultByHistoryOrder(HistoryOrderInfoResponse_402 historyOrderInfoResponse_402)
   	{
   		ChargeResultInfo chargeResultInfo = chargeResultInfoRepository.findChargeResultInfoByEquipmentidAndConnectorid(
   				historyOrderInfoResponse_402.getEquipmentId(), historyOrderInfoResponse_402.getPortId());
   		if (chargeResultInfo == null) {
   			log.info("can not find chargeResultInfo message,equipmentId:{},gun_no:{}",
   					historyOrderInfoResponse_402.getEquipmentId(), historyOrderInfoResponse_402.getPortId());
   			return;
   		}
   		chargeResultInfo.setStopcode(historyOrderInfoResponse_402.getStopReason());
   		chargeResultInfo.setStoptime(DateUtils.getCurDateTime());

   		String stopReasonDescribe = chargeErrorCodeRepository
   				.findErrordescribeByErrorde(historyOrderInfoResponse_402.getStopReason());
   		
   		if (StringUtils.isBlank(stopReasonDescribe)) {
   			log.info("can not find stopReasonDescribe message,stopReasonDescribe:{}", stopReasonDescribe);
   		}
   		else {
   			chargeResultInfo.setStopcodedescribe(stopReasonDescribe);
   		}
   		chargeResultInfoRepository.save(chargeResultInfo);
   	}
    private void calFee(OrderInfoResponse_202 orderInfoResponse_202) {
        double totalMoney = 0;
        double serviceMoney = 0;
        double eleMoney = 0;
        for (int i = 0; i < 48; i++) {
            try {
                Method powerPeriod;
                powerPeriod = OrderInfoResponse_202.class.getMethod("getPowerPeriod" + (i + 1));
                String result = (String) powerPeriod.invoke(orderInfoResponse_202);
                if (!"0".equals(result)) {
                    eleMoney += Double.parseDouble(result) * Double.parseDouble(eleFee[i]) / 10000;
                    serviceMoney += Double.parseDouble(result) * Double.parseDouble(serviceFee[i]) / 10000;
                }
            } catch (Exception e) {
                log.error("handleIntParaSet error", e);
            }
        }

        totalMoney = eleMoney + serviceMoney;
        orderInfoResponse_202.setTotalMoney(String.valueOf(totalMoney));
        orderInfoResponse_202.setServiceMoney(String.valueOf(serviceMoney));
    }

    private void calFee(HistoryOrderInfoResponse_402 historyOrderInfoResponse_402) {
        double totalMoney = 0;
        double serviceMoney = 0;
        double eleMoney = 0;
        for (int i = 0; i < 48; i++) {
            try {
                Method powerPeriod;
                powerPeriod = HistoryOrderInfoResponse_402.class.getMethod("getPowerPeriod" + (i + 1));
                String result = (String) powerPeriod.invoke(historyOrderInfoResponse_402);
                if (StringUtils.isNotBlank(result) && !"0".equals(result)) {
                    eleMoney += Double.parseDouble(result) * Double.parseDouble(eleFee[i]) / 10000;
                    serviceMoney += Double.parseDouble(result) * Double.parseDouble(serviceFee[i]) / 10000;
                }
            } catch (Exception e) {
                log.error("handleIntParaSet error", e);
            }
        }

        totalMoney = eleMoney + serviceMoney;
        historyOrderInfoResponse_402.setTotalMoney(String.valueOf(totalMoney));
        historyOrderInfoResponse_402.setServiceMoney(String.valueOf(serviceMoney));
    }

    @Override
    public void handleHistoryChargeOrderResponse(HistoryOrderInfoResponse_402 historyOrderInfoResponse_402, MqttNetMsgBase base) {
        if (historyOrderInfoResponse_402 == null)
            return;
        String json = JSONObject.toJSONString(historyOrderInfoResponse_402);
        String equipmentId = historyOrderInfoResponse_402.getEquipmentId();
        String chargeSeq = historyOrderInfoResponse_402.getCardNum();
        String portId = historyOrderInfoResponse_402.getPortId();
        stringRedisTemplate.opsForValue().set(RedisConstant.UNICRON_ORDER_INFO + chargeSeq, json);
        calFee(historyOrderInfoResponse_402);
        //触发回调
        notifyService.notifyHistoryChargeOrder(historyOrderInfoResponse_402);
        
        
        ChargeStageEnum statusEnum=ChargeStageEnum.ORDER_CALLBACK;
        saveChargeCompleteInfoByHistoryOrder(equipmentId, portId, chargeSeq,
        		statusEnum, "桩上报账单",historyOrderInfoResponse_402);
        
        saveStopChargeResultByHistoryOrder(historyOrderInfoResponse_402);
        
        ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
        if (channelService == null) {
            return;
        }
        log.info("收到历史账单:{}", json);
        CMD_401 cmd_401 = new CMD_401();
        base.setResponse(true);
        HistoryOrderInfoQuery_401 historyOrderInfoQuery_401 = new HistoryOrderInfoQuery_401();
        historyOrderInfoQuery_401.setChargeIndex(historyOrderInfoResponse_402.getIndex());
        byte[] result = cmd_401.getPayload(historyOrderInfoQuery_401, base);
        try {
            String msg = new String(result, CharsetDef.CHARSET);
            byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
            log.info("发送历史账单回复:");
            EncodeUtil.printHex(result);
            channelService.push(equipmentId, logicServerQos.getQos(), sendbytes);
        } catch (UnsupportedEncodingException e) {
            log.error("handleHistoryChargeOrderResponse", e);
        }
    }

    @Override
    public void handleHistoryLog(LogReportResponse_408 resp_408, MqttNetMsgBase base) {
        log.info("收到日志信息:{}", GsonUtil.getInstance().toJson(resp_408));
        String equipmentId = resp_408.getEquipmentId();
        ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
        if (channelService == null) {
            return;
        }
        CMD_407 cmd_407 = new CMD_407();
        base.setResponse(true);
        byte[] result = cmd_407.getPayload(resp_408.getEquipmentId(), base);
        try {
            String msg = new String(result, CharsetDef.CHARSET);
            byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
            log.info("发送日志上报回复:");
            EncodeUtil.printHex(result);
            channelService.push(equipmentId, logicServerQos.getQos(), sendbytes);
        } catch (UnsupportedEncodingException e) {
            log.error("handleHistoryChargeOrderResponse", e);
        }
    }

    @Override
    public void handleAuthResponse(AuthResponse_802 response_802, MqttNetMsgBase base) {
        response_802.setCmdIndex("" + EncodeUtil.byteToInt(base.getHeadCopyIndex()));
        log.info("收到认证响应:{}", GsonUtil.getInstance().toJson(response_802));
        String equipmentId = response_802.getEquipmentId();
        ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
        if (channelService == null) {
            return;
        }
        String data = response_802.getEncodeData();
        ConcurrentHashMap<String, ClientConnectionInfo> allChannels = ClientConnectionService.getOnlineChannels();
        ClientConnectionInfo client = allChannels.get(equipmentId);

        if (client == null || client.getServerPrivateKey() == null) {
            log.info("#####client is null or client.getServerPrivateKey() is null#####");
            return;
        }
        //modify 2023-07-09
        //String encodeData = RSACoder.decodeByPublicKey(data, ChargeConstant.zhichongClientPublicKey);

        log.info("decodeData data:{} ServerPrivateKey:{}", data, client.getServerPrivateKey());
        try {
            String decodeData = RSACoder.decodeByPrivateKey(data, client.getServerPrivateKey());

            log.info("decodeData:{}", decodeData);
            String sendData = equipmentId + ChargeConstant.AUTH_CONSTANT;

            if (StringUtils.isNotBlank(decodeData) && decodeData.equals(sendData)) {
                log.info("check true:{}", response_802.getEquipmentId());
                iSecurityService.updateCheckStatus(equipmentId, base.getIndexVal() + "", "1");
                stringRedisTemplate.opsForValue().increment(RedisConstant.UNICRON_IS_SEND_AUTH + equipmentId, -1);
            }
        } catch (Exception e) {
            log.info("client decode exception", e);
            e.printStackTrace();
        }

    }

    /**
     * 设置控制命令响应
     */
    public void handle1302(String s1302, MqttNetMsgBase base) {
        log.info("收到价格设置响应:{}", s1302);
    }

    @Override
    public void handle1304(String[] s1304, MqttNetMsgBase base) {
        log.info("收到价格数组响应:{}", StringUtils.join(s1304));
    }

    @Override
    public void handle1306(ServiceFee_1306 s1306, MqttNetMsgBase base) {
        log.info("收到服务费数组响应:{}", JSONObject.toJSONString(s1306));
    }

    @Override
    public void handle1308(DelayFee_1308 s1308, MqttNetMsgBase base) {
        log.info("收到延時费响应:{}", JSONObject.toJSONString(s1308));

    }

    @Override
    public void handle1102(HttpUpdate_1102 h1102, MqttNetMsgBase base) {
        if (h1102 == null)
            return;
        String json = JSONObject.toJSONString(h1102);
        log.info("收到充电桩应答服务器升级指令响应:{}", json);
        if ("0".equals(h1102.getSuccess())) {
            log.info("收到充电桩应答服务器升级指令成功");
        } else {
            log.info("收到充电桩应答服务器升级指令失败");
        }
    }

    @Override
    public void handle1104(FileVersion_1104 fileVersion_1104, String topic, MqttNetMsgBase base) {
        if (fileVersion_1104 == null)
            return;
        String json = JSONObject.toJSONString(fileVersion_1104);
        log.info("收到充电桩主动上报桩的程序、中间件、广告文件的版本信息:{}", json);
        ChannelService channelService = PubChannelUtil.getChannelService(topic, stationManagementService);
        if (channelService == null) {
            return;
        }
        CMD_1103 cmd_1103 = new CMD_1103();
        FileVersion_1103 fileVersion_1103 = new FileVersion_1103();
        fileVersion_1103.setType(fileVersion_1104.getType());
        fileVersion_1103.setEquals("0");
        base.setResponse(true);
        byte[] result = cmd_1103.getPayload(fileVersion_1103, base);
        try {
            String msg = new String(result, CharsetDef.CHARSET);
            byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
            log.info("发送103回复:");
            EncodeUtil.printHex(result);
            channelService.push(topic, logicServerQos.getQos(), sendbytes);
        } catch (UnsupportedEncodingException e) {
            log.error("handle1104 error", e);
        }
    }

    @Override
    public void handle1106(UpdateProcess_1106 updateProcess_1106, String topic, MqttNetMsgBase base) {
        if (updateProcess_1106 == null)
            return;
        ChannelService channelService = PubChannelUtil.getChannelService(topic, stationManagementService);
        if (channelService == null) {
            return;
        }
        String json = JSONObject.toJSONString(updateProcess_1106);
        log.info("收到充电桩上报升级进度信息:{}", json);
        CMD_1105 cmd_1105 = new CMD_1105();
        UpdateProcess_1105 updateProcess_1105 = new UpdateProcess_1105();
        updateProcess_1105.setType(updateProcess_1106.getType());
        base.setResponse(true);
        byte[] result = cmd_1105.getPayload(updateProcess_1105, base);
        try {
            String msg = new String(result, CharsetDef.CHARSET);
            byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
            log.info("发送1105回复:");
            EncodeUtil.printHex(result);
            channelService.push(topic, logicServerQos.getQos(), sendbytes);
        } catch (UnsupportedEncodingException e) {
            log.error("handle1106 error", e);
        }
    }

    private void saveBSMHighTempInfo(ChargeOrderBMSHighTempInfo info){
    	if(info==null) return;
    	chargeOrderBMSHighTempInfoRepository.save(info);
    }
    
    @Override
    public void handleBMSRequest(BMSRequest_302 resp_302, String topic, MqttNetMsgBase base) {
        if (resp_302 == null)
            return;
        
        String json = JSONObject.toJSONString(resp_302);
        log.info("收到充电桩上报BMS信息:{}", json);
        CMD_301 cmd_301 = new CMD_301();
        BMSInfoResponse_301 bmsInfoResponse_301 = new BMSInfoResponse_301();
        bmsInfoResponse_301.setEquipmentId(topic);
        bmsInfoResponse_301.setGun_no(resp_302.getGun_no());
        bmsInfoResponse_301.setIndex(resp_302.getIndex());
        base.setResponse(true);
        //充电口
        String connectorId = ConnectorUtils.getConnectorId(bmsInfoResponse_301.getEquipmentId(), bmsInfoResponse_301.getGun_no());
        //订单号
        String orderid = stringRedisTemplate.opsForValue().get(RedisConstant.UNICRON_CONNECTTOR_CHARGESEQ_REL + connectorId);
        if(StringUtils.isNotBlank(orderid)){
            //保存订单BMS信息
            stringRedisTemplate.opsForValue().set(RedisConstant.UNICRON_BMS_STATUS + orderid, json);
        }
        
        String dateStr = DateUtils.getCurrentDateyyyyMMddHHmmss();
        ChargeOrderBMSHighTempInfo info=new ChargeOrderBMSHighTempInfo();
        info.setConnectorid(connectorId);
        String hightemp=resp_302.getBSM_max_temperature();
		info.setHightemp(hightemp);
        info.setOrderid(orderid);
        info.setReporttime(dateStr);
        info.setComments("");
        saveBSMHighTempInfo(info);
        
        ChannelService channelService = PubChannelUtil.getChannelService(topic, stationManagementService);
        if (channelService == null) {
            return;
        }
        byte[] result = cmd_301.getPayload(bmsInfoResponse_301, base);
        try {
            String msg = new String(result, CharsetDef.CHARSET);
            byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
            log.info("发送301回复:");
            EncodeUtil.printHex(result);
            channelService.push(topic, logicServerQos.getQos(), sendbytes);
        } catch (UnsupportedEncodingException e) {
            log.error("handle1106 error", e);
        }

    }

    // 处理bms历史文件上传
    @Override
    public void handleBmshistoryUpload(LogReportResponse_408 resp_408, MqttNetMsgBase base) {
        // TODO Auto-generated method stub
        log.info("收到Bms日志信息:{}", GsonUtil.getInstance().toJson(resp_408));
    }

    @Override
    public void handleEventReport(StationEventReport_108 stationEventReport_108, MqttNetMsgBase base, String equipmentId) {
        if (stationEventReport_108 == null)
            return;
        String deviceid = equipmentId;
        String eventName = stationEventReport_108.getEvent_name();
        String gunNo = stationEventReport_108.getGun_no();
        String connectorId = ConnectorUtils.getConnectorId(deviceid, gunNo);
        String connectorKey=RedisConstant.UNICRON_CHARGE_CLOUD_CONNECTOTR_EVENT_WORK_STATUS+connectorId;
        String unplugKey=RedisConstant.UNICRON_CHARGE_CLOUD_CONNECTOTR_UNPLUGIN_TIMESTAMP+connectorId;
        log.info("handleEventReport:equipmentId={}||收到事件上报:{}||eventName={}||gunNo={}",
        		equipmentId, JSONObject.toJSONString(stationEventReport_108),eventName,gunNo);
        switch (eventName) {
        case "1"://充电枪插枪
            //枪号
            log.info("notify handleEventReport:{},connectorId={}", JSONObject.toJSONString(stationEventReport_108),connectorId);
            notifyService.notifyConnectorStatus(connectorId, ConnectorStatusEnum.UNCHARGE.getCode());
            //缓存，状态，已经插枪
            stringRedisTemplate.opsForValue().set(connectorKey, ConnectorWorkStatusEnum.PLUGIN.getCode());
            break;
        case "2"://充电枪拔枪
            //枪号
            log.info("notify handleEventReport:{},connectorId={}", JSONObject.toJSONString(stationEventReport_108),connectorId);
            notifyService.notifyConnectorStatus(connectorId, ConnectorStatusEnum.IDLE.getCode());
            stringRedisTemplate.opsForValue().set(connectorKey, ConnectorWorkStatusEnum.UNPLUGIN.getCode());
            //记录拔枪时间
            stringRedisTemplate.opsForValue().set(unplugKey, System.currentTimeMillis()+"");
            break;
        case "3"://充电枪开始充电
            //枪号
            log.info("notify handleEventReport:{},connectorId={}", JSONObject.toJSONString(stationEventReport_108),connectorId);
            notifyService.notifyConnectorStatus(connectorId, ConnectorStatusEnum.CHARGING.getCode());
            
            break;
        case "4"://充电结束
            //枪号
            log.info("notify handleEventReport:{},connectorId={}", JSONObject.toJSONString(stationEventReport_108),connectorId);
            notifyService.notifyConnectorStatus(connectorId, ConnectorStatusEnum.IDLE.getCode());
            //更新状态
            stringRedisTemplate.opsForValue().set(connectorKey, ConnectorWorkStatusEnum.UNPLUGIN.getCode());
            break;
        case "5"://充电取消
            //枪号
            log.info("notify handleEventReport:{},connectorId={}", JSONObject.toJSONString(stationEventReport_108),connectorId);
            notifyService.notifyConnectorStatus(connectorId, ConnectorStatusEnum.IDLE.getCode());
            //更新状态
            stringRedisTemplate.opsForValue().set(connectorKey, ConnectorWorkStatusEnum.UNPLUGIN.getCode());
            break;    
         default:
        	 break;
        }
        
        ChannelService channelService = PubChannelUtil.getChannelService(deviceid, stationManagementService);
        if (channelService == null) {
        	log.info("设备offline：{}",deviceid);
            return;
        }
        StationEventReportResponse_107 response = new StationEventReportResponse_107();
        response.setStation_id(deviceid);
        response.setGun_no(gunNo);
        response.setEvent_name(stationEventReport_108.getEvent_name());
        
       
       
        CMD_107 cmd_107=new CMD_107();
        byte[] result = cmd_107.getPayload(response, base);
        
        try {
            String msg = new String(result, CharsetDef.CHARSET);
            byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
            log.info("发送107回复:traceid={},bytes={}",deviceid,EncodeUtil.printHex(result));
            
            channelService.push(deviceid, logicServerQos.getQos(), sendbytes);
        } catch (Exception e) {
            log.error("handleEventReport error,{}", e.getMessage());
        }

    }
    
    @Override
    public void handleFaultReport(StationFaultReport_118_ext stationFaultReport_118, MqttNetMsgBase base, String equipmentId) {
        if (stationFaultReport_118 == null 
        		||StringUtils.isBlank(stationFaultReport_118.getErrcode()))
            return;
        String deviceid = stationFaultReport_118.getEquipmentid();
        String errcode = stationFaultReport_118.getErrcode();
        String gunNo = stationFaultReport_118.getGun_no();
        String status = stationFaultReport_118.getStatus();
        log.info("handleFaultReport:equipmentId={}||收到故障上报:{}||errcode={}||gunNo={}",
        		equipmentId, JSONObject.toJSONString(stationFaultReport_118),errcode,gunNo);
        String connectorId = ConnectorUtils.getConnectorId(deviceid, gunNo);
        ConnectorStatusInfo connectorStatusInfo=new ConnectorStatusInfo();
        ChargeCloudErrCode cloudCode = StationFaultCode.toCloudErrorCode(errcode);
        if(cloudCode==null){
        	cloudCode=ChargeCloudErrCode.UNKNOWN_ERROR;
        }
        connectorStatusInfo.setErrorCode(cloudCode.getValue()+"");
        connectorStatusInfo.setErrorMsg(cloudCode.getDetail());
        String mkey=RedisConstant.UNICRON_CONNECTTOR_FAULT_INFO+deviceid;
        HashOperations<String, String, String> vo = stringRedisTemplate.opsForHash();
        Map<String,String> errormap=vo.entries(mkey);
        switch (status) {
        case "0":
        	//fix bug 20181120
        	if(ChargeCloudErrCode.UNKNOWN_ERROR.equals(cloudCode)){
        		break;
        	}
            //故障发生
        	if("0".equals(gunNo)){
        		for(int i=1;i<=2;i++){
        			connectorId=ConnectorUtils.getConnectorId(deviceid, i+"");
        			connectorStatusInfo.setStatus(Integer.parseInt(ConnectorStatusEnum.DAMAGE.getCode()));
                	vo.put(mkey, connectorStatusInfo.getErrorCode(), connectorStatusInfo.getErrorMsg());
                	log.info("notify handleFaultReport:{},connectorId={}", JSONObject.toJSONString(stationFaultReport_118),connectorId);
                    connectorStatusInfo.setFaultStatus(1);
        			notifyService.notifyConnectorFaultStatus(connectorId, connectorStatusInfo);
        		}
        		
        	}else{
        		connectorStatusInfo.setStatus(Integer.parseInt(ConnectorStatusEnum.DAMAGE.getCode()));
            	vo.put(mkey, connectorStatusInfo.getErrorCode(), connectorStatusInfo.getErrorMsg());
            	log.info("notify handleFaultReport:{},connectorId={}", JSONObject.toJSONString(stationFaultReport_118),connectorId);
                connectorStatusInfo.setFaultStatus(1);
    			notifyService.notifyConnectorFaultStatus(connectorId, connectorStatusInfo);
        	}
        	
            break;
        case "1":
        	if(ChargeCloudErrCode.UNKNOWN_ERROR.equals(cloudCode)){
        		break;
        	}
            //故障恢复
        	connectorStatusInfo.setStatus(Integer.parseInt(ConnectorStatusEnum.IDLE.getCode()));
        	connectorStatusInfo.setFaultStatus(2);
            log.info("notify handleFaultReport:{},connectorId={}", JSONObject.toJSONString(stationFaultReport_118),connectorId);
            if("0".equals(stationFaultReport_118.getErrcode())||
            		"0000".equals(stationFaultReport_118.getErrcode())){
            	String connectorId1 = ConnectorUtils.getConnectorId(deviceid, "1");
            	String connectorId2= ConnectorUtils.getConnectorId(deviceid, "2");
            	for(String code:errormap.keySet()){
         			vo.delete(mkey, code);
         			connectorStatusInfo.setErrorCode(code);
         			if(ChargeCloudErrCode.getByName(code)!=null){
         				connectorStatusInfo.setErrorMsg(ChargeCloudErrCode.getByName(code).getDetail());
         			}
         			notifyService.notifyConnectorFaultStatus(connectorId1, connectorStatusInfo);
                	notifyService.notifyConnectorFaultStatus(connectorId2, connectorStatusInfo);
        		}
            	
            }else{
            	vo.delete(mkey, connectorStatusInfo.getErrorCode());
            	if("0".equals(gunNo)){
            		for(int i=1;i<=2;i++){
            			connectorId=ConnectorUtils.getConnectorId(deviceid, i+"");
            			notifyService.notifyConnectorFaultStatus(connectorId, connectorStatusInfo);
            		}
            	}else{
            		notifyService.notifyConnectorFaultStatus(connectorId, connectorStatusInfo);
            	}
            	
            }
            
            break;
         default:
        	 break;
        }
        
       
		
        
        ChannelService channelService = PubChannelUtil.getChannelService(deviceid, stationManagementService);
        if (channelService == null) {
            return;
        }
        StationFaultReportResponse_117 response = new StationFaultReportResponse_117();
        
        response.setErrcode(EncodeUtil.byteToCharsequence(stationFaultReport_118.getErrorcodeBytes(), false));
        response.setGun_no(gunNo);
        response.setStation_id(deviceid);
       
        CMD_117 cmd_117=new CMD_117();
        byte[] result = cmd_117.getPayload(response, base);
        
        try {
            String msg = new String(result, CharsetDef.CHARSET);
            byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
            log.info("发送117回复:bytes={}",EncodeUtil.printHex(result));
            
            channelService.push(deviceid, logicServerQos.getQos(), sendbytes);
        } catch (UnsupportedEncodingException e) {
            log.error("handleFaultReport error", e);
        }
    }


	@Override
	public void handleStopChargeBySeqCmd(StopChargeCommandResponse_12 resp_12, MqttNetMsgBase base) {
		// TODO Auto-generated method stub
		 if (resp_12 == null)
	            return;
	        String destEquipmentId=resp_12.getEquipmentId();
			String portId=resp_12.getGunNo();
	        String connectorId = ConnectorUtils.getConnectorId(destEquipmentId, portId);
	        
	        String orderid = stringRedisTemplate.opsForValue().get(RedisConstant.UNICRON_CONNECTTOR_CHARGESEQ_REL + connectorId);
	       
	        String errorcode=ErrcodeUtils.formatStringErrcode(ChargeStageEnum.STOPPING_ASYN, resp_12.getResult());
	        if ("0".equals(resp_12.getResult())) {
	            log.info("收到控制命令响应:{}", JSONObject.toJSONString(resp_12));
	            ChargeStageEnum statusEnum=ChargeStageEnum.STOPPING_ASYN;
	            //暂不存储
				//saveStopChargeOperationInfo(destEquipmentId, portId, orderid, statusEnum, "用户成功终止充电", errorcode);
	            
	        } else {
	        	ChargeStageEnum statusEnum=ChargeStageEnum.STOPPING_ASYN;
				//saveStopChargeOperationInfo(destEquipmentId, portId, orderid, statusEnum, "用户终止充电异常", errorcode);
	            log.error("handleIntParaSet error:{}", JSONObject.toJSONString(resp_12));
	        }
	}

	@Override
	public void handleSowerStrategyDispatchCmd(StationPowerDispatchResult_16 stationPowerDispatchResult_16,
			MqttNetMsgBase base) {
		// TODO Auto-generated method stub
		 if (stationPowerDispatchResult_16 == null)
	            return;
		 log.info("收到充电功率分配回复信息:{}", GsonUtil.getInstance().toJson(stationPowerDispatchResult_16));
	}

	@Override
	public void handleASKSowerStrategyDispatchCmd(
			StationPowerStategyDispatchQueryResponse_18 stategyDispatchQueryResponse_18, MqttNetMsgBase base) {
		// TODO Auto-generated method stub
		if (stategyDispatchQueryResponse_18 == null) {
			return;
		}
		log.info("收到查询功率分配策略回复信息:{}",JSON.toJSONString(stategyDispatchQueryResponse_18));
	}

	@Override
	public void handleLittleSowerStrategyDispatchCmd(
			StationChargeUnitQueryResponse_14 stationChargeUnitQueryResponse_14, MqttNetMsgBase base) {
		// TODO Auto-generated method stub
	
		if(stationChargeUnitQueryResponse_14 == null)
			return;
		log.info("收到查询最小充电分配单元回复信息:{}",JSON.toJSONString(stationChargeUnitQueryResponse_14));
	}

}
