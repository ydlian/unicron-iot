package com.me.unicron.iot.message.parser.impl;

import com.me.unicron.iot.errorcode.CodeTranser;
import com.me.unicron.iot.errorcode.HeadCode;
import com.me.unicron.iot.message.parser.BaseParser;
import org.apache.commons.lang3.StringUtils;

import com.me.epower.direct.entity.upward.StationFaultReport_118;
import com.me.epower.direct.entity.upward.StationFaultReport_118_ext;
import com.me.unicron.EncodeUtil;

import lombok.extern.slf4j.Slf4j;
/**
 * @author lianyadong
 * @func：(CODE=118)充电桩故障上报
 */

@Slf4j
public class CMD_parser_118 implements BaseParser {
	public static final int CMD_NO = 118;
	

    private static final String NULL_RESULT = "0";
    
    public static final String SUCCESS_RESULT = "0000";

	public StationFaultReport_118_ext unpack(String headStartStr,String version,byte[] dataByte) {
		StationFaultReport_118_ext resp = new StationFaultReport_118_ext();
		try {
			byte[] equipmentid = new byte[32];
			System.arraycopy(dataByte, 0, equipmentid, 0, 32);
			resp.setEquipmentid(EncodeUtil.byteToCharsequence(equipmentid, true));

			byte[] gun_no = new byte[1];
			System.arraycopy(dataByte, 32, gun_no, 0, 1);
			resp.setGun_no(EncodeUtil.byteToValue(gun_no[0]) + "");

			byte[] errcode = new byte[4];
			byte[] byt_4 = new byte[4];
            
            
			System.arraycopy(dataByte, 33, errcode, 0, 4);
			System.arraycopy(errcode, 0, byt_4, 0, 4);
			
			resp.setErrorcodeBytes(byt_4);
			
			String queryResult = EncodeUtil.byteToCharsequence(byt_4,true);
			  
			if(headStartStr!=null){
            	if(headStartStr.toLowerCase().equals(HeadCode.CLIENT_ZHONGHENG)){
            		if(EncodeUtil.byteToInt(byt_4)!=0 && byt_4[2]==0 && byt_4[3]==0){
                		//最高位都是0
            			queryResult= CodeTranser.stopReason4ByteHexToAscii(byt_4);
                	}
            		
            	}else if(headStartStr.toLowerCase().equals(HeadCode.CLIENT_ZHICHONG)){
            		if(version.compareTo("01011400")<0){
            			queryResult=CodeTranser.stopReasonToNormalZhichongBeforeV01011400(queryResult);
            		}
            		
            		queryResult=ZhichongCode2CloudErrCode.TranseCode(queryResult, true);
            	}
            	
            	//全部大写
            	if(queryResult!=null){
            		queryResult=queryResult.toUpperCase();
            	}
            	
            	queryResult=CodeTranser.stopReasonToNormal(queryResult);
            	queryResult=CodeTranser.formatAllZeroCode(queryResult);
            	
            }
            if(StringUtils.isBlank(queryResult)){
                queryResult = NULL_RESULT;
            }
            //HEX->int
            queryResult=CodeTranser.formatChargeCloudErrCodeName2IntVal(queryResult);
            if(StringUtils.isNotBlank(queryResult)&&SUCCESS_RESULT.equals(queryResult)){
                queryResult = NULL_RESULT;
            }
			resp.setErrcode(queryResult);

			byte[] status = new byte[1];
			System.arraycopy(dataByte, 37, status, 0, 1);
			resp.setStatus(EncodeUtil.bytesToValue(status));

			return resp;
		} catch (Exception e) {
			log.error("CMD_parser_118 parse error{}", e);
		}
		return null;

	}
}
