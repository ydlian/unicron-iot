package com.me.unicron.iot.message.parser.impl;

import com.me.unicron.iot.message.parser.BaseParser;
import com.me.epower.direct.entity.upward.HttpUpdate_1102;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.util.PackerUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_parser_1102 implements BaseParser {

    public static final int CMD_NO = 1102;

    public HttpUpdate_1102 parse(byte[] dataByte) {
        HttpUpdate_1102 resp = new HttpUpdate_1102();
        try {

            byte[] success = new byte[1];
            System.arraycopy(dataByte, 0, success, 0, 1);
            resp.setSuccess(EncodeUtil.byteToValue(success[0]) + "");

            byte[] md5 = new byte[32];
            System.arraycopy(dataByte, 1, md5, 0, 32);
            resp.setMd5(PackerUtil.byteToCharsequence(md5));

            return resp;
        } catch (Exception e) {
            log.error("CMD_parser_1102 parse error{}", e);
        }
        return null;
    }
}
