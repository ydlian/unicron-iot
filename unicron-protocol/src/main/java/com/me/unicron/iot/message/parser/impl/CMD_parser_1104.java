package com.me.unicron.iot.message.parser.impl;

import com.me.unicron.iot.message.parser.BaseParser;
import com.me.epower.direct.entity.upward.FileVersion_1104;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.util.PackerUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * (CODE=1104)充电桩回应后台下发的固定电费计价策略设置/查询指令
 * 
 * @author fzl
 *
 */
@Slf4j
public class CMD_parser_1104 implements BaseParser {

    public FileVersion_1104 parse(byte[] dataByte) {
        FileVersion_1104 resp = new FileVersion_1104();
        try {

            byte[] success = new byte[1];
            System.arraycopy(dataByte, 0, success, 0, 1);
            resp.setType(EncodeUtil.byteToValue(success[0]) + "");

            byte[] filebyte = new byte[32];
            System.arraycopy(dataByte, 1, filebyte, 0, 32);
            resp.setFilename(PackerUtil.byteToCharsequence(filebyte));
            
            byte[] md5 = new byte[32];
            System.arraycopy(dataByte, 33, md5, 0, 32);
            resp.setMd5(PackerUtil.byteToCharsequence(md5));

            return resp;
        } catch (Exception e) {
            log.error("CMD_parser_1104 parse error{}", e);
        }
        return null;
    }

}
