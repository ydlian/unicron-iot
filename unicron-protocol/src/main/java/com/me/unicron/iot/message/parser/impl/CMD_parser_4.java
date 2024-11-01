package com.me.unicron.iot.message.parser.impl;

import com.me.unicron.iot.message.parser.BaseParser;
import com.me.epower.direct.entity.upward.StringhParaSetResponse_4;
import com.me.unicron.EncodeUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_parser_4 implements BaseParser {

    public static final int CMD_NO = 4;

    public StringhParaSetResponse_4 unpack(byte[] dataByte) {
        try {
            //命令类型
            byte[] byt_1 = new byte[1];
            //命令类型
            byte[] byt_4 = new byte[4];
            StringhParaSetResponse_4 resp = new StringhParaSetResponse_4();
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
            resp.setCmd_type(EncodeUtil.byteToValue(cmt_type_val));
            //设置/查询参数启始地址
            System.arraycopy(dataByte, 37, byt_4, 0, 4);
            resp.setStart_addr(EncodeUtil.byteToInt(byt_4) + "");

            System.arraycopy(dataByte, 41, byt_1, 0, 1);
            resp.setQuery_result(EncodeUtil.byteToValue(byt_1[0]));
            //返回参数信息
            if (cmt_type_val == 0) {
                //去掉前面的42字节
                int copy_len = dataByte.length - 42;
                byte[] byte_n = new byte[copy_len];
                System.arraycopy(dataByte, 42, byte_n, 0, copy_len);
                String newStr  = null;
                if(!("14".equals(resp.getStart_addr()))){
                    byte[] cutBytes = EncodeUtil.cutCharsequence(byte_n);
                    String utf8= new String(cutBytes, "UTF-8");
                    byte[] isobytes = utf8.getBytes("ISO-8859-1");
                    newStr = new String(isobytes, "UTF-8");
                }else{
                    newStr= String.valueOf( EncodeUtil.byteToInt(byte_n));
                }
                resp.setResponse_data(newStr);
            }
            return resp;

        } catch (Exception e) {
            log.error("CMD_parser_1306 parse error{}", e);
        }
        return null;
    }

}
