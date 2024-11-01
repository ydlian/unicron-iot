package com.me.unicron.iot.message.parser.impl;

import com.me.unicron.iot.message.parser.BaseParser;
import com.me.epower.direct.entity.upward.CleanResponse_20;
import com.me.epower.direct.entity.upward.LockResponse_24;
import com.me.unicron.EncodeUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_parser_24 implements BaseParser {

    public static final int CMD_NO = 20;

    public LockResponse_24 unpack(byte[] dataByte) {
        try {

            // 命令类型
            LockResponse_24 resp = new LockResponse_24();
            byte[] reserved1 = new byte[2];
            System.arraycopy(dataByte, 0, reserved1, 0, 2);
            resp.setReserved1(EncodeUtil.byteToShort(reserved1) + "");
            byte[] reserved2 = new byte[2];
            System.arraycopy(dataByte, 2, reserved2, 0, 2);
            resp.setReserved2(EncodeUtil.byteToShort(reserved2) + "");

            //充电桩编码
            byte[] byte32 = new byte[32];
            System.arraycopy(dataByte, 4, byte32, 0, 32);
            String equipmentId =  EncodeUtil.byteToCharsequence(byte32, true);
            resp.setEquipmentId(equipmentId);
            
            byte gunNo = dataByte[36];
            resp.setGunNo(EncodeUtil.byteToValue(gunNo));
            
            
            byte result =dataByte[37];
            resp.setResult(EncodeUtil.byteToValue(result));
            
            // 命令执行结果

            return resp;
        } catch (Exception e) {
            log.error("CMD_parser_24 parse error{}", e);
        }
        return null;
    }
}
