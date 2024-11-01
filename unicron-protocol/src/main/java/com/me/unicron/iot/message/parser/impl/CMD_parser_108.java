package com.me.unicron.iot.message.parser.impl;

import com.me.unicron.iot.message.parser.BaseParser;
import com.me.epower.direct.entity.upward.StationEventReport_108;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.util.PackerUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_parser_108 implements BaseParser {
	public static final int CMD_NO = 108;

	public StationEventReport_108 unpack(byte[] dataByte) {
		StationEventReport_108 resp = new StationEventReport_108();
		try {
			byte[] reserved1 = new byte[2];
			System.arraycopy(dataByte, 0, reserved1, 0, 2);
			resp.setReserved1(PackerUtil.byteToShort(reserved1) + "");
			byte[] reserved2 = new byte[2];
			System.arraycopy(dataByte, 2, reserved2, 0, 2);
			resp.setReserved2(PackerUtil.byteToShort(reserved2) + "");

			byte[] gun_no = new byte[1];
			System.arraycopy(dataByte, 4, gun_no, 0, 1);
			resp.setGun_no(EncodeUtil.byteToValue(gun_no[0]) + "");

			byte[] event_name = new byte[4];
			System.arraycopy(dataByte, 5, event_name, 0, 4);
			resp.setEvent_name(EncodeUtil.byteToInt(event_name) + "");

			byte[] event_val = new byte[4];
			System.arraycopy(dataByte, 9, event_val, 0, 4);
			resp.setEvent_val(EncodeUtil.byteToInt(event_val) + "");

			return resp;
		} catch (Exception e) {
			log.error("CMD_parser_108 parse error:{}", e.getMessage());
		}
		return null;

	}
}
