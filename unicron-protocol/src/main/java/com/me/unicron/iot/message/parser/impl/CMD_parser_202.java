package com.me.unicron.iot.message.parser.impl;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;

import com.me.epower.direct.entity.upward.OrderInfoResponse_202;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.errorcode.CodeTranser;
import com.me.unicron.iot.errorcode.HeadCode;
import com.me.unicron.iot.message.parser.BaseParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_parser_202 implements BaseParser {
	private static final String NULL_RESULT = "0";
    
    public static final String SUCCESS_RESULT = "0000";

    public OrderInfoResponse_202 unpack(String headStartStr,String version,byte[] buf) {
        byte[] shortByte = new byte[2];
        byte[] intByte = new byte[4];
        byte[] longByte = new byte[8];
        byte[] byte32 = new byte[32];
        OrderInfoResponse_202 chargeInfo = new OrderInfoResponse_202();
        try {
            for (int i = 0; i < buf.length;) {
                //保留4字节
                i += 4;
                //充电桩编码
                System.arraycopy(buf, i, byte32, 0, 32);
                String equipmentId =  EncodeUtil.byteToCharsequence(byte32, true);
                i += 32;
                chargeInfo.setEquipmentId(equipmentId);
                //充电枪类型
                byte portType = buf[i];
                chargeInfo.setPortType(EncodeUtil.byteToValue(portType));
                i++;
                //充电枪口
                byte portId = buf[i];
                chargeInfo.setPortId(EncodeUtil.byteToValue(portId));
                i++;
                //充电卡号/用户号（保留）
                System.arraycopy(buf, i, byte32, 0, 32);
                String str32 = EncodeUtil.byteToCharsequence(byte32, true);
                chargeInfo.setCardNum(str32);
                i += 32;
                //充电开始时间
                System.arraycopy(buf, i, longByte, 0, 8);
                chargeInfo.setStartTime(EncodeUtil.byteDateToDateStr(longByte));
                i += 8;
                //充电结束时间
                System.arraycopy(buf, i, longByte, 0, 8);
                chargeInfo.setEndTime(EncodeUtil.byteDateToDateStr(longByte));
                i += 8;
                //充电时间长度
                System.arraycopy(buf, i, intByte, 0, 4);
                chargeInfo.setChargeTime(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;
                //开始SOC
                byte startSoc = buf[i];
                chargeInfo.setStartSoc(EncodeUtil.byteToValue(startSoc));
                i++;
                //结束SOC
                byte endSoc = buf[i];
                chargeInfo.setEndSoc(EncodeUtil.byteToValue(endSoc));
                i++;
                //充电结束原因
                System.arraycopy(buf, i, intByte, 0, 4);
                String queryResult=String.valueOf(EncodeUtil.byteToCharsequence(intByte, true));
                //存储原始字节
                //add 2023-08-28 暂时使用保留字段1存储结束原因的原始编码
                chargeInfo.setReserved1(EncodeUtil.printHex(intByte));
                if(StringUtils.isBlank(queryResult)){
                	queryResult="0000";
                }
                chargeInfo.setReserved2(queryResult);
                
                
                if(headStartStr!=null){
                	if(headStartStr.toLowerCase().equals(HeadCode.CLIENT_ZHONGHENG)){
                		if(intByte[2]==0 && intByte[3]==0){
                    		//最高位都是0
                			queryResult=CodeTranser.stopReason4ByteHexToAscii(intByte);
                    	}
                		
                	}else if(headStartStr.toLowerCase().equals(HeadCode.CLIENT_ZHICHONG)){
                		
                		queryResult=CodeTranser.stopReasonToNormalZhichongBeforeV01011400(queryResult);
                		
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
                //都是10进制的,4位数
                queryResult=CodeTranser.formatChargeCloudErrCodeName2IntVal(queryResult);
                if(StringUtils.isNotBlank(queryResult)&&SUCCESS_RESULT.equals(queryResult)){
                    queryResult = NULL_RESULT;
                }
                
                chargeInfo.setStopReason(queryResult);
                
               
                
                i += 4;
                //本次充电电量
                System.arraycopy(buf, i, intByte, 0, 4);
                
                chargeInfo.setTotalPower(String.valueOf(EncodeUtil.byteToInt(intByte)));
                //log.info("intByte={},val={}",EncodeUtil.printHex(intByte),String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;
                //充电前电表读数
                System.arraycopy(buf, i, intByte, 0, 4);
                chargeInfo.setStartMeterReading(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;
                //充电后电表读数
                System.arraycopy(buf, i, intByte, 0, 4);
                chargeInfo.setEndMeterReading(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;
                //本次充电金额
                System.arraycopy(buf, i, intByte, 0, 4);
                chargeInfo.setTotalMoney(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;
                //是否不刷卡结束（保留）
                i += 4;
                //充电前卡余额（保留）
                i += 4;
                //充充电后卡余额（保留）
                i += 4;
                //服务费金额
                System.arraycopy(buf, i, intByte, 0, 4);
                chargeInfo.setServiceMoney(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;
                //是否线下支付（保留）
                i++;
                //充电策略
                byte chargePolicy = buf[i];
                chargeInfo.setChargePolicy(EncodeUtil.byteToValue(chargePolicy));
                i++;
                //充电策略参数
                i += 4;
                //车辆 VIN
                byte[] vinByte = new byte[17];
                System.arraycopy(buf, i, vinByte, 0, 17);
                chargeInfo.setCardVIN(EncodeUtil.byteToCharsequence(vinByte,true));
                i += 17;
                //车牌号
                System.arraycopy(buf, i, longByte, 0, 8);
                chargeInfo.setCarNum(String.valueOf(EncodeUtil.byteToLong(longByte)));
                i += 8;
                for (int j = 1; j <= 48; j++) {
                    System.arraycopy(buf, i, shortByte, 0, 2);
                    String powerPeriodStr = String.valueOf(EncodeUtil.byteToShort(shortByte));
                    Method powerPeriod;
                    try {
                        powerPeriod = OrderInfoResponse_202.class.getMethod("setPowerPeriod" + j, String.class);
                        powerPeriod.invoke(chargeInfo, powerPeriodStr);
                    } catch (Exception e) {

                    }
                    i += 2;
                }
                //启动方式
                byte startType = buf[i];
                chargeInfo.setStartType(EncodeUtil.byteToValue(startType));
                i++;
                //校验和
                i++;
                return chargeInfo;
            }

        } catch (Exception e) {
        	log.error("CMD_parser_202 parse error", e);
        }
        return null;
    }

}
