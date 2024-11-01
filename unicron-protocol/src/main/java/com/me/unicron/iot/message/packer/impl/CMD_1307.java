package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.epower.direct.entity.downward.DelayFeePolicyQuery_1307;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;

/**
 * @author fzl func：(CODE=1303)后台下发峰平谷电费计价策略设置/查询指令
 */
@Slf4j
public class CMD_1307 implements BaseCMD<DelayFeePolicyQuery_1307> {

    private static final int BODY_LENGTH = 225;
    private static final int CMD_NO = 1307;
    
    private static final String POLICT_TYPE_SET = "1";

    private static final String POLICT_TYPE_QUERY = "0";
    @Override
    public byte[] getPayload(DelayFeePolicyQuery_1307 delayFeePolicyQuery_1307,MqttNetMsgBase base) {
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

        //类型 0-查询 1-设置
        String policyType = delayFeePolicyQuery_1307.getPolicyType();
        byte[] replyChargeStatus1 = new byte[] { EncodeUtil.ValueToByte(policyType) };
        System.arraycopy(replyChargeStatus1, 0, data, 8+bytesOffset, 1);
        
        String equipmentId = delayFeePolicyQuery_1307.getEquipmentId();
        byte[] equipmentIdByte =  EncodeUtil.stringToByte(equipmentId) ;
        byte[] byte32 = new byte[32];
        System.arraycopy(equipmentIdByte, 0, byte32, 0, equipmentIdByte.length);
        System.arraycopy(byte32, 0, data, 9+bytesOffset, 32);
        
        if(POLICT_TYPE_SET.equals(policyType)){
            //该时段内每度电的电费，用整型值表示，要乘0.01才能得到真实的值
            List<String> policyInfos = delayFeePolicyQuery_1307.getDurPrices();
            if (policyInfos != null && policyInfos.size() > 0) {
                for (int i = 0; i < 48; i++) {
                    String durPrice = policyInfos.get(i);
                    byte[] durPriceByte = EncodeUtil.intToByte(Integer.parseInt(durPrice));
                    System.arraycopy(durPriceByte, 0, data, 41 + 4 * i+bytesOffset, 4);
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
	public String pack(DelayFeePolicyQuery_1307 t,MqttNetMsgBase base) {
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
