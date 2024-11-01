package com.me.unicron.iot.message.parser.impl;

import com.me.epower.direct.entity.upward.BeatHeartResponse_102;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.parser.BaseParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_parser_102 implements BaseParser {
	public static final int CMD_NO = 102;

	public BeatHeartResponse_102 unpack(byte[] dataByte) {

		BeatHeartResponse_102 resp = new BeatHeartResponse_102();
		try {
			byte[] reserved1 = new byte[2];
			System.arraycopy(dataByte, 0, reserved1, 0, 2);
			resp.setReserved1(EncodeUtil.byteToShort(reserved1) + "");
			byte[] reserved2 = new byte[2];
			System.arraycopy(dataByte, 2, reserved2, 0, 2);
			resp.setReserved2(EncodeUtil.byteToShort(reserved2) + "");

			byte[] stationId = new byte[32];
			System.arraycopy(dataByte, 4, stationId, 0, 32);
			resp.setStationId(EncodeUtil.byteToCharsequence(stationId, true));

			byte[] byte_2 = new byte[2];
			System.arraycopy(dataByte, 36, byte_2, 0, 2);
			resp.setHeartbeat_index(EncodeUtil.byteToShort(byte_2) + "");

			return resp;
		} catch (Exception e) {
			log.error("CMD_parser_102 parse error{}", e);
		}
		return null;

	}
}
