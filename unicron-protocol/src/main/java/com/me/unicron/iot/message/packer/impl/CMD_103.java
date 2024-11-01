package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;

import com.me.epower.direct.entity.downward.StationInfoQueryResponse_103;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.math.NumberUtils;
//
/**
 * @author lianyadong
 * @func：(CODE=103)服务器应答充电桩状态信息包
 */
@Slf4j
public class CMD_103 implements BaseCMD<StationInfoQueryResponse_103> {
	//默认为查询时长度
	private static final int BODY_LENGTH = 42;
	private static final int CMD_NO = 103;
	//private static short inc = 0;
	
	@Override
	public byte[] getPayload(StationInfoQueryResponse_103 resp,MqttNetMsgBase base) {
		int bodyLenth=BODY_LENGTH;
		String gun_no=resp.getGun_no();
		if(StringUtils.isBlank(gun_no) || !NumberUtils.isNumber(gun_no)){
			//throw new MqttParaException("gun_no not set!");
		}
		
		String charge_card_id=resp.getCard_no_user_id();
		if(StringUtils.isBlank(charge_card_id)){
			//throw new MqttParaException("charge_card_id not set!");
		}
		
		
		bodyLenth=BODY_LENGTH;
		int totalLen = base.getLength(bodyLenth);
		byte[] data = new byte[totalLen];
		data=base.fillPayload2CmdNo(data, totalLen, (short) CMD_NO);
        int bytesOffset = base.getBytesOffset();
        /*
		System.arraycopy(MqttNetMsg.getHerder(), 0, data, 0, 2);
		byte[] length = EncodeUtil.shortToByte((short) totalLen);
		System.arraycopy(length, 0, data, 2, 2);
		byte[] ver = MqttNetMsg.getVersion();
		System.arraycopy(ver, 0, data, 4, 1);
		byte[] index = MqttNetMsg.getIndex();
		System.arraycopy(index, 0, data, 5, 1);
		// byte[] code=MqttNetMsg.getCmdCode();
		System.arraycopy(EncodeUtil.shortToByte((short) CMD_NO), 0, data, 6, 2);
		*/
		//body部分，4字节保留
		byte[] reserved1=new byte[2];
		reserved1=EncodeUtil.shortToByte((short)Integer.parseInt(resp.getReserved1()));
		
		System.arraycopy(reserved1, 0, data, 8+bytesOffset, 2);
		
		byte[] reserved2=new byte[2];
		reserved2=EncodeUtil.shortToByte((short)Integer.parseInt(resp.getReserved2()));
		
		
		System.arraycopy(reserved2, 0, data, 10+bytesOffset, 2);
		
		//充电枪口
		//byte[] byt_gun_no=new byte[1];//new byte[]{(byte)Integer.parseInt(gun_no)};
        byte[] gun_no_byte = new byte[]{EncodeUtil.ValueToByte(gun_no)};
		
		System.arraycopy(gun_no_byte, 0, data, 12+bytesOffset, 1);
		//充电卡号（保留）32
		byte[] byte32 = new byte[32];
		byte[] card_id_byts = null;
        try {
            card_id_byts = resp.getCard_no_user_id().getBytes(CharsetDef.CHARSET);
        } catch (UnsupportedEncodingException e) {
            log.error("CMD_103 parse error",e);
        }
		System.arraycopy(card_id_byts, 0, byte32, 0, card_id_byts.length);		

		System.arraycopy(byte32, 0, data, 13+bytesOffset, 32);
		
		//卡余额（保留）4
		byte[] account_money = new byte[4];
		account_money=EncodeUtil.intToByte(Integer.parseInt(resp.getCard_balance()));
		System.arraycopy(account_money, 0, data, 45+bytesOffset, 4);
		
		//余额是否足够（保留）1
		byte[] account_have_money = new byte[1];
		account_have_money=new byte[]{(byte)Integer.parseInt(resp.getBalance_sufficient())};//resp.getBalance_sufficient().getBytes();
		System.arraycopy(account_have_money, 0, data, 49+bytesOffset, 1);
		
		byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
		System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + bodyLenth, 1);
		return data;
	}

	
	@Override
	public String pack(StationInfoQueryResponse_103 resp,MqttNetMsgBase base) {
		byte[] data=getPayload(resp,base);
		// TODO Auto-generated method stub
		try {
			return new String(data,CharsetDef.CHARSET);
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


	public static void print(){
		CMD_103 cmd = new CMD_103();
		StationInfoQueryResponse_103 resp=new StationInfoQueryResponse_103();
		//125 -33 37 0 1 0 1 0 0 0 0 0 1 1 0 0 0 4 16 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 26 
		//125 -33 41 0 1 0 1 0 0 0 0 0 1 1 0 0 0 5 20 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 35 
		//125 -33 21 0 1 0 1 0 0 0 0 0 0 1 0 0 0 5 0 0 121 
		//编码前：
		byte[] byt = cmd.getPayload(resp,new MqttNetMsg());
		log.info("编码前：");
		EncodeUtil.print(byt);
	}
	public static void main(String[] args) throws Exception {
		print();
	}





}
