package com.me.unicron.iot.gateway.service;

import com.me.epower.direct.enums.ConnectorStatusEnum;

public interface IConnectorService {

    /**
     * 推送枪状态
     */
    public void handleConnectorStatus(String connectorId,String newStatus);
    
    
    /**
     * 获取枪的实时状态
     */
    public ConnectorStatusEnum getConnectorCurrentStatus(String connectorId);
    
   
   
    
    /**
     * 根据桩上报的工作状态生成桩状态
     */
    public String getConnectorStatus(String newStatus);
    
    /**
     * 直接更新枪状态
     */
    public void updateConnectorStatus(String connector, String connectorStatus,String status);
    
    /**
     * 直接更新枪离网状态
     */
    public void handleConnectorOffline(String connectorId);
    
    /**
     * 供应商id
     */
    public String getOperatorId(String connectorId);

    
    
}
