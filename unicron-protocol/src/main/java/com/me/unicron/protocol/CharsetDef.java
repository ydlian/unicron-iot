package com.me.unicron.protocol;

public class CharsetDef {
	/*
	 * 说明： 在网络传输或其它应用中常常有同一的中间件，假设为String类型。因此需要把其它类型的数据转换为中间件的类型。
	 * 将字符串进行网络传输时，如socket，需要将其在转换为byte[]类型。这中间如果采用用不同的编码可能会出现未预料的问题，
	 * 如乱码。
	 */
	//接收方按照单字节解码
	public static final String CHARSET="ISO-8859-1";
	//发送时，utf8编码发送
	public static final String NETTY_CHARSET_UTF8="UTF-8";
}
