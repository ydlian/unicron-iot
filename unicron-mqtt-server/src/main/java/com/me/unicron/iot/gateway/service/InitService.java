package com.me.unicron.iot.gateway.service;

import com.me.epower.component.CmdExcuteResult;
import com.me.epower.component.TResponse;
import com.me.epower.direct.entity.chargecloud.CleanRequestParam;
import com.me.epower.direct.entity.chargecloud.ControlQueryParam;
import com.me.epower.direct.entity.chargecloud.FileVersionQueryParam;
import com.me.epower.direct.entity.chargecloud.HttpUpdateParam;
import com.me.epower.direct.entity.chargecloud.ScreenInfoParam;
import com.me.epower.direct.entity.chargecloud.logBmsRequestParam;
import com.me.epower.direct.entity.downward.StationIntPara_1;
import com.me.epower.direct.entity.downward.StationStringPara_3;

public interface InitService {

    /**
     * 后台服务器设置充电桩整形工作参数
     */
    public TResponse<String> setIntPara(StationIntPara_1 stationIntPara_1);

    /**
     * 后台服务器查询充电桩整形工作参数
     */
    public TResponse<String> queryIntPara(StationIntPara_1 stationIntPara_1);

    /**
     * 后台服务器设置充电桩字符串形工作参数
     */
    public TResponse<CmdExcuteResult> setStringPara(StationStringPara_3 stationStringPara_3);

    /**
     * 后台服务器设置充电桩字符串形工作参数
     */
    public TResponse<CmdExcuteResult> queryStringPara(StationStringPara_3 stationStringPara_3);

    /**
     * 后台服务器设置充电桩字符串形工作参数
     */
    public TResponse<String> controlRequest(ControlQueryParam controlQueryParam);

    /**
     * 后台服务器向充电桩下发升级指令
     */
    public TResponse<String> httpUpdate(HttpUpdateParam httpUpdateParam);

    /**
     * 后台服务器向充电桩下发清除字符型指令
     */
    public TResponse<String> cleanStringRequest(CleanRequestParam cleanRequestParam);
    
    /**
     * 后台服务器向充电桩下发清除整型指令
     */
    public TResponse<String> cleanIntRequest(CleanRequestParam cleanRequestParam);

    /**
     * 
     */
    public TResponse<String> fileVersionQuery(FileVersionQueryParam fileVersionQueryParam);

    
    public TResponse<String> logQuery(String equipmentId);
    
    /**
     * 
     */
    public TResponse<String> authRequest(String equipmentId);

    
    public TResponse<String> logQueryBms(logBmsRequestParam logBmsRequestParam);
    
    public TResponse<String> setScreenInfo(ScreenInfoParam screenInfoParam);
}
