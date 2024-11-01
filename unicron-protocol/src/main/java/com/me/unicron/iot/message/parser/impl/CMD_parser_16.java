package com.me.unicron.iot.message.parser.impl;

import com.me.unicron.iot.message.parser.BaseParser;
import com.me.epower.direct.entity.upward.StationPowerDispatchResult_16;
import com.me.unicron.EncodeUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_parser_16 implements BaseParser {
	public static final int CMD_NO = 16;

	public  StationPowerDispatchResult_16 unpack(byte[] dataByte) {
		try {

			StationPowerDispatchResult_16 resp = new StationPowerDispatchResult_16();
			byte[] equipmentBytes = new byte[32];
			System.arraycopy(dataByte, 0, equipmentBytes, 0, equipmentBytes.length);
			resp.setEquipmentId(EncodeUtil.byteToCharsequence(equipmentBytes, true));

			byte[] gunno = new byte[1];
			System.arraycopy(dataByte, 32, gunno, 0, 1);
			int gunnoVal=gunno[0];
			resp.setGun_no(gunnoVal+"");

			byte[] totalPower = new byte[4];
			System.arraycopy(dataByte, 33, totalPower, 0, 4);
			resp.setConnectorTotalPower(""+EncodeUtil.byteToInt(totalPower));
			
			byte[] resultBytes = new byte[4];
			System.arraycopy(dataByte, 37, totalPower, 0, 4);
			resp.setResult(""+EncodeUtil.byteToInt(resultBytes));
			
			
			return resp;

		} catch (Exception e) {
			log.error("CMD_parser_16 parse error{}", e.getMessage());
		}
		return null;
	}
}
