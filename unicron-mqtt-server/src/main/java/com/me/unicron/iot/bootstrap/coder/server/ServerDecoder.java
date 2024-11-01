package com.me.unicron.iot.bootstrap.coder.server;

import java.io.UnsupportedEncodingException;

import org.springframework.stereotype.Component;

import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.exception.PayloadDecodeException;
import com.me.unicron.iot.message.decoder.BaseDecoder;
import com.me.unicron.iot.util.ServerDecoderUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ServerDecoder implements BaseDecoder {

	public static final int CMD_8 = 8;
	//
	public static final int CMD_102 = 102;
	public static final int CMD_114 = 114;
	// (CODE=106)充电桩签到信息上报
	public static final int CMD_106 = 106;
	public static final int CMD_6 = 6;
	public static final int CMD_4 = 4;
	public static final int CMD_2 = 2;
	public static final int CMD_12 = 12;
	public static final int CMD_14 = 14;
	public static final int CMD_16 = 16;
	public static final int CMD_18 = 18;
	public static final int CMD_20 = 20;
	public static final int CMD_22 = 22;
	public static final int CMD_24 = 24;
	public static final int CMD_104 = 104;
	public static final int CMD_108 = 108;

	public static final int CMD_110 = 110;
	public static final int CMD_112 = 112;
	public static final int CMD_116 = 116;
	public static final int CMD_118 = 118;
	public static final int CMD_201 = 201;
	public static final int CMD_202 = 202;
	public static final int CMD_206 = 206;
	public static final int CMD_302 = 302;
	public static final int CMD_402 = 402;
	public static final int CMD_408 = 408;
	public static final int CMD_410 = 410;
	public static final int CMD_510 = 510;
	public static final int CMD_802 = 802;
	public static final int CMD_1302 = 1302;

	public static final int CMD_1304 = 1304;

	public static final int CMD_1306 = 1306;

	public static final int CMD_1308 = 1308;

	public static final int CMD_1102 = 1102;

	public static final int CMD_1104 = 1104;

	public static final int CMD_1106 = 1106;

	@Override
	public byte[] getBodyData(byte[] data) {
		// TODO Auto-generated method stub
		// return 2+2+1+1+2+dataLen+1;
		// int cmdLeftLengthAndChecksum=2+2+1+1+2+1;
		int cmdLeftLengthAndChecksum = 2 + 2 + 4 + 4 + 2 + 1;
		int cmdLeftLength = 2 + 2 + 4 + 4 + 2;
		if (data == null || data.length < cmdLeftLengthAndChecksum) {
			return null;
		}

		int bodyLen = data.length - cmdLeftLengthAndChecksum;
		byte bodyData[] = new byte[bodyLen];
		System.arraycopy(data, cmdLeftLength, bodyData, 0, bodyLen);
		return bodyData;
	}

	@Override
	public int getCmdNo(byte[] data) {
		// TODO Auto-generated method stub
		if (data == null || data.length < 8) {
			return -255;
		}
		byte code[] = new byte[2];
		int cmdLeftLengthAndChecksum = 2 + 2 + 4 + 4 + 2 + 1;
		int cmdLeftLength = 2 + 2 + 4 + 4 + 2;

		System.arraycopy(data, cmdLeftLength - 2, code, 0, 2);
		int cmdNo = EncodeUtil.byteToShort(code);
		return cmdNo;
	}

	@Override
	public boolean checkClientkMsg(byte[] data) {
		// TODO Auto-generated method stub
		// log.info("收到不合法的PUBLISH数据:"+data);
		boolean result = false;
		try {
			result = ServerDecoderUtil.checkClientkMsg(data);
		} catch (PayloadDecodeException e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public boolean checkClientkMsg(String data) {
		// TODO Auto-generated method stub
		byte[] decode = null;
		try {
			decode = data.getBytes(CHARSET);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return checkClientkMsg(decode);
	}

}
