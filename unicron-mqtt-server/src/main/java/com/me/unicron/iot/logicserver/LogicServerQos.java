package com.me.unicron.iot.logicserver;

import org.springframework.stereotype.Component;

import io.netty.handler.codec.mqtt.MqttQoS;
@Component
public class LogicServerQos {
	private MqttQoS qos=MqttQoS.AT_MOST_ONCE;

	public MqttQoS getQos() {
		return qos;
	}

	public void setQos(MqttQoS qos) {
		this.qos = qos;
	}
	
}
