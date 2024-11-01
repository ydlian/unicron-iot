package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import com.me.epower.direct.entity.downward.CleanRequest_19;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class CMD_19 implements BaseCMD<CleanRequest_19> {

    // 默认为查询时长度
    private static final int BODY_LENGTH = 164;
    private static final int CMD_NO = 19;
    @Override
    public byte[] getPayload(CleanRequest_19 cleanRequest_19,MqttNetMsgBase base) {
        int totalLen = base.getLength(BODY_LENGTH);
        //起始域
        byte[] data = new byte[totalLen];
        data=base.fillPayload2CmdNo(data, totalLen, (short) CMD_NO);
        int bytesOffset = base.getBytesOffset();
        /*
        System.arraycopy(MqttNetMsg.getHerder(), 0, data, 0, 2);
        //长度域
        byte[] length = EncodeUtil.shortToByte((short) totalLen);
        System.arraycopy(length, 0, data, 2, 2);
        //版本域
        byte[] ver = MqttNetMsg.getVersion();
        System.arraycopy(ver, 0, data, 4, 1);
        //序列号域
        byte[] index = MqttNetMsg.getIndex();
        System.arraycopy(index, 0, data, 5, 1);
        //命令域
        System.arraycopy(EncodeUtil.shortToByte((short) CMD_NO), 0, data, 6, 2);
        */

        //body部分，4字节保留
        byte[] reserved = new byte[4];
        System.arraycopy(reserved, 0, data, 8+bytesOffset, 4);

        String equipmentId = cleanRequest_19.getEquipmentId();
        byte[] cardNumBytes;
        try {
            cardNumBytes = equipmentId.getBytes(CharsetDef.CHARSET);
            byte[] byte32 = new byte[32];
            System.arraycopy(cardNumBytes, 0, byte32, 0, cardNumBytes.length);
            System.arraycopy(byte32, 0, data, 12+bytesOffset, 32);

            String clean_no = cleanRequest_19.getClean_no();
            byte[] byte16 = new byte[128];
            for(int i=0;i<128;i++){
                if('1'==clean_no.charAt(i)){
                    byte16[i]=1;
                }
            }
            
            
            System.arraycopy(byte16, 0, data, 44+bytesOffset, 128);
            byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
            System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + BODY_LENGTH, 1);
        } catch (UnsupportedEncodingException e) {
            log.error("CMD_19 error", e);
        }
        return data;
    }

    @Override
	public String pack(CleanRequest_19 t,MqttNetMsgBase base) {
		// TODO Auto-generated method stub
		byte[] data = getPayload(t,base);
        try {
            return new String(data, CharsetDef.CHARSET);
        } catch (UnsupportedEncodingException e) {
            log.error("CMD_115 parse error", e);
        }
        return null;
	}


    @Override
    public int getCmdNo() {
        // TODO Auto-generated method stub
        return CMD_NO;
    }

}
