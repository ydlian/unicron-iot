package com.me.unicron.iot.message.parser.impl;

import com.me.epower.direct.entity.upward.LogReportResponse_408;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.parser.BaseParser;
import com.me.unicron.iot.util.PackerUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_parser_410 implements BaseParser {

    public static final int CMD_NO = 410;

    public LogReportResponse_408 unpack(byte[] dataByte) {
        LogReportResponse_408 resp = new LogReportResponse_408();
        try {
            byte[] reserved1 = new byte[2];
            System.arraycopy(dataByte, 0, reserved1, 0, 2);
            resp.setReserved1(PackerUtil.byteToShort(reserved1) + "");
            byte[] reserved2 = new byte[2];
            System.arraycopy(dataByte, 2, reserved2, 0, 2);
            resp.setReserved2(PackerUtil.byteToShort(reserved2) + "");

            byte[] equipmentId = new byte[32];
            System.arraycopy(dataByte, 4, equipmentId, 0, 32);
            resp.setEquipmentId(EncodeUtil.byteToCharsequence(equipmentId, true));

            byte[] filename = new byte[128];
            System.arraycopy(dataByte, 36, filename, 0, 128);
            resp.setFilename(EncodeUtil.byteToCharsequence(filename, true));

            return resp;
        } catch (Exception e) {
            log.error("CMD_parser_408 parse error{}", e);
        }
        return null;
    }
}