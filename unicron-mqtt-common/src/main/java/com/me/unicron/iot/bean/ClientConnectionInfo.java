package com.me.unicron.iot.bean;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.fastjson.JSONObject;

import io.netty.channel.Channel;
import lombok.Data;

@Data
public class ClientConnectionInfo implements Serializable {

	private static final long serialVersionUID = -2751394300700288904L;
	String connectDateTime;
	String ipPort;//IP和端口，ip:port	格式,客户端的IP
	String ip;//
	String localServerIp;//服务器IP
	Channel channel;
	String deviceID;
	String protocolVer;//通信协议版本
	String clientPublicKey;//RSA密钥
	String serverPublicKey;
	String serverPrivateKey;
	boolean authCheckResult=false;
	boolean repeatConnection=false;
	boolean isOnline=false;
	String userName;//连接的用户名
	Date lastLiveTime;//最后一次联系时间
	
	String stationSoftwareVersion="未知";//
	long stationLocalTime=0;//充电桩本地时间，毫秒时间格式
	long msecondOffset;//服务器时间-桩时间偏移量
	
	@Override
	public String toString(){
		return JSONObject.toJSONString(this);
		
	}
	
}
