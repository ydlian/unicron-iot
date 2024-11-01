package com.me.unicron.iot.message.parser.impl;

import com.me.epower.direct.entity.upward.AuthResponse_802;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.parser.BaseParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_parser_802 implements BaseParser {

    public AuthResponse_802 parse(byte[] bytes) {

        byte[] shortByte = new byte[2];
        byte[] intByte = new byte[4];
        byte[] longByte = new byte[8];
        byte[] byte32 = new byte[32];
        AuthResponse_802 authResponse_802 = new AuthResponse_802();
        try {
            for (int i = 0; i < bytes.length;) {
                System.arraycopy(bytes, i, intByte, 0, 4);
                int contentLength = EncodeUtil.byteToInt(intByte);
                authResponse_802.setEncodeDataLength(String.valueOf(contentLength));
                i += 4;

                byte[] byt_content = new byte[contentLength];
                System.arraycopy(bytes, i, byt_content, 0, contentLength);
                String encodeData = EncodeUtil.byteToCharsequence(byt_content, true);
                authResponse_802.setEncodeData(encodeData);
                i += contentLength;

                byte[] equipmentId = new byte[32];
                System.arraycopy(bytes, i, equipmentId, 0, 32);
                authResponse_802.setEquipmentId(EncodeUtil.byteToCharsequence(equipmentId, true));
                i += 32;

                System.arraycopy(bytes, i, shortByte, 0, 2);
                authResponse_802.setEncodeType(String.valueOf(EncodeUtil.byteToShort(shortByte)));

                return authResponse_802;
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("CMD_parser_802 parse error{}", e);
        }
        return null;
    }

}
