package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;

import com.me.epower.direct.entity.downward.PowerStrategyDispatch_15;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_15 implements BaseCMD<PowerStrategyDispatch_15>{

    private static final int BODY_LENGTH = 38;//+n
    private static final int CMD_NO = 15;
    
	@Override
	public byte[] getPayload(PowerStrategyDispatch_15 powerStrategyDispatch_15, MqttNetMsgBase base) {
		
        String equipmentId = powerStrategyDispatch_15.getBaseEquipmentId();
        if (StringUtils.isBlank(equipmentId)) {
        	log.info("equipmentId not set!");
        }
        int unitSize=0;
        try{
        	unitSize=Integer.parseInt(powerStrategyDispatch_15.getUnit_num());
        }catch(Exception e){
        	e.printStackTrace();
        }
        		
		int totalLen = base.getLength(BODY_LENGTH+unitSize);
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

			
			byte[] gunno = new byte[]{(byte) Integer.parseInt(powerStrategyDispatch_15.getGun_no())};
			System.arraycopy(gunno, 0, data, offset, 1);
			offset += 1;
			
			byte[] stategyType =  new byte[]{(byte) Integer.parseInt(powerStrategyDispatch_15.getStategy_type())};
			System.arraycopy(stategyType, 0, data, offset, 1);
			offset += 1;
			
			int unitSizeVal=Integer.parseInt(powerStrategyDispatch_15.getUnit_num());
			byte[] unitnum = EncodeUtil.intToByte(unitSizeVal);
			System.arraycopy(unitnum, 0, data, offset, 4);
			offset += 4;
			
			if(unitSizeVal>0){
				
				for(int i=0;i<unitSizeVal;i++){
					byte[] unitArray=new byte[]{(byte) Integer.parseInt(powerStrategyDispatch_15.getUnitIndex()[i])};
					System.arraycopy(unitArray, 0, data, offset, 1);
					offset += 1;
				}
				
			}
			
			
			byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
			System.arraycopy(checkSum, 0, data, totalLen-1, 1);

		} catch (Exception e) {
			// TODO: handle exception
			log.info(e.getMessage());
		}

		return data;
	}

	@Override
	public String pack(PowerStrategyDispatch_15 t, MqttNetMsgBase base) {
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
