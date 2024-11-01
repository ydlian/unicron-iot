package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;

import com.me.epower.direct.entity.downward.StationLocalLogQuery_111;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.exception.MqttParaException;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.protocol.CharsetDef;

import net.logstash.logback.encoder.org.apache.commons.lang.math.NumberUtils;

/**
 * @author lianyadong
 * @func：(CODE=111)服务器查询充电桩本地日志数据
 */
public class CMD_111 implements BaseCMD<StationLocalLogQuery_111> {

    private static final int BODY_LENGTH = 53;
    private static final int CMD_NO = 111;

    @Override
    public byte[] getPayload(StationLocalLogQuery_111 query,MqttNetMsgBase base) {
        String station_id = query.getStationId();
        if (StringUtils.isBlank(station_id)) {
            throw new MqttParaException("station_id not set!");
        }

        String gun_no = query.getGun_no();
        if (StringUtils.isBlank(gun_no) || !NumberUtils.isNumber(gun_no)) {
            throw new MqttParaException("gun_no not set!");
        }

        String start_time = query.getStart_time();
        if (StringUtils.isBlank(start_time)) {
            throw new MqttParaException("start_time not set!");
        }

        String end_time = query.getEnd_time();
        if (StringUtils.isBlank(end_time)) {
            throw new MqttParaException("end_time not set!");
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
        // byte[] code=MqttNetMsg.getCmdCode();
        System.arraycopy(EncodeUtil.shortToByte((short) CMD_NO), 0, data, 6, 2);
        */
        
        byte[] reserved = new byte[4];
        System.arraycopy(reserved, 0, data, 8+bytesOffset, 4);

        //充电桩编码 32
        byte[] station_id_byte = new byte[32];
        System.arraycopy(station_id_byte, 0, data, 12+bytesOffset, 32);

        //充电枪口 1
        byte[] gun_no_byte = new byte[] { EncodeUtil.ValueToByte(query.getGun_no()) };

        System.arraycopy(gun_no_byte, 0, data, 44+bytesOffset, 1);

        //起始时间
        byte[] start_time_byte = EncodeUtil.dateStrToByteDate(start_time);
        System.arraycopy(start_time_byte, 0, data, 45+bytesOffset, 8);
        //结束时间
        byte[] end_time_byte = EncodeUtil.dateStrToByteDate(end_time);
        System.arraycopy(end_time_byte, 0, data, 53+bytesOffset, 8);

        byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
        System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + BODY_LENGTH, 1);
        return data;

    }
    @Override
    public String pack(StationLocalLogQuery_111 query,MqttNetMsgBase base) {
        byte[] data = getPayload(query,base);
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
