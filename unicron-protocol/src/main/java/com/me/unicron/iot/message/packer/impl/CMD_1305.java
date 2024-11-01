package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import com.me.epower.direct.entity.downward.ServiceFeeQuery_1305;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;

/**
 * @author fzl func：(CODE=1305)后台下发当前充电用户充电服务费设置/查询指令
 */
@Slf4j
public class CMD_1305 implements BaseCMD<ServiceFeeQuery_1305> {

    private static final int BODY_LENGTH = 194;
    private static final int CMD_NO = 1305;
    private static final String POLICT_TYPE_SET = "1";
    private static final String POLICT_TYPE_QUERY = "0";

    @Override
    public byte[] getPayload(ServiceFeeQuery_1305 serviceFeeQuery_1305,MqttNetMsgBase base) {
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
        //命令域
        System.arraycopy(EncodeUtil.shortToByte((short) CMD_NO), 0, data, 6, 2);
        */
        //
        String policyType = serviceFeeQuery_1305.getPolicyType();
        byte[] replyChargeStatus1 = new byte[] { EncodeUtil.ValueToByte(policyType) };
        System.arraycopy(replyChargeStatus1, 0, data, 8+bytesOffset, 1);
        //充电口号
        String portId = serviceFeeQuery_1305.getPortId();
        byte[] replyChargeStatus2 = new byte[] { EncodeUtil.ValueToByte(portId) };
        System.arraycopy(replyChargeStatus2, 0, data, 9+bytesOffset, 1);

        if (POLICT_TYPE_SET.equals(policyType)) {
            //每度电要收取的服务费
            ArrayList<String> serviceFee = serviceFeeQuery_1305.getDurPrices();

            if (serviceFee != null && serviceFee.size() > 0) {
                for (int i = 0; i < 48; i++) {
                    String durPrice = serviceFee.get(i);
                    int durPriceInt = StringUtils.isBlank(durPrice) ? 0 : Integer.parseInt(durPrice);
                    byte[] durPriceByte = EncodeUtil.intToByte(durPriceInt);
                    System.arraycopy(durPriceByte, 0, data, 10 + 4 * i+bytesOffset, 4);
                }
            }
        }

        byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
        System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + BODY_LENGTH, 1);
        return data;
    }

   
    @Override
    public int getCmdNo() {
        return CMD_NO;
    }

	@Override
	public String pack(ServiceFeeQuery_1305 t,MqttNetMsgBase base) {
		// TODO Auto-generated method stub
		byte[] data = getPayload(t,base);
        try {
            return new String(data, CharsetDef.CHARSET);
        } catch (UnsupportedEncodingException e) {
            log.error("CMD_115 parse error", e);
        }
        return null;
	}

}
