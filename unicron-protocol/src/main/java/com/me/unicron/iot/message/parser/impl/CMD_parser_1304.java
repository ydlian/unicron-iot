package com.me.unicron.iot.message.parser.impl;

import com.me.unicron.iot.message.parser.BaseParser;
import com.me.unicron.EncodeUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * (CODE=1304)充电桩响应后台下发峰平谷电费计价策略设置/查询指令
 * 
 * @author fzl
 *
 */
@Slf4j
public class CMD_parser_1304 implements BaseParser {

    public String[] parse(byte[] buf) {
        byte[] intByte = new byte[4];
        try {
            String[] periodPriceArray = new String[48];
            for (int i = 0; i < 48; i++) {
                //每度电价格
                System.arraycopy(buf,  4 * i, intByte, 0, 4);
                periodPriceArray[i] = String.valueOf(EncodeUtil.byteToInt(intByte));
            }
            return periodPriceArray;
        } catch (Exception e) {
            log.error("CMD_parser_1304 parse error{}", e);
        }

        return null;
    }
}
