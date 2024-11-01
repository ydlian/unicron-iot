package com.me.unicron.iot.errorcode;

import org.apache.commons.lang.StringUtils;

import com.me.unicron.EncodeUtil;
import com.me.unicron.Enum.errorcode.ChargeCloudErrCode;

public class CodeTranser {

	
	public static String formatAllZeroCode(String code){
		String result="0";
		if(StringUtils.isBlank(code)){
			return result;
		}
		int size=0;
		for(int i=0;i<code.length();i++){
			if(code.charAt(i)=='0'){
				size++;
			}
		}
		if(size==code.length()){
			result="0";
		}else{
			result=code;
		}
		return result;
	}
	
	public static String formatChargeCloudErrCodeName2IntVal(String codeName){
		String result="0";
		if(StringUtils.isBlank(codeName)){
			return result;
		}
		if(codeName.length()<4){
			return codeName;
		}
		int val=0;

    	ChargeCloudErrCode[] types = ChargeCloudErrCode.values();
    	 for (ChargeCloudErrCode type:types) {
             if (type.getName().equals(codeName)) {
                 return String.valueOf(type.getValue());
             }
         }
		return val+"";
	}
	public static String stopReasonToNormal(String code){
		String result="0";
		if(StringUtils.isBlank(code)){
			return result;
		}
		code=code.toUpperCase();
		if(code.equals(ChargeCloudErrCode.CHARGE_FULL.getName())
				||code.equals(ChargeCloudErrCode.APP_WECHAT_STOP_CHARGE.getName())
				||code.equals(ChargeCloudErrCode.ACCOUNT_INSUFFICIENT.getName())
				||code.equals(ChargeCloudErrCode.DEVICE_SCREEN_MANUAL_STOP.getName())
				||code.equals(ChargeCloudErrCode.SERVER_BACKEND_STOP.getName())
				||code.equals(ChargeCloudErrCode.CHARGE_TIME_LIMIT_STOP.getName())
				||code.equals(ChargeCloudErrCode.CHARGE_DEGREE_LIMIT_STOP.getName())
				||code.equals(ChargeCloudErrCode.CHARGE_AMOUNT_LIMIT_STOP.getName())
				||code.equals(ChargeCloudErrCode.CHARGE_SWING_CARD_STOP.getName())
				||code.equals(ChargeCloudErrCode.BMS_REACH_CONDITION_STOP.getName())
				||code.equals(ChargeCloudErrCode.STATION_REACH_CONDITION_STOP.getName())
				||code.equals(ChargeCloudErrCode.HAVE_NON_STOP_ORDER.getName())
				
				){
			result="0";
		}else{
			result=code;
		}
		if(ChargeCloudErrCode.STATION_START_CHARGE_FAILED.getName().equals(result)){
			result=ChargeCloudErrCode.START_CHARGE_TIMEOUT.getName();
		}
		return result;
	}
	
	public static String stopReasonToNormalZhichongBeforeV01011400(String code){
		String result="0";
		if(StringUtils.isBlank(code)){
			return result;
		}
		code=code.toUpperCase();
		if(code.equals("4003")
				||code.equals("3001")
				||code.equals("4020")
				||code.equals("A000")
				||code.equals("3001")
				){
			result="0";
		}else{
			result=code;
		}
		return result;
	}
	
	public static String stopReason4ByteHexToAscii(byte[] codeByte){
		String result="0";
		byte[] intByte = new byte[2];
		if(codeByte==null || codeByte.length<4){
			return result;
		}
		System.arraycopy(codeByte, 0, intByte, 0, 2);
		result=EncodeUtil.bytesToReverseHexString(intByte);
		return result;
	}
	//insert into ep_operator_deivce values("913300002539163407","杭州电气股份有限公司","019218090001","1","101437000",250,750,'','',"10437000_13","武汉体育中心充电站");
	public static void main(String[] args) throws Exception {
		byte arr[] =new byte[]{0x0d,0x10,0x00,0x00};
		System.out.println("测试时间："+stopReason4ByteHexToAscii(arr));
		String version="01011302";
		System.out.println(StringUtils.leftPad("0", 4, '0'));
		//System.out.println(String.format("%02d%4s",ChargeStageEnum.STARTING_PRE.getStage(),"0"));
		System.out.println(version.compareTo("01011400"));
		System.out.println(formatChargeCloudErrCodeName2IntVal("100D"));
		System.out.println(ChargeCloudErrCode.getByName("100D"));
	}
}
