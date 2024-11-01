package com.me.unicron.iot.message.parser.impl;

import com.me.epower.direct.entity.upward.StationChargeUnitQueryResponse_14;
import com.me.epower.direct.entity.upward.StationPowerStategyDispatchQueryResponse_18;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.parser.BaseParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_parser_18 implements BaseParser {
	public static final int CMD_NO = 18;

	public StationPowerStategyDispatchQueryResponse_18 unpack(byte[] dataByte) {
		try {

			StationPowerStategyDispatchQueryResponse_18 resp = new StationPowerStategyDispatchQueryResponse_18();
			byte[] equipmentBytes = new byte[32];
			System.arraycopy(dataByte, 0, equipmentBytes, 0, equipmentBytes.length);
			resp.setEquipmentId(EncodeUtil.byteToCharsequence(equipmentBytes, true));
			
			byte[] byte1 = new byte[1];
			System.arraycopy(dataByte, 32, byte1, 0, 1);
			int gun_noVal=byte1[0];
			resp.setGun_no(gun_noVal+"");
			
			byte1 = new byte[1];
			System.arraycopy(dataByte, 33, byte1, 0, 1);
			int typeVal=byte1[0];
			resp.setStategy_type(typeVal+"");

			byte[] byte4 = new byte[4];
			System.arraycopy(dataByte, 34, byte4, 0, 4);
			int unitNum=EncodeUtil.byteToInt(byte4);
			resp.setUnit_num(""+unitNum);
			if(unitNum>0){
				String[] unitIndex=new String[unitNum];
				
				for(int i=0;i<unitNum;i++){
					byte[] unitIndexByte = new byte[1];
					System.arraycopy(dataByte, 38+i*1, unitIndexByte, 0, 1);
					
					unitIndex[i]=""+EncodeUtil.byteToValue(unitIndexByte[0]);
					
				}

				resp.setUnitIndex(unitIndex);
				
			}
			
			
			return resp;

		} catch (Exception e) {
			log.error("CMD_parser_18 parse error{}", e);
		}
		return null;
	}
}
