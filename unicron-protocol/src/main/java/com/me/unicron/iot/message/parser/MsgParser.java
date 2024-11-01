package com.me.unicron.iot.message.parser;

import com.me.unicron.EncodeUtil;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttMessage;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class MsgParser {

	public static byte[] parseBytes(MqttMessage mqttMessage){
		MqttMessage ms = mqttMessage;
		if(ms.payload()!=null){
			try{
				ByteBuf buf =  (ByteBuf)ms.payload();
				
				int size=buf.readableBytes();
				byte[] req = new byte[size];
				log.info("[Payload]可读取的字节数："+size);
				buf.readBytes(req);
				
				EncodeUtil.print(req);
				EncodeUtil.printHex(req);
				return req;
			}catch(Exception e){
				e.printStackTrace();
				log.info("数据解包失败：{}",mqttMessage);
				return null;
			}
			
		}else{
			log.info("parseBytes:payload is null!");
		}
		return null;
	}
}
