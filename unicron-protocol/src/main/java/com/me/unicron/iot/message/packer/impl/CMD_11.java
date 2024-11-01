package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;

import com.me.epower.direct.entity.chargecloud.QueryStopChargeParam;
import com.me.epower.direct.entity.downward.ChargeStopQuery_11;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.math.NumberUtils;

@Slf4j
public class CMD_11 implements BaseCMD<ChargeStopQuery_11>{

    private static final int BODY_LENGTH = 65;
    private static final int CMD_NO = 11;
    
	@Override
	public byte[] getPayload(ChargeStopQuery_11 chargeStopQuery_11, MqttNetMsgBase base) {
		int bodyLenth = BODY_LENGTH;

        //
        String startChargeSeq = chargeStopQuery_11.getCharge_seq();
        if (StringUtils.isBlank(startChargeSeq)) {
        	log.info("startChargeSeq not set!");
        }

        String gun_no = chargeStopQuery_11.getGun_no();
        if (StringUtils.isBlank(gun_no) || !NumberUtils.isNumber(gun_no)) {
        	log.info("gun_no not set!");
        }
        
        String equipmentId = chargeStopQuery_11.getBaseEquipmentId();
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

			byte[] portBytes = {EncodeUtil.ValueToByte(gun_no)};
			System.arraycopy(portBytes, 0, data, offset, portBytes.length);
			offset += 1;
			
			
			byte[] chargeSeqBytes = new byte[32];
			byte[] startChargeSeqBytes = EncodeUtil.stringToByte(startChargeSeq);
			System.arraycopy(startChargeSeqBytes, 0, chargeSeqBytes, 0, startChargeSeqBytes.length);
			System.arraycopy(chargeSeqBytes, 0, data, offset, chargeSeqBytes.length);
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
	public String pack(ChargeStopQuery_11 t, MqttNetMsgBase base) {
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
