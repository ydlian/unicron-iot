package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import com.me.epower.direct.entity.downward.HistoryOrderInfoQuery_401;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;

/**
 * @author fzl func：(CODE=401)服务器应答充电桩未上传历史充电记录
 */
@Slf4j
public class CMD_401 implements BaseCMD<HistoryOrderInfoQuery_401> {

    private static final int BODY_LENGTH = 8;
    private static final int CMD_NO = 401;

   
    @Override
    public byte[] getPayload(HistoryOrderInfoQuery_401 historyOrderInfoQuery_401,MqttNetMsgBase base) {
        int totalLen = base.getLength(BODY_LENGTH);
        //起始域
        byte[] data = new byte[totalLen];
        data=base.fillPayload2CmdNo(data, totalLen, (short) CMD_NO);
        int bytesOffset = base.getBytesOffset();
        
        /*
        System.arraycopy(MqttNetMsg.getHerder(), 0, data, 0, 2);
        //长度域
        byte[] length = EncodeUtil.shortToByte((short) totalLen);
        System.arraycopy(length, 0, data, 2, 2);
        //版本域
        byte[] ver = MqttNetMsg.getVersion();
        System.arraycopy(ver, 0, data, 4, 1);
        //序列号域
        byte[] index = MqttNetMsg.getIndex();
        System.arraycopy(index, 0, data, 5, 1);
        //命令域 14
        System.arraycopy(EncodeUtil.shortToByte((short) CMD_NO), 0, data, 6, 2);
        */
        //保留4字节
        byte[] replyChargeStatus1 = new byte[4];
        System.arraycopy(replyChargeStatus1, 0, data, 8+bytesOffset, 4);

        try {
            //充电记录索引
            String chargeIndex = historyOrderInfoQuery_401.getChargeIndex();
            byte[] chargeIndexBytes = EncodeUtil.intToByte(Integer.parseInt(chargeIndex));
            System.arraycopy(chargeIndexBytes, 0, data, 12+bytesOffset, 4);
        } catch (Exception e) {
            log.error("CMD_401_ERROR", e);
        }
        byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
        System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + BODY_LENGTH, 1);
        return data;
    }

    
    @Override
    public int getCmdNo() {
        // TODO Auto-generated method stub
        return CMD_NO;
    }

    public static void print() {
        
    }

    public static void main(String[] args) throws Exception {
        print();

    }


	@Override
	public String pack(HistoryOrderInfoQuery_401 t,MqttNetMsgBase base) {
		// TODO Auto-generated method stub
		byte[] data = getPayload(t,base);
        try {
            return new String(data, CharsetDef.CHARSET);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
	}

   
}
