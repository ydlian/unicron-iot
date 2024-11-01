package com.me.unicron.iot.message.decoder;

public interface BaseDecoder {

	//public abstract byte[] getPayload(T t);
	public abstract int getCmdNo(byte[] data);
	//对客户端传入的数据进行合规检查
	public abstract boolean checkClientkMsg(byte[] data);
	public abstract boolean checkClientkMsg(String data);
	public static final String CHARSET="ISO-8859-1";
	byte[] getBodyData(byte[] data);
	
}
