package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.epower.direct.entity.downward.LogRequest_409;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_409 implements BaseCMD<LogRequest_409> {

    // 默认为查询时长度
    private static final int BODY_LENGTH = 164;
    private static final int CMD_NO = 409;

    @Override
    public byte[] getPayload(LogRequest_409 logRequest_409,MqttNetMsgBase base) {
        int totalLen = base.getLength(BODY_LENGTH);
        //起始域
        byte[] data = new byte[totalLen];
        data=base.fillPayload2CmdNo(data, totalLen, (short) CMD_NO);
        int bytesOffset = base.getBytesOffset();

        //body部分，4字节保留
        byte[] reserved = new byte[4];
        System.arraycopy(reserved, 0, data, 8+bytesOffset, 4);

        String equipmentId = logRequest_409.getEquipmentId();
        byte[] cardNumBytes;
        try {
            cardNumBytes = equipmentId.getBytes(CharsetDef.CHARSET);
            byte[] byte32 = new byte[32];
            System.arraycopy(cardNumBytes, 0, byte32, 0, cardNumBytes.length);
            System.arraycopy(byte32, 0, data, 12+bytesOffset, 32);

            String filename = logRequest_409.getFilename();
            byte[] filenameBytes= filename.getBytes(CharsetDef.CHARSET);
            byte[] filenameBytes128 = new byte[128];
            System.arraycopy(filenameBytes, 0, filenameBytes128, 0, filenameBytes.length);
            System.arraycopy(filenameBytes128, 0, data, 44+bytesOffset, 128);

            byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
            System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + BODY_LENGTH, 1);
        } catch (UnsupportedEncodingException e) {
            log.error("CMD_409 error", e);
        }
        return data;
    }

    @Override
    public int getCmdNo() {
        // TODO Auto-generated method stub
        return CMD_NO;
    }

    @Override
    public String pack(LogRequest_409 t,MqttNetMsgBase base) {
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
