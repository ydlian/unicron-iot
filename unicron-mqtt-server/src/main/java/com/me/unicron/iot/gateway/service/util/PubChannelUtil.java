package com.me.unicron.iot.gateway.service.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.me.unicron.iot.bootstrap.ChannelService;
import com.me.unicron.iot.bootstrap.bean.StationInfo;
import com.me.unicron.iot.bootstrap.channel.StationManagementService;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class PubChannelUtil {
    
    /**
     * 根据设备id取到通道服务
     * @param equipmentId
     * @param stationManagementService
     * @return
     */
    public static ChannelService getChannelService(String equipmentId,StationManagementService stationManagementService){
        if(StringUtils.isBlank(equipmentId)){
        	log.info("equipmentId is null!");
        	return null;
        }
            
        List<StationInfo> list = stationManagementService.getStation(equipmentId);
        if(list==null || list.size()==0){
        	log.info("ChannelService map is null!||equipmentId={}",equipmentId);
        	return null;
        }
        StationInfo stationInfo = list.get(0);
        return stationInfo.getChannelService();
    }
    
    public static boolean closeChannel(String equipmentId,StationManagementService stationManagementService){
        if(StringUtils.isBlank(equipmentId)){
        	log.info("equipmentId is null!");
        	return false;
        }
            
        List<StationInfo> list = stationManagementService.getStation(equipmentId);
        if(list==null || list.size()==0){
        	log.info("ChannelService map is null!||equipmentId={}",equipmentId);
        	return false;
        }
        StationInfo stationInfo = list.get(0);
        ChannelService channelService =stationInfo.getChannelService();
        try{
			channelService.getMqttChannel(equipmentId).close();
		}catch(Exception e){
			e.printStackTrace();
		}
        return true;
    }

}
