package com.me.unicron.iot.gateway.service;

import org.springframework.stereotype.Service;

import com.me.epower.direct.entity.downward.BMSRequest_302;
import com.me.epower.direct.entity.upward.AuthResponse_802;
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
import com.me.epower.direct.entity.upward.StationFaultReport_118;
import com.me.epower.direct.entity.upward.StationFaultReport_118_ext;
import com.me.epower.direct.entity.upward.StationPowerDispatchResult_16;
import com.me.epower.direct.entity.upward.StationPowerStategyDispatchQueryResponse_18;
import com.me.epower.direct.entity.upward.StationStatInfoResponse_104;
import com.me.epower.direct.entity.upward.StopChargeCommandResponse_12;
import com.me.epower.direct.entity.upward.StringhParaSetResponse_4;
import com.me.epower.direct.entity.upward.UpdateProcess_1106;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;

import io.netty.channel.Channel;

@Service
public interface ResponseHandleService {

    /**
     * 充电桩上传心跳包信息
     */
    public void handleBeatHeartResponse(Channel channel,BeatHeartResponse_102 beatHeartResponse_102, MqttNetMsgBase base);

    /**
     * 将上传的充电状态信息保存到缓存中
     */
    public void handleChargeStatusUpload(StationStatInfoResponse_104 stationStatInfoResponse_104, MqttNetMsgBase base);

    /**
     * 将查询的充电状态信息保存到缓存中
     */
    public void handleQueryChargeStatusUpload(StationStatInfoResponse_104 stationStatInfoResponse_104, MqttNetMsgBase base);

    /**
     * 服务器应答充电桩签到信息
     */
    public void handleSignup(SignupResponse_106 resp_106, MqttNetMsgBase base);

    /**
     * 设置整型命令响应
     * @param base 
     */
    public void handleIntParaSet(IntParaSetResponse_2 resp_2, MqttNetMsgBase base);

    /**
     * 设置字符型命令响应
     * @param base 
     */
    public void handleStringParaSet(StringhParaSetResponse_4 resp_4, MqttNetMsgBase base);

    /**
     * 控制命令响应
     * @param base 
     */
    public void handleControlCmd(ControlCommandResponse_6 resp_6, MqttNetMsgBase base);

    /**
     * 处理充电启动响应
     */
    public void handleStartChargeResponse(StartChargeResponse_8 resp_8, MqttNetMsgBase base);

    /**
     * 设置控制命令响应
     */
    public void handleCleanResponse(CleanResponse_20 cleanResponse_20, MqttNetMsgBase base);

    public void handleLockResponse(LockResponse_24 lockResponse_24, MqttNetMsgBase base);

    public void handleEventReport(StationEventReport_108 stationEventReport_108, MqttNetMsgBase base,String equipmentId);

    public void handleFaultReport(StationFaultReport_118_ext stationFaultReport_118, MqttNetMsgBase base,String equipmentId);

    /**
     * 处理上传的账单信息
     */
    public void handleChargeOrderResponse(OrderInfoResponse_202 orderInfoResponse_202, String logInfo, MqttNetMsgBase base);
    
    public void handleAdjustChargeOrderResponse(OrderInfoResponse_202 orderInfoResponse_202,
    		String code, String logInfo, MqttNetMsgBase base);
    /**
     * 处理上传的
     */
    public void handleBMSRequest(BMSRequest_302 resp_302, String topic, MqttNetMsgBase base);

    /**
     * 处理上传的账单信息
     */
    public void handleHistoryChargeOrderResponse(HistoryOrderInfoResponse_402 historyOrderInfoResponse_402, MqttNetMsgBase base);

    /**
     * 处理上传的日志
     */
    public void handleHistoryLog(LogReportResponse_408 resp_408, MqttNetMsgBase base);

    /**
     * 处理上传的价格信息
     */
    public void handle1302(String s1302, MqttNetMsgBase base);

    /**
     * 处理上传的价格信息
     */
    public void handle1304(String[] s1304, MqttNetMsgBase base);

    /**
     * 处理上传的价格信息
     */
    public void handle1306(ServiceFee_1306 s1306, MqttNetMsgBase base);

    /**
     * 处理上传的价格信息
     */
    public void handle1308(DelayFee_1308 s1308, MqttNetMsgBase base);

    /**
     * 充电桩应答服务器升级指令
     */
    public void handle1102(HttpUpdate_1102 h1102, MqttNetMsgBase base);

    /**
     * 充电桩主动上报桩的程序、中间件、广告文件的版本信息
     */
    public void handle1104(FileVersion_1104 fileVersion_1104, String topic, MqttNetMsgBase base);

    /**
     * 充电桩上报升级进度
     * @param base 
     */
    public void handle1106(UpdateProcess_1106 updateProcess_1106, String topic, MqttNetMsgBase base);

    void handleAuthResponse(AuthResponse_802 response_802, MqttNetMsgBase base);

    public void handleBmshistoryUpload(LogReportResponse_408 resp_408, MqttNetMsgBase base); 
    
    public void handleStopChargeBySeqCmd(StopChargeCommandResponse_12 resp_12, MqttNetMsgBase base);
    
    public void handleLittleSowerStrategyDispatchCmd(StationChargeUnitQueryResponse_14 stationChargeUnitQueryResponse_14, MqttNetMsgBase base);
    
    public void handleSowerStrategyDispatchCmd(StationPowerDispatchResult_16 stationPowerDispatchResult_16, MqttNetMsgBase base);
    
    public void handleASKSowerStrategyDispatchCmd(StationPowerStategyDispatchQueryResponse_18 stategyDispatchQueryResponse_18, MqttNetMsgBase base);
}
