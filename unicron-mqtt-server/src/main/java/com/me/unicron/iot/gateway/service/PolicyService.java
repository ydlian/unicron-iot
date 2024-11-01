package com.me.unicron.iot.gateway.service;

import com.me.epower.component.TResponse;
import com.me.epower.direct.entity.chargecloud.DelayFeeSetParam;
import com.me.epower.direct.entity.chargecloud.FixPolicySetParam;
import com.me.epower.direct.entity.chargecloud.PolicySetParam;
import com.me.epower.direct.entity.chargecloud.ServiceFeeParam;

public interface PolicyService {
    
    //后台下发峰平谷电费计价策略设置
    public TResponse<String> setPolicy(PolicySetParam policySetParam);
    
    //后台下发峰平谷电费计价策略查询
    public TResponse<String> queryPolicy(PolicySetParam policySetParam);
    
    //后台下发固定电费计价策略设置
    public TResponse<String> setFixPolicy(FixPolicySetParam fixPolicySetParam);
    
    //后台下发固定电费计价策略查询
    public TResponse<String> queryFixPolicy(FixPolicySetParam fixPolicySetParam);
    
    //后台下发峰平谷电费计价策略设置
    public TResponse<String> setServiceFee(ServiceFeeParam serviceFeeParam);
    
    //后台下发峰平谷电费计价策略设置
    public TResponse<String> queryServiceFee(ServiceFeeParam serviceFeeParam);

    
    //后台延时费计价策略设置
    public TResponse<String> setDelayFee(DelayFeeSetParam delayFeeParam);
    
    //后台延时费计价策略设置
    public TResponse<String> queryDelayeFee(DelayFeeSetParam delayFeeParam);

    
    
}
