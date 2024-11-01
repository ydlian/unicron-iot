package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;

import com.me.epower.direct.entity.downward.RecentOrderQuery_205;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.math.NumberUtils;

/**
 * @author lianyadong @func：(CODE=115)
 */
@Slf4j
public class CMD_205 implements BaseCMD<RecentOrderQuery_205> {

    private static final int BODY_LENGTH = 37;
    private static final int CMD_NO = 205;
    @Override
    public byte[] getPayload(RecentOrderQuery_205 recentOrderQuery_205,MqttNetMsgBase base) {
        String equipmentId = recentOrderQuery_205.getCardNum();
        if (StringUtils.isBlank(equipmentId)) {
            return null;
        }

        String gun_no = recentOrderQuery_205.getPortId();
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

        //充电枪口 1
        byte[] gun_no_byte = new byte[] { EncodeUtil.ValueToByte(gun_no) };
        System.arraycopy(gun_no_byte, 0, data, 12+bytesOffset, 1);
        
        //充电桩编码 32
        byte[] station_id_byte = new byte[32];
        byte[] equipmentIdByte = EncodeUtil.charsequenceToByte(equipmentId);
        System.arraycopy(equipmentIdByte, 0, station_id_byte, 0, equipmentIdByte.length);
        System.arraycopy(station_id_byte, 0, data, 13+bytesOffset, 32);


        byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
        System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + BODY_LENGTH, 1);
        return data;

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
	public String pack(RecentOrderQuery_205 t,MqttNetMsgBase base) {
		// TODO Auto-generated method stub
		byte[] data = getPayload(t,base);
        try {
            return new String(data, CharsetDef.CHARSET);
        } catch (UnsupportedEncodingException e) {
            log.error("CMD_201 pack error", e);
        }
        return null;
	}

}
