package com.me.unicron.iot.bootstrap;

import com.me.unicron.iot.bootstrap.bean.MqttChannel;
import com.me.unicron.iot.bootstrap.bean.WillMeaasge;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;

import java.util.List;
import java.util.Set;

/**
 * 消息处理
 *
 * @author lianyadong
 * @create 2023-11-23 11:38
 **/
public interface ChannelService {

    MqttChannel getMqttChannel(String deviceId);
    boolean addMqttChannel(String deviceId,MqttChannel mqttChannel);
    public void deleteMqttChannel(MqttChannel mqttChannel);
    
    boolean connectSuccess(String s, MqttChannel build);


    void suscribeSuccess(String deviceId, Set<String> topics);


    void loginSuccess(Channel channel, String deviceId, MqttConnectMessage mqttConnectMessage);

    void publishSuccess(Channel channel, MqttPublishMessage mqttPublishMessage);

    void closeSuccess(String deviceId,boolean isDisconnect);

    void sendWillMsg(WillMeaasge willMeaasge);

    String  getDeviceId(Channel channel);

    void unsubscribe(String deviceId, List<String> topics1);

    void  doPubrel(Channel channel, int mqttMessage);

    void  doPubrec(Channel channel, int mqttMessage);
    
    void  push(String topic,MqttQoS qos, byte[] bytes);

}
