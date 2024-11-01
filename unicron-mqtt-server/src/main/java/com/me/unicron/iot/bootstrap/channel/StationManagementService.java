package com.me.unicron.iot.bootstrap.channel;

import java.util.List;

import org.springframework.stereotype.Component;

import com.me.unicron.iot.bootstrap.bean.StationInfo;
import com.me.unicron.iot.bootstrap.channel.cache.CacheMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StationManagementService {
	protected static CacheMap<String,StationInfo> cacheMap= new CacheMap<>();
	protected  static char SPLITOR = '/';

    protected String[] getTopic(String stationEquipId)  {
        //return StringUtils.split(stationEquipId,SPLITOR);
    		return new String[]{stationEquipId};
    }
    
	public static CacheMap<String, StationInfo> getCacheMap() {
		return cacheMap;
	}

	protected boolean deleteStation(String stationEquipId,StationInfo station){
        
        return  cacheMap.delete(getTopic(stationEquipId),station);
    }
	
	//根据设备ID删除对应的通道映射信息，重连时桩id对应的channel已经关闭，
	//但是在map中存在垃圾映射信息,按需调用做清理 2023-03-09
	public boolean deleteStation(String stationEquipId){
		if(stationEquipId==null){
			return false;
		}
		List<StationInfo> list=getStation(stationEquipId);
		if(list==null || list.size()==0){
			return false;
		}
		for(StationInfo station:list){
			cacheMap.delete(getTopic(stationEquipId),station);
		}
		return true;
    }

    protected boolean addStation(String stationEquipId,StationInfo station)
    {
        
        return  cacheMap.putData(getTopic(stationEquipId),station);
    }
    
    public List<StationInfo> getStation(String stationEquipId)
    {
        
        return  cacheMap.getData(getTopic(stationEquipId));
    }
    
    public StationInfo updateStation(String stationEquipId,StationInfo station)
    {
    		if(stationEquipId == null || getTopic(stationEquipId)==null){
    			log.info("null参数，updateStation跳过");
    			return station;
    		}
    		List<StationInfo>  list=cacheMap.getData(getTopic(stationEquipId));
    		
    		StationInfo theStation=null;
    		if(list!=null && list.size()>0){
    			StationInfo oldStation=list.get(0);
    			theStation = oldStation;
    			if(station!=null){
    				deleteStation(stationEquipId,oldStation);
    				theStation.setStationOnlineStatus(station.isStationOnlineStatus());
    				theStation.setLastAliveSignalTime(station.getLastAliveSignalTime());
    				addStation(stationEquipId,theStation);
    			}
    		}
    		
        return  theStation;
    }
}
