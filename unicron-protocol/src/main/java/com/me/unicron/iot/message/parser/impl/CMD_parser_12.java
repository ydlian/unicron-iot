package com.me.unicron.iot.message.parser.impl;

import com.me.epower.direct.entity.upward.StopChargeCommandResponse_12;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.parser.BaseParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_parser_12 implements BaseParser {
	public static final int CMD_NO = 12;

	public StopChargeCommandResponse_12 unpack(byte[] dataByte) {
		try {

			StopChargeCommandResponse_12 resp = new StopChargeCommandResponse_12();
			byte[] equipmentBytes = new byte[32];
			System.arraycopy(dataByte, 0, equipmentBytes, 0, equipmentBytes.length);
			resp.setEquipmentId(EncodeUtil.byteToCharsequence(equipmentBytes, true));
			
			byte[] gunNoBytes = new byte[1];
			System.arraycopy(dataByte, 32, gunNoBytes, 0, 1);
			resp.setGunNo(EncodeUtil.byteToValue(gunNoBytes[0]));

			byte[] chargeSeqBytes = new byte[32];
			System.arraycopy(dataByte, 33, chargeSeqBytes, 0, chargeSeqBytes.length);
			resp.setChargeSeq(EncodeUtil.byteToCharsequence(chargeSeqBytes, true));

			byte[] resultBytes = new byte[4];
			System.arraycopy(dataByte, 65, resultBytes, 0, resultBytes.length);
			resp.setResult(EncodeUtil.byteToCharsequence(resultBytes, true));
			
			return resp;

		} catch (Exception e) {
			log.error("CMD_parser_12 parse error{}", e);
		}
		return null;
	}
}
