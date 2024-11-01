package com.me.unicron.iot.bootstrap.bean;

import java.util.Date;

import com.me.unicron.iot.enums.ConfirmStatus;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.Builder;
import lombok.Data;

/**
 * mqtt 消息
 *
 * @author lianyadong
 * @create 2023-01-17 19:54
 **/
@Builder
@Data
public class SendMqttMessage {

    private int messageId;

    private Channel channel;

    private volatile ConfirmStatus confirmStatus;

    private long time;

    private byte[]  byteBuf;

    private boolean isRetain;

    private MqttQoS qos;

    private String topic;
    int resendTimes;//Qos1、Qos2重发次数
    Date lastSendTime;
    int cmdno;

}
