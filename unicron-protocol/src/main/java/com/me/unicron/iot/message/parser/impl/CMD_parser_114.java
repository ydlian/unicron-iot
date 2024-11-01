package com.me.unicron.iot.message.parser.impl;

import com.me.unicron.iot.message.parser.BaseParser;
import com.me.epower.direct.entity.upward.StationQueryLogicInfo_114;
import com.me.unicron.iot.util.PackerUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_parser_114 implements BaseParser {

    public static final int CMD_NO = 114;

    public StationQueryLogicInfo_114 unpack(byte[] dataByte) {
        StationQueryLogicInfo_114 resp = new StationQueryLogicInfo_114();
        try {
            byte[] reserved1 = new byte[2];
            System.arraycopy(dataByte, 0, reserved1, 0, 2);
            resp.setReserved1(PackerUtil.byteToShort(reserved1) + "");
            byte[] reserved2 = new byte[2];
            System.arraycopy(dataByte, 2, reserved2, 0, 2);
            resp.setReserved2(PackerUtil.byteToShort(reserved2) + "");

            byte[] stationId = new byte[32];
            System.arraycopy(dataByte, 4, stationId, 0, 32);
            resp.setStationId(PackerUtil.byteToCharsequence(stationId));
            return resp;
        } catch (Exception e) {
            log.error("CMD_parser_114 parse error{}", e);
        }
        return null;

    }
}