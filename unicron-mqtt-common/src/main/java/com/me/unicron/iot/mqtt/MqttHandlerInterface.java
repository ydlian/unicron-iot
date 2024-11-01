package com.me.unicron.iot.mqtt;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 自定义 对外暴露消息处理api
 *
 * @author lianyadong
 * @create 2023-11-21 9:53
 **/
public interface MqttHandlerInterface {

	
	
    void close(Channel channel);

    void puback(Channel channel, MqttMessage mqttMessage);

    void pubrec(Channel channel, MqttMessage mqttMessage);

    void pubrel(Channel channel, MqttMessage mqttMessage);

    void pubcomp(Channel channel, MqttMessage mqttMessage);

    void doTimeOut(Channel channel, IdleStateEvent evt);

	


}
