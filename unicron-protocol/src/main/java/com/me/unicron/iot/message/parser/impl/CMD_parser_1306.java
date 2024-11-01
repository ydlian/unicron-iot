package com.me.unicron.iot.message.parser.impl;

import java.util.ArrayList;

import com.me.epower.direct.entity.upward.ServiceFee_1306;
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
public class CMD_parser_1306 implements BaseParser {

    public ServiceFee_1306 parse(byte[] buf) {
        ServiceFee_1306 serviceFee_1306 = new ServiceFee_1306();
        try {
            byte type = buf[0];
            serviceFee_1306.setType(EncodeUtil.byteToValue(type));

            byte gun_no = buf[1];
            serviceFee_1306.setGun_no_str(EncodeUtil.byteToValue(gun_no));
            
            byte[] intByte = new byte[4];
            ArrayList<String> periodPriceArray = new ArrayList<String>();
            for (int i = 0; i < 48; i++) {
                //每度电价格
                System.arraycopy(buf, 2 + 4 * i, intByte, 0, 4);
                periodPriceArray.add(String.valueOf(EncodeUtil.byteToInt(intByte)));
            }
            serviceFee_1306.setServiceFee(periodPriceArray);

        } catch (Exception e) {
            log.error("CMD_parser_1306 parse error{}", e);
        }

        return serviceFee_1306;
    }
}
