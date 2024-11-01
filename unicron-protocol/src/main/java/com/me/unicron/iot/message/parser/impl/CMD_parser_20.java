package com.me.unicron.iot.message.parser.impl;

import com.me.epower.direct.entity.upward.CleanResponse_20;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.parser.BaseParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_parser_20 implements BaseParser {

    public static final int CMD_NO = 20;

    public CleanResponse_20 unpack(byte[] dataByte) {
        try {

            // 命令类型
            CleanResponse_20 resp = new CleanResponse_20();
            byte[] reserved1 = new byte[2];
            System.arraycopy(dataByte, 0, reserved1, 0, 2);
            resp.setReserved1(EncodeUtil.byteToShort(reserved1) + "");
            byte[] reserved2 = new byte[2];
            System.arraycopy(dataByte, 2, reserved2, 0, 2);
            resp.setReserved2(EncodeUtil.byteToShort(reserved2) + "");

            byte[] stationId = new byte[32];
            System.arraycopy(dataByte, 4, stationId, 0, 32);
            resp.setEquipmentId(EncodeUtil.byteToCharsequence(stationId, true));

            // 命令执行结果
            byte byt_4 = dataByte[36];
            resp.setQuery_result(EncodeUtil.byteToValue(byt_4));
            return resp;
        } catch (Exception e) {
            log.error("CMD_parser_20 parse error{}", e);
        }
        return null;
    }
}
