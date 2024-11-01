package com.me.unicron.iot.message.packer.station.impl;

import java.io.UnsupportedEncodingException;

import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.protocol.CharsetDef;

/**
 * @author lianyadong
 * @func：(CODE=114)充电桩向中心服务器索取对应逻辑服务器信息
 */
public class CMD_114 implements BaseCMD<String> {

    private static final int BODY_LENGTH = 36;
    private static final int CMD_NO = 114;

    @Override
    public byte[] getPayload(String s,MqttNetMsgBase base) {
        int totalLen = base.getLength(BODY_LENGTH);
        byte[] data = new byte[totalLen];
        data=base.fillPayload2CmdNo(data, totalLen, (short) CMD_NO);
        int bytesOffset = base.getBytesOffset();
        
        byte[] reserved = new byte[4];
        System.arraycopy(reserved, 0, data, 8+bytesOffset, 4);

        byte[] station_id_byte = new byte[32];
        System.arraycopy(station_id_byte, 0, data, 12+bytesOffset, 32);

        byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
        System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + BODY_LENGTH, 1);
        return data;
    }

   


    @Override
    public int getCmdNo() {
        // TODO Auto-generated method stub
        return CMD_NO;
    }
    
    public static void print() {

    }

    public static void main(String[] args) throws Exception {
        print();
    }




	@Override
	public String pack(String t,MqttNetMsgBase base) {
		// TODO Auto-generated method stub
		byte[] data = getPayload(t,base);
		String sendMsg="";
		try {
			sendMsg = new String(data,CharsetDef.CHARSET);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sendMsg;
	}

}
