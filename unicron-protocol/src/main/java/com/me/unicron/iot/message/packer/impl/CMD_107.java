package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import com.me.epower.direct.entity.downward.StationEventReportResponse_107;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;

/**
 * @author lianyadong
 * @func：(CODE=107)服务器应答充电桩事件信息上报
 */
@Slf4j
public class CMD_107 implements BaseCMD<StationEventReportResponse_107> {

    private static final int BODY_LENGTH = 41;
    private static final int CMD_NO = 107;

    @Override
    public byte[] getPayload(StationEventReportResponse_107 resp,MqttNetMsgBase base) {
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

        try {
            //充电桩编码 32
            byte[] station_id_byte = new byte[32];
            if(resp.getStation_id()!=null){
                station_id_byte = resp.getStation_id().getBytes(CharsetDef.CHARSET);
            }
            byte[] byte32 = new byte[32];
            System.arraycopy(station_id_byte, 0, byte32, 0, station_id_byte.length);      
            System.arraycopy(byte32, 0, data, 12+bytesOffset, 32);
            //充电枪口 1
            byte[] gun_no_byte = new byte[] { EncodeUtil.ValueToByte(resp.getGun_no()) };
            System.arraycopy(gun_no_byte, 0, data, 44+bytesOffset, 1);

            //事件名称 4
            byte[] event_id_byte = new byte[4];
            byte[] event_bytes= EncodeUtil.intToByte(Integer.parseInt(resp.getEvent_name()));
            System.arraycopy(event_bytes, 0, event_id_byte,0, 4);
            System.arraycopy(event_id_byte, 0, data, 45+bytesOffset, 4);
            
        } catch (Exception e) {
            log.error("CMD_107 error,{}", e.getMessage());
        }
        byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
        System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + BODY_LENGTH, 1);
        return data;
    }

    @Override
    public String pack(StationEventReportResponse_107 resp,MqttNetMsgBase base) {
        // TODO Auto-generated method stub
        byte[] data = getPayload(resp,base);
        try {
            return new String(data, CharsetDef.CHARSET);
        } catch (UnsupportedEncodingException e) {
            log.error("CMD_107 error", e);
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
