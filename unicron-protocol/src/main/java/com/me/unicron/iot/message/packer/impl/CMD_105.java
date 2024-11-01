package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;
// (CODE=105)服务器应答充电桩签到信息

/**
 * @author lianyadong
 * @func：(CODE=105)服务器应答充电桩签到信息
 */
@Slf4j
public class CMD_105 implements BaseCMD<Long> {

    private static final int BODY_LENGTH = 4;
    private static final int CMD_NO = 105;

    @Override
    public byte[] getPayload(Long msecondOffset ,MqttNetMsgBase base) {
        int totalLen = base.getLength(BODY_LENGTH);
        byte[] data = new byte[totalLen];
        data=base.fillPayload2CmdNo(data, totalLen, (short) CMD_NO);
        int bytesOffset = base.getBytesOffset();
       
        
        byte[] signupByte = new byte[4];
        int secOffset=(int) (msecondOffset/1.0);//附带存储时间偏移量
        signupByte=EncodeUtil.intToByte(secOffset);
        System.arraycopy(signupByte, 0, data, 8+bytesOffset, 4);
        byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
        System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + BODY_LENGTH, 1);
        return data;
    }

    @Override
    public String pack(Long msecondOffset,MqttNetMsgBase base) {
        // TODO Auto-generated method stub
        byte[] data = getPayload(msecondOffset,base);
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

    public static void print() {

    }

    public static void main(String[] args) throws Exception {
        print();
    }

}
