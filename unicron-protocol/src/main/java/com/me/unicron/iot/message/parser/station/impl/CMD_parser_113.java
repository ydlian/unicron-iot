package com.me.unicron.iot.message.parser.station.impl;

import com.me.unicron.iot.message.parser.BaseParser;
import com.me.epower.direct.entity.downward.StationQueryLogicInfoResponse_113;
import com.me.unicron.EncodeUtil;

public class CMD_parser_113 implements BaseParser {
	public static final int CMD_NO = 113;
	public StationQueryLogicInfoResponse_113 unpack(byte[] dataByte){
		StationQueryLogicInfoResponse_113 resp=new StationQueryLogicInfoResponse_113();
		byte[] reserved1=new byte[2];
		System.arraycopy(dataByte, 0, reserved1, 0, 2);
		resp.setReserved1(EncodeUtil.byteToShort(reserved1)+"");
		byte[] reserved2=new byte[2];
		System.arraycopy(dataByte, 2, reserved2, 0, 2);
		resp.setReserved2(EncodeUtil.byteToShort(reserved2)+"");
		
		byte[] ip=new byte[4];
		System.arraycopy(dataByte, 4, ip, 0, 4);
		resp.setIp(EncodeUtil.byteToCharsequence(ip,true));
		
		byte[] port=new byte[4];
		System.arraycopy(dataByte, 8, port, 0, 4);
		resp.setIp(EncodeUtil.byteToCharsequence(port,true));
		
		return resp;
		
	}
}