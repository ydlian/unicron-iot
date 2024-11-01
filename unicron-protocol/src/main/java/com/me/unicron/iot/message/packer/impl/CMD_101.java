package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.iot.util.Count;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;

/**
 * @author lianyadong
 * @func：(CODE=101)服务器应答心跳包信息
 */
@Slf4j
public class CMD_101 implements BaseCMD<String> {
	private static final int BODY_LENGTH = 6;
	private static final int CMD_NO = 101;
	private static short inc = 0;

	@Override
	public byte[] getPayload(String ob,MqttNetMsgBase base) {
		
		int totalLen = base.getLength(BODY_LENGTH);
		byte[] data = new byte[totalLen];
		data=base.fillPayload2CmdNo(data, totalLen, (short) CMD_NO);
        int bytesOffset = base.getBytesOffset();
        
        /*
		System.arraycopy(MqttNetMsg.getHerder(), 0, data, 0, 2);
		byte[] length = EncodeUtil.shortToByte((short) totalLen);
		System.arraycopy(length, 0, data, 2, 2);
		byte[] ver = MqttNetMsg.getVersion();
		System.arraycopy(ver, 0, data, 4, 1);
		byte[] index = MqttNetMsg.getIndex();
		System.arraycopy(index, 0, data, 5, 1);
		System.arraycopy(EncodeUtil.shortToByte((short) CMD_NO), 0, data, 6, 2);
		*/

		byte[] heartBeat1 = new byte[4];
		System.arraycopy(heartBeat1, 0, data, 8+bytesOffset, 4);
		inc = Count.getShortCount();
		byte[] heartBeat2 = EncodeUtil.shortToByte(inc);
		System.arraycopy(heartBeat2, 0, data, 12+bytesOffset, 2);
		byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
		System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + BODY_LENGTH, 1);
		return data;
	}

	@Override
	public String pack(String ob,MqttNetMsgBase base) {
		// TODO Auto-generated method stub
		byte[] sendBytes = getPayload(ob,base);
		// byte[] sendBytes= sendString .getBytes("UTF8");
		String sendMsg = "";
		try {
			
			sendMsg = new String(sendBytes, CharsetDef.CHARSET);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sendMsg;
	}

	
	@Override
	public int getCmdNo() {
		// TODO Auto-generated method stub
		return CMD_NO;
	}

	

	public static void print() {
		CMD_101 cmd = new CMD_101();
		//编码前：
		byte[] byt = cmd.getPayload(null,new MqttNetMsg());
		log.info("编码前：");
		EncodeUtil.print(byt);
		String msg = cmd.pack(null,new MqttNetMsg());
		try {
			//编码后
			log.info("编码后：");
			byte[] decode = msg.getBytes(CharsetDef.CHARSET);
			EncodeUtil.print(decode);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(msg);
	}

	public static void main(String[] args) throws Exception {
		print();

	}

}
