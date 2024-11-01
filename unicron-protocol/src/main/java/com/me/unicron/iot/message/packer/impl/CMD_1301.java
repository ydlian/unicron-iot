package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.epower.direct.entity.downward.FixPolicyQuery_1301;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;

/**
 * @author fzl func：(CODE=1301)后台下发固定电费计价策略设置/查询指令
 */
@Slf4j
public class CMD_1301 implements BaseCMD<FixPolicyQuery_1301> {

    private static final int BODY_LENGTH = 5;
    private static final int CMD_NO = 1301;
    private static short inc = 0;
    private static final String POLICT_TYPE_SET = "1";
    private static final String POLICT_TYPE_QUERY = "0";
    
    

    @Override
    public int getCmdNo() {
        // TODO Auto-generated method stub
        return CMD_NO;
    }

    @Override
    public byte[] getPayload(FixPolicyQuery_1301 fixPolicyQuery_1301,MqttNetMsgBase base) {
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
        String policyType = fixPolicyQuery_1301.getPolicyType();
        byte[] replyChargeStatus1 = new byte[] { EncodeUtil.ValueToByte(policyType) };
        System.arraycopy(replyChargeStatus1, 0, data, 8+bytesOffset, 1);
        
        if(POLICT_TYPE_SET.equals(policyType)){
            //每度电价格
            String pricePerKwh = fixPolicyQuery_1301.getPricePerKwh();
            Integer pricePerKwhInt = Integer.parseInt(pricePerKwh);
            byte[] replyChargeStatus2 = EncodeUtil.intToByte(pricePerKwhInt);        
            System.arraycopy(replyChargeStatus2, 0, data, 9+bytesOffset, 4);
        }else{
            byte[] replyChargeStatus2 = new byte[4];        
            System.arraycopy(replyChargeStatus2, 0, data, 9+bytesOffset, 4);
        }
        
        byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
        System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + BODY_LENGTH, 1);
        return data;
    }

    
    public static void print() {
        CMD_1301 cmd = new CMD_1301();
        FixPolicyQuery_1301 fixPolicyQuery_1301 = new FixPolicyQuery_1301();
        fixPolicyQuery_1301.setPolicyType("1");
        fixPolicyQuery_1301.setPricePerKwh("80");
        //编码前：
        byte[] byt = cmd.getPayload(fixPolicyQuery_1301,new MqttNetMsg());
        log.info("编码前：");
        //EncodeUtil.print(byt);
    }

	@Override
	public String pack(FixPolicyQuery_1301 t,MqttNetMsgBase base) {
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
