package com.me.unicron.iot.message.parser.impl;

import com.me.unicron.iot.message.parser.BaseParser;
import com.me.epower.direct.entity.upward.UpdateProcess_1106;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.util.PackerUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * (CODE=1106)充电桩回应后台下发的固定电费计价策略设置/查询指令
 * 
 * @author fzl
 *
 */
@Slf4j
public class CMD_parser_1106 implements BaseParser {

    public UpdateProcess_1106 parse(byte[] dataByte) {
        UpdateProcess_1106 updateProcess_1106 = new UpdateProcess_1106();
        byte[] equipmentIdByte = new byte[32];
        System.arraycopy(dataByte, 0, equipmentIdByte, 0, 32);
        updateProcess_1106.setEquipmentId(PackerUtil.byteToCharsequence(equipmentIdByte)+"");
        byte[] type = new byte[1];
        System.arraycopy(dataByte, 32, type, 0, 1);
        updateProcess_1106.setType(EncodeUtil.byteToValue(type[0]) + "");
        byte[] process = new byte[1];
        System.arraycopy(dataByte, 33, process, 0, 1);
        updateProcess_1106.setType(EncodeUtil.byteToValue(process[0]) + "");        
        return updateProcess_1106;
    }

}
