package com.me.unicron.iot.message.parser.impl;

import com.me.epower.direct.entity.upward.ControlCommandResponse_6;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.parser.BaseParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_parser_6 implements BaseParser {

    public static final int CMD_NO = 6;

    public ControlCommandResponse_6 unpack(byte[] dataByte) {
    	try{
    	
    	//命令类型
        byte[] byt_1 = new byte[1];
        //命令类型
        byte[] byt_4 = new byte[4];
        ControlCommandResponse_6 resp = new ControlCommandResponse_6();
        byte[] reserved1 = new byte[2];
        System.arraycopy(dataByte, 0, reserved1, 0, 2);
        resp.setReserved1(EncodeUtil.byteToShort(reserved1) + "");
        byte[] reserved2 = new byte[2];
        System.arraycopy(dataByte, 2, reserved2, 0, 2);
        resp.setReserved2(EncodeUtil.byteToShort(reserved2) + "");

        byte[] stationId = new byte[32];
        System.arraycopy(dataByte, 4, stationId, 0, 32);
        resp.setStationId(EncodeUtil.byteToCharsequence(stationId, true));

        //枪口
        System.arraycopy(dataByte, 36, byt_1, 0, 1);
        String gun_no_val = EncodeUtil.byteToValue(byt_1[0]);
        resp.setGun_no(gun_no_val);
        //命令启始标志
        System.arraycopy(dataByte, 37, byt_4, 0, 4);
        resp.setStart_addr(EncodeUtil.byteToInt(byt_4) + "");

        System.arraycopy(dataByte, 41, byt_1, 0, 1);
        resp.setPara_cnt(EncodeUtil.byteToValue(byt_1[0]));

        System.arraycopy(dataByte, 42, byt_1, 0, 1);
        resp.setQuery_result(EncodeUtil.byteToValue(byt_1[0]));

        resp.setReserved1(EncodeUtil.printHex(byt_1));
        return resp;
    	
    	}catch(Exception e){
    		log.error("CMD_parser_1306 parse error{}", e);
    	}
    	return null;
    }
}
