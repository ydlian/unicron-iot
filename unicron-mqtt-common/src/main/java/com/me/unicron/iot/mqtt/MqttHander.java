package com.me.unicron.iot.mqtt;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * mqtt协议处理器
 *
 * @author lianyadong
 * @create 2023-11-20 13:38
 **/
@Slf4j
public abstract class MqttHander extends SimpleChannelInboundHandler<MqttMessage> {

	
	
	MqttHandlerInterface mqttHandlerApi;

	public MqttHander(MqttHandlerInterface mqttHandlerIntf) {
		this.mqttHandlerApi = mqttHandlerIntf;
	}

	// 成功事件 2023-02-22 add
	/*
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		log.info("ctx={}",JSONObject.toJSON(ctx));
		log.info("【建立了新连接：MqttHander：channelActive】" + ctx.channel().remoteAddress().toString() + "链接成功");
		super.channelActive(ctx);
	}*/

	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, MqttMessage mqttMessage) throws Exception {
		MqttFixedHeader mqttFixedHeader = mqttMessage.fixedHeader();
		//log.info("####[channelRead0]####,{}",JSONObject.toJSON(mqttMessage));
		
		Optional.ofNullable(mqttFixedHeader)
				.ifPresent(mqttFixedHeader1 -> doMessage(channelHandlerContext, mqttMessage));
	}

	public abstract void doMessage(ChannelHandlerContext channelHandlerContext, MqttMessage mqttMessage);

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		//log.info("【MqttHander：channelInactive】" + ctx.channel().localAddress().toString() + "关闭成功");
		
		mqttHandlerApi.close(ctx.channel());
		//add by 2023-04-10
		//ctx.disconnect();
		super.channelInactive(ctx);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

		if (evt instanceof IdleStateEvent) {
			log.info("【处理用户事件】userEventTriggered：");
			mqttHandlerApi.doTimeOut(ctx.channel(), (IdleStateEvent) evt);
		}
		super.userEventTriggered(ctx, evt);
	}

}
