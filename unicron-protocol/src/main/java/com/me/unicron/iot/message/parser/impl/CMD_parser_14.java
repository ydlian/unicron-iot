package com.me.unicron.iot.message.parser.impl;

import com.me.unicron.iot.message.parser.BaseParser;
import com.me.epower.direct.entity.upward.StationChargeUnitQueryResponse_14;
import com.me.unicron.EncodeUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_parser_14 implements BaseParser {
	public static final int CMD_NO = 14;

	public StationChargeUnitQueryResponse_14 unpack(byte[] dataByte) {
		try {

			StationChargeUnitQueryResponse_14 resp = new StationChargeUnitQueryResponse_14();
			byte[] equipmentBytes = new byte[32];
			System.arraycopy(dataByte, 0, equipmentBytes, 0, equipmentBytes.length);
			resp.setEquipmentId(EncodeUtil.byteToCharsequence(equipmentBytes, true));
			
			byte[] unitSize = new byte[4];
			System.arraycopy(dataByte, 32, unitSize, 0, 4);
			int unitSizeVal=EncodeUtil.byteToInt(unitSize);
			resp.setChargeUnitSize(""+unitSizeVal);

			byte[] totalPower = new byte[4];
			System.arraycopy(dataByte, 36, totalPower, 0, 4);
			resp.setStationTotalPower(""+EncodeUtil.byteToInt(unitSize));
			if(unitSizeVal>0){
				String[] unitStatus=new String[unitSizeVal];
				String[] unitPower=new String[unitSizeVal];
				for(int i=0;i<unitSizeVal;i++){
					byte[] resultBytes = new byte[5];
					System.arraycopy(dataByte, 40+i*5, resultBytes, 0, 5);
					byte byte1 = resultBytes[0];
					unitStatus[i]=EncodeUtil.byteToValue(byte1);
					byte[] byte4 = new byte[4];
					System.arraycopy(resultBytes, 1, byte4, 0, 4);
					unitPower[i]=EncodeUtil.byteToInt(byte4)+"";
				}

				resp.setUnitStatus(unitStatus);
				resp.setUnitPower(unitPower);
				
			}
			
			
			return resp;

		} catch (Exception e) {
			log.error("CMD_parser_14 parse error{}", e.getMessage());
		}
		return null;
	}
}
