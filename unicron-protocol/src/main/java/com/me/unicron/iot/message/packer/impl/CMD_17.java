package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;

import com.me.epower.direct.entity.downward.PowerStrategyDispatchQuery_17;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_17 implements BaseCMD<PowerStrategyDispatchQuery_17>{

    private static final int BODY_LENGTH = 33;
    private static final int CMD_NO = 17;
    
	@Override
	public byte[] getPayload(PowerStrategyDispatchQuery_17 powerStrategyDispatchQuery_17, MqttNetMsgBase base) {
		
        String equipmentId = powerStrategyDispatchQuery_17.getEquipment_id();
        if (StringUtils.isBlank(equipmentId)) {
        	log.info("equipmentId not set!");
        }
        
		int totalLen = base.getLength(BODY_LENGTH);
		byte[] data = new byte[totalLen];
		
    	data = base.fillPayload2CmdNo(data, totalLen, (short) CMD_NO);
		int offset = base.getBytesOffset();
		offset += 8;

		try {

			byte[] equipmentBytes = new byte[32];
			byte[] equipmentidByte = EncodeUtil.stringToByte(equipmentId);
			System.arraycopy(equipmentidByte, 0, equipmentBytes, 0, equipmentidByte.length);
			System.arraycopy(equipmentBytes, 0, data, offset, equipmentBytes.length);
			offset += 32;
			
			String gunno=powerStrategyDispatchQuery_17.getGun_no();
			byte[] gunnoByte=new byte[]{(byte) Integer.parseInt(gunno)};
			System.arraycopy(gunnoByte, 0, data, offset, 1);
			offset += 1;
			
			byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
			System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + BODY_LENGTH, 1);

		} catch (Exception e) {
			// TODO: handle exception
			log.info(e.getMessage());
		}

		return data;
	}

	@Override
	public String pack(PowerStrategyDispatchQuery_17 t, MqttNetMsgBase base) {
		// TODO Auto-generated method stub
		byte[] data = getPayload(t, base);
		try {
			return new String(data, CharsetDef.CHARSET);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			log.info(e.getMessage());
		}
		return null;
	}

	@Override
	public int getCmdNo() {
		// TODO Auto-generated method stub
		 return CMD_NO;
	}

}
