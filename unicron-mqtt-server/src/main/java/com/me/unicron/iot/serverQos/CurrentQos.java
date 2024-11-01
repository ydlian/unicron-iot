package com.me.unicron.iot.serverQos;

import com.me.unicron.common.server.ServerList;
import com.me.unicron.iot.ip.IpUtils;

import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class CurrentQos {
	public static MqttQoS QoS=MqttQoS.AT_MOST_ONCE;
	
	public static MqttQoS QoS0=MqttQoS.AT_MOST_ONCE;
	public static MqttQoS QoS1=MqttQoS.AT_LEAST_ONCE;
	public static MqttQoS QoS2=MqttQoS.EXACTLY_ONCE;
	public static MqttQoS getQos(){
		String ip = IpUtils.getHost();
		boolean result=false;
		if (ip.equals(ServerList.logicServer1)){
			result=true;
		}
		if(result){
			return QoS2;
		}
		return QoS2;
			
	}
}
