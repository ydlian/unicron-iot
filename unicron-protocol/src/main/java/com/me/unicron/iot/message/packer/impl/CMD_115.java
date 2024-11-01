package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import com.me.unicron.iot.message.packer.BaseCMD;
import org.apache.commons.lang.StringUtils;

import com.me.epower.direct.entity.downward.ChargeStatus_115;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.math.NumberUtils;

/**
 * @author lianyadong @func：(CODE=115)
 */
@Slf4j
public class CMD_115 implements BaseCMD<ChargeStatus_115> {

    private static final int BODY_LENGTH = 37;
    public static final int CMD_NO = 115;
    @Override
    public byte[] getPayload(ChargeStatus_115 chargeStatus_115,MqttNetMsgBase base) {
        String equipmentId = chargeStatus_115.getEquipmentId();
        if (StringUtils.isBlank(equipmentId)) {
            return null;
        }

        String gun_no = chargeStatus_115.getGun_no();
        if (StringUtils.isBlank(gun_no) || !NumberUtils.isNumber(gun_no)) {
            return null;
        }

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
        
        byte[] reserved = new byte[4];
        System.arraycopy(reserved, 0, data, 8+bytesOffset, 4);

        //充电桩编码 32
        //byte[] station_id_byte = new byte[32];
        //byte[] equipmentIdByte = EncodeUtil.charsequenceToByte(equipmentId);
        //System.arraycopy(station_id_byte, 0, equipmentIdByte, 0, equipmentIdByte.length);
        //System.arraycopy(station_id_byte, 0, data, 12+bytesOffset, 32);

        //充电桩编码 32
        byte[] station_id_byte = new byte[32];
        if(equipmentId!=null){
            try {
				station_id_byte = equipmentId.getBytes(CharsetDef.CHARSET);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        byte[] byte32 = new byte[32];
        System.arraycopy(station_id_byte, 0, byte32, 0, station_id_byte.length);      
        System.arraycopy(byte32, 0, data, 12+bytesOffset, 32);
        
        //充电枪口 1
        byte[] gun_no_byte = new byte[] { EncodeUtil.ValueToByte(gun_no) };
        System.arraycopy(gun_no_byte, 0, data, 44+bytesOffset, 1);

        byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
        System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + BODY_LENGTH, 1);
        return data;

    }
    @Override
    public String pack(ChargeStatus_115 query,MqttNetMsgBase base) {
        byte[] data = getPayload(query,base);
        try {
            return new String(data, CharsetDef.CHARSET);
        } catch (UnsupportedEncodingException e) {
            log.error("CMD_115 parse error", e);
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

}
