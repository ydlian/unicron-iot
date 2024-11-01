package com.me.unicron.iot.message.parser.impl;

import com.me.epower.direct.entity.upward.IntParaSetResponse_2;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.parser.BaseParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_parser_2 implements BaseParser {

    public static final int CMD_NO = 2;

    public IntParaSetResponse_2 unpack(byte[] dataByte) {
        try {
            //命令类型
            byte[] byt_1 = new byte[1];
            //命令类型
            byte[] byt_4 = new byte[4];
            IntParaSetResponse_2 resp = new IntParaSetResponse_2();
            byte[] reserved1 = new byte[2];
            System.arraycopy(dataByte, 0, reserved1, 0, 2);
            resp.setReserved1(EncodeUtil.byteToShort(reserved1) + "");
            byte[] reserved2 = new byte[2];
            System.arraycopy(dataByte, 2, reserved2, 0, 2);
            resp.setReserved2(EncodeUtil.byteToShort(reserved2) + "");

            byte[] stationId = new byte[32];
            System.arraycopy(dataByte, 4, stationId, 0, 32);
            resp.setStationId(EncodeUtil.byteToCharsequence(stationId, true));

            System.arraycopy(dataByte, 36, byt_1, 0, 1);
            byte cmt_type_val = (byte) byt_1[0];
            resp.setCmd_type(EncodeUtil.byteToValue(byt_1[0]));
            //设置/查询参数启始地址
            System.arraycopy(dataByte, 37, byt_4, 0, 4);
            int start_addr = EncodeUtil.byteToInt(byt_4);
            resp.setStart_addr(start_addr + "");

            System.arraycopy(dataByte, 41, byt_1, 0, 1);
            byte para_cnt_val = (byte) byt_1[0];
            resp.setPara_cnt(EncodeUtil.byteToValue(byt_1[0]));

            System.arraycopy(dataByte, 42, byt_1, 0, 1);
            resp.setQuery_result(EncodeUtil.byteToValue(byt_1[0]));
            //对查询结果解析，按每四位逐个解析，最后拼成字符串
            StringBuffer sb = new StringBuffer();
            if (cmt_type_val == 0) {
                for (int j = 0; j < para_cnt_val; j++) {
                    byte[] byte_n = new byte[4];
                    System.arraycopy(dataByte, 43 + j * 4, byte_n, 0, 4);
                    if ((start_addr + j) == 34) {
                        String ip = EncodeUtil.byte4ToIp(byte_n);
                        sb.append(String.valueOf(ip)).append(",");
                    } else {
                        Integer intValue = EncodeUtil.byteToInt(byte_n);
                        sb.append(String.valueOf(intValue)).append(",");
                    }
                }
                if (sb.length() > 0) {
                    resp.setResponse_data(sb.substring(0, sb.length() - 1));
                }
            }
            return resp;
        } catch (Exception e) {
            log.error("CMD_parser_2 parse error{}", e);
        }
        return null;

    }
}
