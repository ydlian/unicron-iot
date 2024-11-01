package com.me.unicron.iot.message.packer;

import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;

public interface BaseCMD<T> {

	public abstract byte[] getPayload(T t,MqttNetMsgBase base);
	public abstract String pack(T t,MqttNetMsgBase base);
	public abstract int getCmdNo();
	
	
}
