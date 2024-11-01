package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import com.me.epower.direct.entity.downward.OrderInfoQuery_201;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;

/**
 * @author fzl func：(CODE=201)服务器应答充电桩充电上报最新一次充电信息
 */
@Slf4j
public class CMD_201 implements BaseCMD<OrderInfoQuery_201> {

    private static final int BODY_LENGTH = 37;
    private static final int CMD_NO = 201;
    @Override
    public byte[] getPayload(OrderInfoQuery_201 orderInfoQuery_201,MqttNetMsgBase base) {
        int totalLen = base.getLength(BODY_LENGTH);
        //起始域
        byte[] data = new byte[totalLen];
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
        //命令域
        System.arraycopy(EncodeUtil.shortToByte((short) CMD_NO), 0, data, 6, 2);
		*/
        data=base.fillPayload2CmdNo(data, totalLen, (short) CMD_NO);
        int bytesOffset = base.getBytesOffset();
        
        //保留4字节
        byte[] replyChargeStatus1 = new byte[4];
        System.arraycopy(replyChargeStatus1, 0, data, 8+bytesOffset, 4);

        try {
            //充电枪号
            String portId = orderInfoQuery_201.getPortId();
            byte[] replyChargeStatus2 = new byte[]{EncodeUtil.ValueToByte(portId)};
            System.arraycopy(replyChargeStatus2, 0, data, 12+bytesOffset, 1);
            //充电卡号
            String cardNum = orderInfoQuery_201.getCardNum();
            byte[] cardNumBytes = cardNum.getBytes(CharsetDef.CHARSET);
            byte[] byte32 = new byte[32];
            System.arraycopy(cardNumBytes, 0, byte32, 0, cardNumBytes.length);
            System.arraycopy(byte32, 0, data, 13+bytesOffset, 32);
        } catch (Exception e) {
            log.error("CMD_201_ERROR", e);
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
	public String pack(OrderInfoQuery_201 t,MqttNetMsgBase base) {
		// TODO Auto-generated method stub
		byte[] data = getPayload(t,base);
        try {
            return new String(data, CharsetDef.CHARSET);
        } catch (UnsupportedEncodingException e) {
            log.error("CMD_201 pack error", e);
        }
        return null;
	}

}
