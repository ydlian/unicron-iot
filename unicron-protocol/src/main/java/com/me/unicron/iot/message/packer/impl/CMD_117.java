package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.epower.direct.entity.downward.StationFaultReportResponse_117;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;

/**
 * @author lianyadong
 * @func：(CODE=117)服务器应答充电桩故障上报
 */
@Slf4j
public class CMD_117 implements BaseCMD<StationFaultReportResponse_117> {

    private static final int BODY_LENGTH = 37;
    private static final int CMD_NO = 117;

    @Override
    public byte[] getPayload(StationFaultReportResponse_117 resp,MqttNetMsgBase base) {
        int totalLen = base.getLength(BODY_LENGTH);
        byte[] data = new byte[totalLen];
        data=base.fillPayload2CmdNo(data, totalLen, (short) CMD_NO);
        int bytesOffset = base.getBytesOffset();
       
        
       //byte[] reserved = new byte[4];
       //System.arraycopy(reserved, 0, data, 8+bytesOffset, 4);

        try {
            //充电桩编码 32
            byte[] station_id_byte = new byte[32];
            if(resp.getStation_id()!=null){
                station_id_byte = resp.getStation_id().getBytes(CharsetDef.CHARSET);
            }
            byte[] byte32 = new byte[32];
            System.arraycopy(station_id_byte, 0, byte32, 0, station_id_byte.length);      
            System.arraycopy(byte32, 0, data, 8+bytesOffset, 32);
            //充电枪口 1
            byte[] gun_no_byte = new byte[] { EncodeUtil.ValueToByte(resp.getGun_no()) };
            System.arraycopy(gun_no_byte, 0, data, 40+bytesOffset, 1);

            //错误码 4
            byte[] event_id_byte = new byte[4];
            event_id_byte = resp.getErrcode().getBytes(CharsetDef.CHARSET);
            System.arraycopy(event_id_byte, 0, data, 41+bytesOffset, 4);
            
        } catch (UnsupportedEncodingException e) {
            log.error("CMD_117 error", e);
        }
        byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
        System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + BODY_LENGTH, 1);
        return data;
    }

    @Override
    public String pack(StationFaultReportResponse_117 resp,MqttNetMsgBase base) {
        // TODO Auto-generated method stub
        byte[] data = getPayload(resp,base);
        try {
            return new String(data, CharsetDef.CHARSET);
        } catch (UnsupportedEncodingException e) {
            log.error("CMD_117 error", e);
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
