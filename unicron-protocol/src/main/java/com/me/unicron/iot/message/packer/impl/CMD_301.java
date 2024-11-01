package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import com.me.epower.direct.entity.upward.BMSInfoResponse_301;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;

/**
 * @author fzl func：(CODE=401)服务器应答充电桩未上传历史充电记录
 */
@Slf4j
public class CMD_301 implements BaseCMD<BMSInfoResponse_301> {

    private static final int BODY_LENGTH = 36;
    private static final int CMD_NO = 301;
    @Override
    public byte[] getPayload(BMSInfoResponse_301 bmsInfoResponse_301,MqttNetMsgBase base) {
        int totalLen = base.getLength(BODY_LENGTH);
        //起始域
        byte[] data = new byte[totalLen];
        data=base.fillPayload2CmdNo(data, totalLen, (short) CMD_NO);
        int bytesOffset = base.getBytesOffset();
        /*
        System.arraycopy(MqttNetMsg.getHerder(), 0, data, 0, 2);
        //长度域
        byte[] length = EncodeUtil.shortToByte((short) totalLen);
        System.arraycopy(length, 0, data, 2, 2);
        //版本域
        byte[] ver = MqttNetMsg.getVersion();
        System.arraycopy(ver, 0, data, 4, 1);
        //序列号域
        byte[] index = MqttNetMsg.getIndex();
        System.arraycopy(index, 0, data, 5, 1);
        //命令域
        System.arraycopy(EncodeUtil.shortToByte((short) CMD_NO), 0, data, 6, 2);
		*/
        
        try {
            //充电记录索引
            String chargeIndex = bmsInfoResponse_301.getIndex();
            byte[] chargeIndexBytes = EncodeUtil.intToByte(Integer.parseInt(chargeIndex));
            System.arraycopy(chargeIndexBytes, 0, data, 8+bytesOffset, 2);
            
            //槍號
            String gun_no = bmsInfoResponse_301.getGun_no();
            byte[] gun_noByte = EncodeUtil.intToByte(Integer.parseInt(gun_no));
            System.arraycopy(gun_noByte, 0, data, 10+bytesOffset, 2);
            
            //充电桩编码 32
            //byte[] station_id_byte = new byte[32];
            //byte[] equipmentIdByte = EncodeUtil.charsequenceToByte(bmsInfoResponse_301.getEquipmentId());
            //System.arraycopy(station_id_byte, 0, equipmentIdByte, 0, equipmentIdByte.length);
            //System.arraycopy(station_id_byte, 0, data, 12+bytesOffset, 32);
            
            //充电桩编码 32
            byte[] station_id_byte = new byte[32];
            if(bmsInfoResponse_301.getEquipmentId()!=null){
                station_id_byte = bmsInfoResponse_301.getEquipmentId().getBytes(CharsetDef.CHARSET);
            }
            byte[] byte32 = new byte[32];
            System.arraycopy(station_id_byte, 0, byte32, 0, station_id_byte.length);      
            System.arraycopy(byte32, 0, data, 12+bytesOffset, 32);
            
            byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
            System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + BODY_LENGTH, 1);
            
        } catch (Exception e) {
            log.error("CMD_301_ERROR", e);
        }
        
        return data;
    }

   

    public static void print() {
        
    }

    public static void main(String[] args) throws Exception {
        print();

    }



	@Override
	public String pack(BMSInfoResponse_301 t,MqttNetMsgBase base) {
		// TODO Auto-generated method stub
		byte[] data = getPayload(t,base);
        try {
            return new String(data, CharsetDef.CHARSET);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
	}



	@Override
	public int getCmdNo() {
		// TODO Auto-generated method stub
		return CMD_NO;
	}

   
}
