package com.me.unicron.iot.gateway.service;

import org.springframework.stereotype.Component;

import com.me.epower.component.TResponse;
import com.me.epower.direct.entity.chargecloud.ChargeStatusParam;
import com.me.epower.direct.entity.chargecloud.LockRequestParam;
import com.me.epower.direct.entity.chargecloud.QueryStartChargeParam;
import com.me.epower.direct.entity.chargecloud.QueryStopChargeParam;
import com.me.epower.direct.entity.downward.HistoryOrderQuery_403;
import com.me.epower.direct.entity.downward.RecentOrderQuery_205;

@Component
public interface OperatorCharge {
    //发起启动充电
    public TResponse<String> queryStartCharge(QueryStartChargeParam queryStartChargeParam);
    
    //查询充电状态
    public TResponse<String> queryEquipChargeStatus(ChargeStatusParam chargeStatusParam);
   
    //发起停止充电
    public TResponse<String> queryStopCharge(QueryStopChargeParam queryStopChargeParam);
    
    //查询历史账单
    public TResponse<String> queryHistoryOrder(HistoryOrderQuery_403 historyOrderQuery_403);
    
    //查询最近账单
    public TResponse<String> queryRecentOrder(RecentOrderQuery_205 recentOrderQuery_205);
    
    //地锁控制
    public TResponse<String> lockRequest(LockRequestParam lockRequestParam);

    //根据订单发起停止充电
    public TResponse<String> queryStopChargeByChargeSeq(QueryStopChargeParam queryStopChargeParam);
    
}
