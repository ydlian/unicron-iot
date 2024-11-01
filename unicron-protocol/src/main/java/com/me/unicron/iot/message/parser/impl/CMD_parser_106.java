package com.me.unicron.iot.message.parser.impl;

import com.me.unicron.iot.message.parser.BaseParser;
import com.me.epower.direct.entity.upward.SignupResponse_106;
import com.me.unicron.EncodeUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_parser_106 implements BaseParser {
	public static final int CMD_NO = 106;

	// 122(body)+9(Header)
	public SignupResponse_106 unpack(byte[] dataByte) {
		SignupResponse_106 resp = new SignupResponse_106();
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

			byte[] allow_charge_flag = new byte[1];
			System.arraycopy(dataByte, 36, allow_charge_flag, 0, 1);
			resp.setAllow_charge_flag(EncodeUtil.byteToValue(allow_charge_flag[0]) + "");

			byte[] station_ver = new byte[4];
			System.arraycopy(dataByte, 37, station_ver, 0, 4);
			resp.setStation_ver(EncodeUtil.byteToStr(station_ver));

			byte[] station_type = new byte[2];
			System.arraycopy(dataByte, 41, station_type, 0, 2);
			resp.setStation_type(EncodeUtil.byteToShort(station_type) + "");

			byte[] start_times = new byte[4];
			System.arraycopy(dataByte, 43, start_times, 0, 4);
			resp.setStart_times(EncodeUtil.byteToInt(start_times) + "");

			byte[] data_upload_mod = new byte[1];
			System.arraycopy(dataByte, 47, data_upload_mod, 0, 1);
			resp.setData_upload_mod(EncodeUtil.byteToValue(data_upload_mod[0]) + "");

			byte[] signup_timespan_min = new byte[2];
			System.arraycopy(dataByte, 48, signup_timespan_min, 0, 2);
			resp.setSignup_timespan_min(EncodeUtil.byteToShort(signup_timespan_min) + "");

			byte[] reserved3 = new byte[1];
			System.arraycopy(dataByte, 50, reserved3, 0, 1);
			resp.setReserved3(EncodeUtil.byteToValue(reserved3[0]) + "");

			byte[] gun_cnt = new byte[1];
			System.arraycopy(dataByte, 51, gun_cnt, 0, 1);
			resp.setGun_cnt(EncodeUtil.byteToValue(gun_cnt[0]) + "");

			byte[] heartbeat_timespan_sec = new byte[1];
			System.arraycopy(dataByte, 52, heartbeat_timespan_sec, 0, 1);
			resp.setHeartbeat_timespan_sec(EncodeUtil.byteToValue(heartbeat_timespan_sec[0]) + "");

			byte[] heartbeat_check_timeout_times = new byte[1];
			System.arraycopy(dataByte, 53, heartbeat_check_timeout_times, 0, 1);
			resp.setHeartbeat_check_timeout_times(EncodeUtil.byteToValue(heartbeat_check_timeout_times[0]) + "");

			byte[] charge_record_cnt = new byte[4];
			System.arraycopy(dataByte, 54, charge_record_cnt, 0, 4);
			resp.setCharge_record_cnt(EncodeUtil.byteToInt(charge_record_cnt) + "");

			byte[] station_local_time = new byte[8];
			System.arraycopy(dataByte, 58, station_local_time, 0, 8);
			resp.setStation_local_time(EncodeUtil.byteDateToDateStr(station_local_time) + "");

			byte[] latest_charge_time = new byte[8];
			System.arraycopy(dataByte, 66, latest_charge_time, 0, 8);
			resp.setLatest_charge_time(EncodeUtil.byteDateToDateStr(latest_charge_time) + "");

			byte[] latest_start_time = new byte[8];
			System.arraycopy(dataByte, 74, latest_start_time, 0, 8);
			resp.setLatest_start_time(EncodeUtil.byteDateToDateStr(latest_start_time) + "");

			byte[] signup_pass = new byte[8];
			System.arraycopy(dataByte, 82, signup_pass, 0, 8);
			resp.setSignup_pass(EncodeUtil.byteToCharsequence(signup_pass, true) + "");

			byte[] station_mac_imei = new byte[32];
			System.arraycopy(dataByte, 90, station_mac_imei, 0, 32);
			resp.setStation_mac_imei(EncodeUtil.byteToCharsequence(station_mac_imei, true) + "");

			return resp;
		} catch (Exception e) {
			log.error("CMD_parser_1306 parse error{}", e);
		}
		return null;
	}
}
