package com.me.unicron.iot.centerserver;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.me.epower.direct.entity.ServerInfo;
import com.me.epower.direct.repositories.ServerInfoRepository;
import com.me.unicron.iot.properties.InitBean;
import com.me.unicron.station.service.IPreReleaseService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ServerPoolService {

	private static final String LOGIC_SERV_DOMAIN="..com";
	private static final int LOGIC_SERV_PORT=1884;
	
	private static final String PRE_LOGIC_SERV_DOMAIN="-test..com";
	private static final int PRE_LOGIC_SERV_PORT=1888;
	
	
	@Autowired
    private ServerInfoRepository serverInfoRepository;

	@Autowired
    IPreReleaseService iPreReleaseService;
	
	public ServerInfo getUseableLogicServer(String stationEquipId) {
		ServerInfo server = new ServerInfo();
		server.setIp(LOGIC_SERV_DOMAIN);
		server.setPort(LOGIC_SERV_PORT);
		
		List<ServerInfo> listTemp =new ArrayList<ServerInfo>();
    	try{
    		List<ServerInfo> list = serverInfoRepository.findUseableServer();
    		if(list ==null || list.size()<=0){
    			return server;
            }
            for (ServerInfo aServ : list) {
                if (aServ.getConnectionLoadPercent() < 100) {
                	listTemp.add(aServ);
                }

            }
            if(listTemp ==null || listTemp.size()<=0){
    			return server;
            }
            
    	}catch(Exception e){
    		e.printStackTrace();
    	}

    	String whiteList=iPreReleaseService.getPreReleaseWhiteList();
    	if(StringUtils.isNotEmpty(whiteList)){
    		
    		log.info("getUseableLogicServer:white list={}",whiteList);
    		if(!StringUtils.isBlank(whiteList) && whiteList.contains(stationEquipId+",")){
    			server.setIp(PRE_LOGIC_SERV_DOMAIN);
    			server.setPort(PRE_LOGIC_SERV_PORT);
    			log.info("Station equipment={} in white list={},set logic server={}",stationEquipId,whiteList,PRE_LOGIC_SERV_DOMAIN);
    			return server;
    		}
    	}else{
    		log.info("getUseableLogicServer:white list is null");
    	}
    	log.info("equipment={},logicServer={}",stationEquipId,JSONObject.toJSON(server));
        return server;
        
        
    }
	
	
}
