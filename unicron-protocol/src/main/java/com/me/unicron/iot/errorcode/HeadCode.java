package com.me.unicron.iot.errorcode;

public class HeadCode {

	//起始域相关说明
	//起始域定义：用于识别供应商，不同的供应商根据其与XX的约定确定此域的数值
	//桩端软件开发联调通过后，上线时由XX分配起始域，中恒，特锐德：7dd9，...
	//联调测试阶段，桩端统一使用：7ddf
	//服务器返回的数据报文起始域统一为7dd0
	public static final String SERVER="0x7dd0";
	public static final String CLIENT_ZHICHONG="0x7ddf";
	public static final String CLIENT_ZHONGHENG="0x7dd8";
	public static final String CLIENT_TERUIDE="0x7dd9";
	public static final String CLIENT_ENT_A="0x7dda";
	public static final String CLIENT_ENT_B="0x7ddb";
	public static final String CLIENT_ENT_C="0x7ddc";
	
}
