package com.me.unicron.iot.gateway.service;

import com.me.epower.component.TResponse;
import com.me.epower.direct.entity.chargecloud.ChargeUnitQueryParam;
import com.me.epower.direct.entity.chargecloud.PowerDispatchStategyQueryParam;
import com.me.epower.direct.entity.chargecloud.StationPowerDispatchParam;

public interface IPowerDispatchService {

	//后台下发峰平谷电费计价策略设置 13
    public TResponse<String> queryChargeUnitInfo(ChargeUnitQueryParam chargeUnitQueryParam);
    //15
    public TResponse<String> executeChargeUnitPowerDispatch(StationPowerDispatchParam stationPowerDispatchParam);
    //17
    public TResponse<String> queryPowerDispatchStategy(PowerDispatchStategyQueryParam powerDispatchStategyQueryParam);
    
}
