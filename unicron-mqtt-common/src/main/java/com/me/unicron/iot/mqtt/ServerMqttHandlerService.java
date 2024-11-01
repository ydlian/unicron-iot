package com.me.unicron.iot.mqtt;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 抽象出服务端事件
 *
 * @author lianyadong
 * @create 2023-01-03 16:11
 **/
public abstract class ServerMqttHandlerService implements MqttHandlerInterface {

	
	public abstract void doLogic(Channel channel, MqttMessage mqttMessage);
	
    public abstract boolean login(Channel channel, MqttConnectMessage mqttConnectMessage);

    public abstract  void  publish(Channel channel, MqttPublishMessage mqttPublishMessage);

    public abstract void subscribe(Channel channel, MqttSubscribeMessage mqttSubscribeMessage);
    

    public abstract void pong(Channel channel);

    public abstract  void unsubscribe(Channel channel, MqttUnsubscribeMessage mqttMessage);


    public abstract void disconnect(Channel channel);

    public abstract void doTimeOut(Channel channel, IdleStateEvent evt);
}

