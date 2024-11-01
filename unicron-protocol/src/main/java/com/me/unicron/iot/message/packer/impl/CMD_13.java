package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;

import com.me.epower.direct.entity.chargecloud.QueryStopChargeParam;
import com.me.epower.direct.entity.downward.ChargeStopQuery_11;
import com.me.epower.direct.entity.downward.ChargeUnitQuery_13;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.math.NumberUtils;

@Slf4j
public class CMD_13 implements BaseCMD<ChargeUnitQuery_13>{

    private static final int BODY_LENGTH = 32;
    private static final int CMD_NO = 13;
    
	@Override
	public byte[] getPayload(ChargeUnitQuery_13 chargeUnitQuery_13, MqttNetMsgBase base) {
		int bodyLenth = BODY_LENGTH;

      
        String equipmentId = chargeUnitQuery_13.getBaseEquipmentId();
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

			
			byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
			System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + BODY_LENGTH, 1);

		} catch (Exception e) {
			// TODO: handle exception
			log.info(e.getMessage());
		}

		return data;
	}

	@Override
	public String pack(ChargeUnitQuery_13 t, MqttNetMsgBase base) {
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
