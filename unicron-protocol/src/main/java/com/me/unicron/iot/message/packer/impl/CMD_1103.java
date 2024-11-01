package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.epower.direct.entity.downward.FileVersion_1103;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_1103 implements BaseCMD<FileVersion_1103> {

    private static final int BODY_LENGTH = 2;
    private static final int CMD_NO = 1103;

    @Override
    public byte[] getPayload(FileVersion_1103 fileVersion_1103,MqttNetMsgBase base) {
        int totalLen = base.getLength(BODY_LENGTH);
        byte[] data = new byte[totalLen];
        try {
        	
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

            byte[] type = new byte[] { EncodeUtil.ValueToByte(fileVersion_1103.getType()) };
            System.arraycopy(type, 0, data, 8+bytesOffset, 1);
            byte[] equals = new byte[] { EncodeUtil.ValueToByte(fileVersion_1103.getEquals()) };
            System.arraycopy(equals, 0, data, 9+bytesOffset, 1);

            byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
            System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + BODY_LENGTH, 1);
            return data;
        } catch (Exception e) {
            log.error("CMD_1103 error", e);
        }
        return data;
    }


    @Override
    public int getCmdNo() {
        // TODO Auto-generated method stub
        return CMD_NO;
    }


	@Override
	public String pack(FileVersion_1103 t,MqttNetMsgBase base) {
		// TODO Auto-generated method stub
		byte[] data = getPayload(t,base);
        try {
            return new String(data, CharsetDef.CHARSET);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
	}

}
