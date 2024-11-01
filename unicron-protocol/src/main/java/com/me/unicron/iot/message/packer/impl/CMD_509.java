package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import com.me.epower.direct.entity.downward.LogRequest_409;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.builder.AgentBuilder.Identified.Narrowable;

@Slf4j
public class CMD_509 implements BaseCMD<LogRequest_409> {

	private static final int BODY_LENGTH = 164;
	private static final int CMD_NO = 509;

	@Override
	public byte[] getPayload(LogRequest_409 logRequest_409, MqttNetMsgBase base) {
		// MqttNetMsg
		int totalLen = base.getLength(BODY_LENGTH);
		byte[] data = new byte[totalLen];

		data = base.fillPayload2CmdNo(data, totalLen, (short) CMD_NO);
		int offset = base.getBytesOffset();
		offset += 8;

		try {

			byte[] reserveByte = new byte[2];
			System.arraycopy(reserveByte, 0, data, offset, reserveByte.length);
			offset += 2;
			// 枪号
			byte[] reserve = EncodeUtil.shortToByte(Short.parseShort(logRequest_409.getReserved2()));
			System.arraycopy(reserve, 0, reserveByte, 0, reserve.length);
			System.arraycopy(reserveByte, 0, data, offset, reserveByte.length);
			offset += 2;

			byte[] equipmentId = new byte[32];
			byte[] equipmentidByte = EncodeUtil.stringToByte(logRequest_409.getEquipmentId());
			System.arraycopy(equipmentidByte, 0, equipmentId, 0, equipmentidByte.length);
			System.arraycopy(equipmentId, 0, data, offset, equipmentId.length);
			offset += 32;

			String filename = logRequest_409.getFilename();
			byte[] filenameBytes = filename.getBytes(CharsetDef.CHARSET);
			byte[] filenameBytes128 = new byte[128];
			System.arraycopy(filenameBytes, 0, filenameBytes128, 0, filenameBytes.length);
			System.arraycopy(filenameBytes128, 0, data, offset, 128);
			offset += 128;
			
			byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
			System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + BODY_LENGTH, 1);

		} catch (Exception e) {
			// TODO: handle exception
			log.info(e.getMessage());
		}

		return data;
	}

	@Override
	public String pack(LogRequest_409 t, MqttNetMsgBase base) {
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
