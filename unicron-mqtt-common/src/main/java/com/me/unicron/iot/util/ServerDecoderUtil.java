package com.me.unicron.iot.util;

import java.util.HashMap;
import java.util.Map;

import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

@Slf4j
public class ServerDecoderUtil {

	private final static Map<String,String> _headCheckMap= new HashMap<String,String>();
	static{
		_headCheckMap.put("7dd0", "true");
		//_headCheckMap.put("7dd1", "true");
		//_headCheckMap.put("7dd2", "true");
		//_headCheckMap.put("7dd3", "true");
		//_headCheckMap.put("7dd4", "true");
		_headCheckMap.put("7dd5", "true");
		_headCheckMap.put("7dd6", "true");
		_headCheckMap.put("7dd8", "true");
		_headCheckMap.put("7ddf", "true");
	}
	public static  byte[] getHeadStartCode(byte[] data) {
		byte result[]=new byte[2];
		if(data!=null && data.length>=2){
			System.arraycopy(data, 0, result, 0, 2);
		}
		
		return result;
		
	}
	
	public static  String getHeadStartCodeString(byte[] data) {
		byte result[]=new byte[2];
		if(data!=null && data.length>=2){
			System.arraycopy(data, 0, result, 0, 2);
			return "0x"+EncodeUtil.bytesToHexString(result);
		}else{
			return "";
		}
		
		
		
	}
	
	public static  byte[] getHeadVersion(byte[] data) {
		byte result[]=new byte[4];
		if(data!=null && data.length>=8){
			System.arraycopy(data, 4, result, 0, 4);
		}
		
		return result;
		
	}
	
	
	
	public static int checkClientkProtocolType(byte[] data) {
		int protocolType=0;
		if (data == null || data.length < 2) {
            log.error("payload data error!checkClientkProtocolType:protocolType={}",protocolType);
            return 0;
        }
		if(EncodeUtil.getUnsignedByte(data[0])==EncodeUtil.getUnsignedByte((byte)0x6d)){
			protocolType = 1;
		}else{
			protocolType = 2;
		}
		log.info("checkClientkProtocolType:protocolType={}",protocolType);
		return protocolType;
		
	}
	
	private static String  getClientkProtocolVersionSub(byte[] data) {
		 String ver="0";
		 if(data == null || data.length < 4){
			 return ver;
		 }
		 ver=String.format("%02d%02d%02d%02d", data[0],data[1],data[2],data[3]);
		 return ver;
	 }

	public static String  getClientkProtocolVersion(byte[] dataBody) {
		 String ver="0";
		 if(dataBody == null || dataBody.length < 8){
			 return ver;
		 }
		 byte data[]=new byte[4];
		 System.arraycopy(dataBody, 4, data, 0, 4);
		 
		 return getClientkProtocolVersionSub(data);
	 }
	
	public static  byte[] getHeadIndex(byte[] data) {
		byte result[]=new byte[4];
		if(data!=null && data.length>=12){
			System.arraycopy(data, 8, result, 0, 4);
		}
		
		return result;
		
	}
	
	public static int  getClientkDataIndex(byte[] dataBody) {
		 
		 if(dataBody == null || dataBody.length < 12){
			 return 0;
		 }
		 byte data[]=new byte[4];
		 System.arraycopy(dataBody, 8, data, 0, 4);
		 
		 return EncodeUtil.byteToInt(data);
	 }
	
    public static boolean checkClientkMsg(byte[] data) {

        if (data == null || data.length < 2) {
            log.error("payload data error!");
            return false;
        }
        String key = EncodeUtil.bytesToHexString(new byte[]{data[0],data[1]});
        if(!StringUtils.isEmpty(key) && _headCheckMap.get(key.toLowerCase())!=null){
        	return true;
        }else{
        	return false;
        }
        /*
        boolean headCheckResult=false;
        if(EncodeUtil.getUnsignedByte(data[0]) == EncodeUtil.getUnsignedByte(MqttNetMsg.HEADER1) && EncodeUtil.getUnsignedByte(data[1]) == EncodeUtil.getUnsignedByte(MqttNetMsg.HEADER2)){
        		headCheckResult=true;
        	}
        	//前两字节
        	byte b[]=new byte[2];
        	System.arraycopy(data, 0, b, 0, 2);
        	if(EncodeUtil.getUnsignedShort(EncodeUtil.byteToShort(b))==MqttNetMsg.HEADER_ZH){
        		headCheckResult=true;
        	}
        	
        if(!headCheckResult) {
        		EncodeUtil.printHex(b);
            //EncodeUtil.printHex(new byte[]{(byte)MqttNetMsg.HEADER2,(byte)MqttNetMsg.HEADER2});
            log.error("payload header error!");
            return false;

        }
        byte len[] = new byte[2];
        System.arraycopy(data, 2, len, 0, 2);
        //日志数据长度超长
        if(EncodeUtil.byteToShort(len) == 0x7fff){
        		return true;
        }
        if (len[0] == 127 && len[1] == -1) {
            return true;
        }

        if (EncodeUtil.byteToShort(len) != data.length) {
            log.error("payload data(lenth) error!");
            return false;

        }
        */
        
    }

    public static void main(String[] args) throws Exception {
    	System.out.println(getClientkProtocolVersion(new byte[]{0x00,0x0,0x0,0x0,0x01,0x01,0x0d,0x02}));
        System.out.println(EncodeUtil.bytesToHexString(new byte[]{MqttNetMsg.HEADER1,MqttNetMsg.HEADER2}));
        System.out.println(EncodeUtil.getUnsignedByte(MqttNetMsg.HEADER2));
        EncodeUtil.printHex(EncodeUtil.longToByte(0x7fff));
        EncodeUtil.print(EncodeUtil.longToByte(0x7fff));
        String str=getHeadStartCodeString(new byte[]{0x7d ,(byte)0xff,0x0,0x0});
        System.out.println(str);
        System.out.println(0xFFFF);
    }
}
