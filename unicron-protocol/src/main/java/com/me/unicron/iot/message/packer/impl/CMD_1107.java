package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import com.me.epower.direct.entity.downward.FileVersionQuery_1107;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author fzl
 *
 */
@Slf4j
public class CMD_1107 implements BaseCMD<FileVersionQuery_1107> {

    private static final int BODY_LENGTH = 1;
    private static final int CMD_NO = 1107;
    @Override
    public byte[] getPayload(FileVersionQuery_1107 fileVersionQuery_1107,MqttNetMsgBase base) {
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

            String type = fileVersionQuery_1107.getType();
            byte[] byt_type=new byte[]{(byte)Integer.parseInt(type)};
            System.arraycopy(byt_type, 0, data, 8+bytesOffset, 1);
            
            byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
            System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + BODY_LENGTH, 1);
            return data;
        } catch (Exception e) {
            log.error("CMD_1107 error", e);
        }
        return data;
    }

   
    @Override
    public String pack(FileVersionQuery_1107 t,MqttNetMsgBase base) {
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

    @Override
    public int getCmdNo() {
        // TODO Auto-generated method stub
        return CMD_NO;
    }

}
