package com.me.unicron.iot.mqtt;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 抽象出客户端事件
 *
 * @author lianyadong
 * @create 2023-01-03 16:11
 **/
public abstract class ClientMqttHandlerService implements MqttHandlerInterface {

    @Override
    public void doTimeOut(Channel channel, IdleStateEvent evt) {
        heart(channel,evt);
    }

    public abstract void  heart(Channel channel, IdleStateEvent evt);

    public abstract void suback(Channel channel,MqttSubAckMessage mqttMessage) ;

    public abstract void pubBackMessage(Channel channel, int i);

    public abstract void unsubBack(Channel channel, MqttMessage mqttMessage);
}

