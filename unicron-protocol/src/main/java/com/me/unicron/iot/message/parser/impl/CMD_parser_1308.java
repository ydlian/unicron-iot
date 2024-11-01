package com.me.unicron.iot.message.parser.impl;

import java.util.ArrayList;

import com.me.epower.direct.entity.upward.DelayFee_1308;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.parser.BaseParser;

import lombok.extern.slf4j.Slf4j;

/**
 * (CODE=1306)充电桩响应后台下发当前充电用户充电服务费设置/查询指令
 * 
 * @author fzl
 *
 */
@Slf4j
public class CMD_parser_1308 implements BaseParser {

    public DelayFee_1308 parse(byte[] buf) {
        DelayFee_1308 delayFee_1308 = new DelayFee_1308();
        try {

            byte type = buf[0];
            delayFee_1308.setPolicyType(EncodeUtil.byteToValue(type) + "");

            byte[] equipmentByte = new byte[32];
            System.arraycopy(buf, 1, equipmentByte, 0, 32);
            delayFee_1308.setEquipmentId(EncodeUtil.byteToCharsequence(equipmentByte, true));
            byte[] intByte = new byte[4];
            ArrayList<String> periodPriceArray = new ArrayList<String>();
            for (int i = 0; i < 48; i++) {
                //每度电价格
                System.arraycopy(buf, 33 + 4 * i, intByte, 0, 4);
                periodPriceArray.add(String.valueOf(EncodeUtil.byteToInt(intByte)));
            }
            delayFee_1308.setDurPrices(periodPriceArray);

        } catch (Exception e) {
            log.error("CMD_parser_1308 parse error{}", e);
        }

        return delayFee_1308;

    }
}
