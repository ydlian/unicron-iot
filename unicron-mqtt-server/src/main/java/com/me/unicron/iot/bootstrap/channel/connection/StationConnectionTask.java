package com.me.unicron.iot.bootstrap.channel.connection;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.me.epower.direct.entity.constant.RedisConstant;
import com.me.unicron.date.DateUtils;
import com.me.unicron.iot.bean.ClientConnectionInfo;
import com.me.unicron.iot.ip.IpUtils;
import com.me.unicron.iot.mqtt.ClientConnectionService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StationConnectionTask {

//	@Autowired
//	private IStationService iStationService;
	@Autowired
	ClientConnectionService clientConnectionService;

	@Autowired
    private StringRedisTemplate stringRedisTemplate;
	//private int local_cnt=0;
	//private final static long MAX_TIME_SPAN_MSEC=6*60*1000;//十分钟无心跳则设备离线
	
	private final static long MAX_LOGIN_DELAY_TIME_SPAN_SEC=(long) (4.5*60);//N分钟无通讯数据往来，判定设备离线
	
	private final static String GLOBLE_CONNECT_LIST_KEY = RedisConstant.UNICRON_GLOBLE_CONNECT_LIST;
	
	public boolean isLoginConnectionLive(String deviceId){
		boolean isLive=false;
		//String cacheKey = GLOBLE_CONNECT_LIST_KEY;
		HashOperations<String, String, String> vo = stringRedisTemplate.opsForHash();
		String data=vo.get(GLOBLE_CONNECT_LIST_KEY, deviceId);
		if(data!=null){
			log.info("isLoginConnectionLive:deviceId={}||status=online||info={}",deviceId,data);
			isLive=true;
		}else{
			log.info("isLoginConnectionLive:deviceId={}||status=offline",deviceId);
		}

		return isLive;
	}

	// Collection<MqttChannel> subChannels = getChannels(topic, topic1 ->
	// cacheMap.getData(getTopic(topic1)));
	// 执行频率：1分钟一次
	@Scheduled(cron = "0 0/1 * * * ?")
	public void updateThisNodeConnectionStatus() {
		ConcurrentHashMap<String, ClientConnectionInfo> allChannels = ClientConnectionService.getOnlineChannels();
		
		//String cacheKey = GLB_CONN_CACHE_KEY;
		HashOperations<String, String, String> vo = stringRedisTemplate.opsForHash();
		
		if (allChannels != null) {
			for(String deviceId:allChannels.keySet()){
				ClientConnectionInfo client=allChannels.get(deviceId);
				client.setLastLiveTime(DateUtils.getCurDateTime());
			}
		}
		
		Map<String,String> glbConnMap=vo.entries(GLOBLE_CONNECT_LIST_KEY);
		for(String deviceId:glbConnMap.keySet()){
			String data=glbConnMap.get(deviceId);
			try{
				ClientConnectionInfo client=JSON.parseObject(data, ClientConnectionInfo.class);
				Date oldDate=client.getLastLiveTime();
				if(StringUtils.isBlank(data) || client==null || oldDate==null){
					continue;
				}
				Date nowDate=DateUtils.getCurDateTime();
				if(Math.abs(DateUtils.getIntervalSecond(oldDate,nowDate))>=MAX_LOGIN_DELAY_TIME_SPAN_SEC){
					//上次心跳距离现在太久了,判定通信链路不通，设备按照离线处理
					vo.delete(GLOBLE_CONNECT_LIST_KEY, deviceId);
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
		//String result=String.valueOf(vo.entries(GLOBLE_CONNECT_LIST_KEY));
		log.info("_com_unicron_logic_server||traceid={}||updateThisNodeConnectionStatus:连接数=[{}]",IpUtils.getHost(),vo.entries(GLOBLE_CONNECT_LIST_KEY).size());
	}
}
