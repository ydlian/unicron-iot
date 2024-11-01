package com.me.unicron.iot.message.parser.impl;

import com.me.unicron.iot.message.parser.BaseParser;
import com.me.unicron.EncodeUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * (CODE=1302)充电桩回应后台下发的固定电费计价策略设置/查询指令
 * 
 * @author fzl
 *
 */
@Slf4j
public class CMD_parser_1302 implements BaseParser {

    public String parse(byte[] buf) {
        byte[] intByte = new byte[4];
        try {
            //每度电价格
            System.arraycopy(buf, 0, intByte, 0, 4);
            return String.valueOf(EncodeUtil.byteToInt(intByte));
        } catch (Exception e) {
            log.error("CMD_parser_1302 parse error{}", e);
        }
        return null;
    }

}
