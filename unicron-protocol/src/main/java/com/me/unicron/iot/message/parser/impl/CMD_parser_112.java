package com.me.unicron.iot.message.parser.impl;

import com.me.unicron.iot.message.parser.BaseParser;
import com.me.epower.direct.entity.upward.StationLocalLogQueryResponse_112;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.util.PackerUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_parser_112 implements BaseParser {
	public static final int CMD_NO = 112;
	public StationLocalLogQueryResponse_112 unpack(byte[] dataByte){
		StationLocalLogQueryResponse_112 resp=new StationLocalLogQueryResponse_112();
		try{
		byte[] reserved1=new byte[2];
		System.arraycopy(dataByte, 0, reserved1, 0, 2);
		resp.setReserved1(PackerUtil.byteToShort(reserved1)+"");
		byte[] reserved2=new byte[2];
		System.arraycopy(dataByte, 2, reserved2, 0, 2);
		resp.setReserved2(PackerUtil.byteToShort(reserved2)+"");
		
		byte[] stationId=new byte[32];
		System.arraycopy(dataByte, 4, stationId, 0, 32);
		resp.setStationId(PackerUtil.byteToCharsequence(stationId));
		
		byte[] gun_no=new byte[1];
		System.arraycopy(dataByte, 36, gun_no, 0, 1);
		resp.setGun_no(EncodeUtil.byteToValue(gun_no[0]));
		
		byte[] start_time=new byte[8];
		System.arraycopy(dataByte, 37, start_time, 0, 8);
		resp.setStart_time(EncodeUtil.byteDateToDateStr(start_time));
		
		byte[] end_time=new byte[8];
		System.arraycopy(dataByte, 45, end_time, 0, 8);
		resp.setEnd_time(EncodeUtil.byteDateToDateStr(end_time));
		
		byte[] log_data_pack_cnt=new byte[4];
		System.arraycopy(dataByte, 53, log_data_pack_cnt, 0, 4);
		resp.setLog_data_pack_cnt(PackerUtil.byteToInt(log_data_pack_cnt)+"");
		
		byte[] log_data_pack_index=new byte[4];
		System.arraycopy(dataByte, 57, log_data_pack_index, 0, 4);
		resp.setLog_data_pack_index(PackerUtil.byteToInt(log_data_pack_index)+"");
		
		byte[] data_length=new byte[4];
		System.arraycopy(dataByte, 61, data_length, 0, 4);
		resp.setData_length(PackerUtil.byteToInt(data_length)+"");
		
		int body_len=dataByte.length-65;
		byte[] data_body=new byte[body_len];
		System.arraycopy(dataByte, 65, data_body, 0, body_len);
		resp.setData_body(PackerUtil.byteToCharsequence(data_body)+"");
		
		return resp;
		}catch(Exception e){
			log.error("CMD_parser_1306 parse error{}", e);
		}
		return null;
	}
}