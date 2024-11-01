package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;

/**
 * @author fzl func：(CODE=401)服务器应答充电桩未上传历史充电记录
 */
@Slf4j
public class CMD_407 implements BaseCMD<String> {

    private static final int BODY_LENGTH = 37;
    private static final int CMD_NO = 407;

    @Override
    public byte[] getPayload(String equipmentId,MqttNetMsgBase base) {
        int totalLen = base.getLength(BODY_LENGTH);
        //起始域
        byte[] data = new byte[totalLen];
        data=base.fillPayload2CmdNo(data, totalLen, (short) CMD_NO);
        int bytesOffset = base.getBytesOffset();

        try {
            //保留4字节
            byte[] replyChargeStatus1 = new byte[4];
            System.arraycopy(replyChargeStatus1, 0, data, 8+bytesOffset, 4);
            //设备号
            byte[] equipmentIdByte = EncodeUtil.charsequenceToByte(equipmentId);
            byte[] byte32 = new byte[32];
            System.arraycopy(equipmentIdByte, 0, byte32, 0, equipmentIdByte.length);
            System.arraycopy(byte32, 0, data, 12+bytesOffset, 32);

            byte[] replyChargeStatus2 = new byte[] { EncodeUtil.ValueToByte("1") };
            System.arraycopy(replyChargeStatus2, 0, data, 44+bytesOffset, 1);

        } catch (Exception e) {
            log.error("CMD_407_ERROR", e);
        }
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
        try {
            return new String(data, CharsetDef.CHARSET);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
