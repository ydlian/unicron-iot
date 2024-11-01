package com.me.unicron.iot.gateway.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.me.epower.direct.entity.constant.RedisConstant;
import com.me.epower.direct.enums.ConnectorStatusEnum;
import com.me.epower.entity.OperatorDevice;
import com.me.epower.repositories.OperatorDeviceRepository;
import com.me.unicron.iot.gateway.service.IConnectorService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConnectorServiceImpl implements IConnectorService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private NotifyServiceImpl notifyService;

    @Autowired
    private OperatorDeviceRepository operatorDeviceRepository;

    /**
     * 推送枪状态
     */
    @Override
    public void handleConnectorStatus(String connectorId, String newStatus) {
        String connector_status = stringRedisTemplate.opsForValue().get(RedisConstant.UNICRON_CONNECTTOR_STATUS + connectorId);
        if (StringUtils.isNotBlank(newStatus)) {
            String status = getConnectorStatus(newStatus);
            if (!status.equals(connector_status)) {
                updateConnectorStatus(connectorId, connector_status, status);
                notifyService.notifyConnectorStatus(connectorId, status);
            }
        }
    }

    /**
     * 根据桩上报的工作状态生成桩状态
     * 
     * @param newStatus
     * @return
     */
    public String getConnectorStatus(String newStatus) {
        String status = null;
        switch (newStatus) {
        case "0":
            status = ConnectorStatusEnum.IDLE.getCode();
            break;
        case "1":
            status = ConnectorStatusEnum.UNCHARGE.getCode();
            break;
        case "2":
            status = ConnectorStatusEnum.CHARGING.getCode();
            break;
        case "4":
            status = ConnectorStatusEnum.BOOKED.getCode();
            break;
        case "6":
            status = ConnectorStatusEnum.DAMAGE.getCode();
            break;
        default:
            status = ConnectorStatusEnum.UNCHARGE.getCode();
            break;
        }
        return status;
    }

    public void handleConnectorOffline(String connectorId) {
        //log.info("updateConnectorStatus:{},status:{}", connectorId, ConnectorStatusEnum.OFFLINE.getCode());
        String connectorStatus = stringRedisTemplate.opsForValue().get(RedisConstant.UNICRON_CONNECTTOR_STATUS + connectorId);
        updateConnectorStatus(connectorId, connectorStatus, ConnectorStatusEnum.OFFLINE.getCode());
        notifyService.notifyConnectorStatus(connectorId, ConnectorStatusEnum.OFFLINE.getCode());
    }

    public void updateConnectorStatus(String connectorId, String connectorStatus, String status) {
        if ((StringUtils.isBlank(connectorStatus)) || !(connectorStatus.equals(status))) {
            log.info("updateConnectorStatus:{},connectorStatus:{},status:{}", connectorId, connectorStatus, status);
            stringRedisTemplate.opsForValue().set(RedisConstant.UNICRON_CONNECTTOR_STATUS + connectorId, status);
            notifyService.notifyConnectorStatus(connectorId, status);
        }
    }

    public String getOperatorId(String connectorId) {
        String operatorId = stringRedisTemplate.opsForValue().get(RedisConstant.UNICRON_CONNECTOR_OPERATOR + connectorId);
        if (StringUtils.isNotBlank(operatorId)) {
            return operatorId;
        } else {
            String deviceid = connectorId.substring(0, connectorId.length() - 2);
            OperatorDevice operatorDevice = operatorDeviceRepository.findOneDeviceByDeviceid(deviceid);
            if (operatorDevice != null) {
                operatorId = operatorDevice.getHlhtOperatorId();
                stringRedisTemplate.opsForValue().set(RedisConstant.UNICRON_CONNECTOR_OPERATOR + connectorId, operatorId);
                return operatorId;
            }
        }
        return null;
    }

	@Override
	public ConnectorStatusEnum getConnectorCurrentStatus(String connectorId) {
		// TODO Auto-generated method stub
		String connector_status = stringRedisTemplate.opsForValue().get(RedisConstant.UNICRON_CONNECTTOR_STATUS + connectorId);
		ConnectorStatusEnum status=null;
		switch (connector_status) {
        case "0":
            status = ConnectorStatusEnum.IDLE;
            break;
        case "1":
            status = ConnectorStatusEnum.UNCHARGE;
            break;
        case "2":
            status = ConnectorStatusEnum.CHARGING;
            break;
        case "4":
            status = ConnectorStatusEnum.BOOKED;
            break;
        case "6":
            status = ConnectorStatusEnum.DAMAGE;
            break;
        default:
            status = null;
            break;
        }
        return status;
	}

}
