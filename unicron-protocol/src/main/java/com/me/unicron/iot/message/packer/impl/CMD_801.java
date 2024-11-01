package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import org.hibernate.type.descriptor.java.CalendarDateTypeDescriptor;

import com.me.epower.direct.entity.downward.AuthRequest_801;
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
public class CMD_801 implements BaseCMD<AuthRequest_801> {

    private static final int CMD_NO = 801;

    @Override
    public byte[] getPayload(AuthRequest_801 authRequest_801, MqttNetMsgBase base) {
        try {

            String encodeData = authRequest_801.getEncodeData();
            byte[] charsequenceToByte = EncodeUtil.charsequenceToByte(encodeData);

            int dataLength = Integer.parseInt(authRequest_801.getEncodeDataLength());
            int BODY_LENGTH = 43 + dataLength;
            int totalLen = base.getLength(BODY_LENGTH);

            //起始域
            byte[] data = new byte[totalLen];
            
            data = base.fillPayload2CmdNo(data, totalLen, (short) CMD_NO);
            int bytesOffset = base.getBytesOffset();
            System.arraycopy(EncodeUtil.intToByte(charsequenceToByte.length), 0, data, 8 + bytesOffset, 4);
            
            
            System.arraycopy(charsequenceToByte, 0, data, 12 + bytesOffset, charsequenceToByte.length);


            String equipmentId = authRequest_801.getEquipmentId();
            byte[] equipmentIdByte = EncodeUtil.charsequenceToByte(equipmentId);
            byte[] equipmentByte = new byte[32];
            System.arraycopy(equipmentIdByte, 0, equipmentByte, 0, equipmentIdByte.length);
            System.arraycopy(equipmentByte, 0, data, 12 + bytesOffset + charsequenceToByte.length, 32);

            String encodeType = authRequest_801.getEncodeType();
            byte[] shortBytes = EncodeUtil.shortToByte(Short.parseShort(encodeType));
            System.arraycopy(shortBytes, 0, data, 44 + bytesOffset + charsequenceToByte.length, 2);

            Calendar now = Calendar.getInstance();
            byte[] yearBytes = EncodeUtil.shortToByte((short)now.get(Calendar.YEAR));
            System.arraycopy(yearBytes, 0, data, 46 + bytesOffset + charsequenceToByte.length, 2);
           
            byte[] monthBytes= new byte[]{(byte) (now.get(Calendar.MONTH)+1)};
            System.arraycopy(monthBytes, 0, data, 48 + bytesOffset + charsequenceToByte.length, 1);
            
            byte[] dayBytes= new byte[]{(byte) now.get(Calendar.DAY_OF_MONTH)};
            System.arraycopy(dayBytes, 0, data, 49 + bytesOffset + charsequenceToByte.length, 1);
            
            byte[] seqBytes= new byte[]{(byte)0x01};
            System.arraycopy(seqBytes, 0, data, 50 + bytesOffset + charsequenceToByte.length, 1);
            
            byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
            System.arraycopy(checkSum, 0, data, 51 + bytesOffset + charsequenceToByte.length, 1);
            return data;

        } catch (Exception e) {
            log.error("CMD_801_ERROR", e);
        }
        return null;
    }

    @Override
    public int getCmdNo() {
        return CMD_NO;
    }

    public static void print() {

    }

    public static void main(String[] args) throws Exception {
        print();

    }

    @Override
    public String pack(AuthRequest_801 t, MqttNetMsgBase base) {
        byte[] data = getPayload(t, base);
        try {
            return new String(data, CharsetDef.CHARSET);
        } catch (UnsupportedEncodingException e) {
            log.error("pack CMD_801_ERROR", e);
        }
        return null;
    }

}
