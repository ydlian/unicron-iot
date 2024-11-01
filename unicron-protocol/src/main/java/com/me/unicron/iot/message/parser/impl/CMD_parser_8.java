package com.me.unicron.iot.message.parser.impl;

import org.apache.commons.lang3.StringUtils;

import com.me.epower.direct.entity.upward.StartChargeResponse_8;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.errorcode.CodeTranser;
import com.me.unicron.iot.errorcode.HeadCode;
import com.me.unicron.iot.message.parser.BaseParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_parser_8 implements BaseParser {

    public static final int CMD_NO = 8;
    
    private static final String NULL_RESULT = "0";
    
    public static final String SUCCESS_RESULT = "0000";

    public StartChargeResponse_8 unpack(String headStartStr,String version,byte[] dataByte) {
        try {
            // 命令类型
            byte[] byt_1 = new byte[1];
            // 命令类型
            StartChargeResponse_8 resp = new StartChargeResponse_8();
            byte[] reserved1 = new byte[2];
            System.arraycopy(dataByte, 0, reserved1, 0, 2);
            resp.setReserved1(EncodeUtil.byteToShort(reserved1) + "");
            byte[] reserved2 = new byte[2];
            System.arraycopy(dataByte, 2, reserved2, 0, 2);
            resp.setReserved2(EncodeUtil.byteToShort(reserved2) + "");

            byte[] stationId = new byte[32];
            System.arraycopy(dataByte, 4, stationId, 0, 32);
            String equipmentId=EncodeUtil.byteToCharsequence(stationId, true);
            resp.setEquipmentId(equipmentId);

            // 枪口
            System.arraycopy(dataByte, 36, byt_1, 0, 1);
            resp.setGun_no(EncodeUtil.byteToValue(byt_1[0]));
            // 命令执行结果
            byte[] byt_4 = new byte[4];
            System.arraycopy(dataByte, 37, byt_4, 0, 4);
            String queryResult = EncodeUtil.byteToCharsequence(byt_4,true);
            //存储原始字节
            resp.setReserved1(EncodeUtil.printHex(byt_4));
            if(StringUtils.isBlank(queryResult)){
            	queryResult=SUCCESS_RESULT;
            }
            resp.setReserved2(queryResult);
            
            if(headStartStr!=null){
            	if(headStartStr.toLowerCase().equals(HeadCode.CLIENT_ZHONGHENG)){
            		if(EncodeUtil.byteToInt(byt_4)!=0 && byt_4[2]==0 && byt_4[3]==0){
                		//最高位都是0
            			queryResult=CodeTranser.stopReason4ByteHexToAscii(byt_4);
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
            	
            	log.info("equipmentId={},stopReasonToNormal={}",equipmentId,queryResult);
            }
            if(StringUtils.isBlank(queryResult)){
                queryResult = NULL_RESULT;
            }
            //HEX->int
            queryResult=CodeTranser.formatChargeCloudErrCodeName2IntVal(queryResult);
            if(StringUtils.isNotBlank(queryResult)&&SUCCESS_RESULT.equals(queryResult)){
                queryResult = NULL_RESULT;
            }
            
            resp.setQuery_result(queryResult);
            return resp;
        } catch (Exception e) {
            log.error("CMD_parser_8 parse error", e);
        }
        return null;
    }
}
