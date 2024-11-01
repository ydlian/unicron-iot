package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;

import com.me.epower.direct.entity.downward.StationIntPara_1;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.math.NumberUtils;
@Slf4j
public class CMD_1 implements BaseCMD<StationIntPara_1> {
	//默认为查询时长度
	private static final int BODY_LENGTH = 12;
	private static final int CMD_NO = 1;
	//private static short inc = 0;
	@Override
	public byte[] getPayload(StationIntPara_1 query,MqttNetMsgBase base) {
		
		//数据域部分长度
		int bodyLenth=BODY_LENGTH;
		String para_cnt=query.getPara_cnt();
		if(StringUtils.isBlank(para_cnt) || !NumberUtils.isNumber(para_cnt)){
			log.info("para_cnt not set!");
			return null;
		}
		
		if(Integer.parseInt(para_cnt)>10){
			log.info("para_cnt must <= 10!");
			return null;
		}
		
		String start_addr=query.getStart_addr();
		if(StringUtils.isBlank(start_addr) || !NumberUtils.isNumber(start_addr)){
			log.info("start_addr not set!");
			return null;
		}
		
		
		String cmd_type=query.getCmd_type();
		if(StringUtils.isBlank(cmd_type) || !NumberUtils.isNumber(cmd_type)){
			log.info("cmd_type not set!");
			return null;
		}
		if(Integer.parseInt(cmd_type)==1){
			bodyLenth=BODY_LENGTH+Integer.parseInt(para_cnt)*4;
		}
		
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
		byte[] reserved = new byte[4];
		System.arraycopy(reserved, 0, data, 8+bytesOffset, 4);
		//命令类型
		byte[] byt_type=new byte[]{(byte)Integer.parseInt(cmd_type)};
		System.arraycopy(byt_type, 0, data, 12+bytesOffset, 1);
		//设置/查询参数启始地址
		int start_addr_int=Integer.parseInt(start_addr);
		byte para_cnt_val=(byte) Integer.parseInt(para_cnt);
		System.arraycopy(EncodeUtil.intToByte(start_addr_int), 0, data, 13+bytesOffset, 4);
		//设置/查询个数
		System.arraycopy(new byte[]{para_cnt_val}, 0, data, 17+bytesOffset, 1);
		
		//设置参数字节数
		System.arraycopy(EncodeUtil.shortToByte((short) (para_cnt_val*4)), 0, data, 18+bytesOffset, 2);

		//设置数据
		if(Integer.parseInt(cmd_type)==1){
			byte[] bodyDataCont =EncodeUtil.dataBody4BitString2Bytes(query.getData_body(), para_cnt_val);
			System.arraycopy(bodyDataCont, 0, data, 20+bytesOffset, para_cnt_val*4);
		}
		byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
		System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + bodyLenth, 1);
		
		EncodeUtil.printHex(data);
		return data;
	}

	@Override
	public String pack(StationIntPara_1 query,MqttNetMsgBase base) {
		// TODO Auto-generated method stub
		byte[] data = getPayload(query,base);
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
		CMD_1 cmd = new CMD_1();
		StationIntPara_1 query=new StationIntPara_1();
		query.setCmd_type("0");
		query.setPara_cnt("5");
		query.setStart_addr("1");
		
		//125 -33 37 0 1 0 1 0 0 0 0 0 1 1 0 0 0 4 16 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 26 
		//125 -33 41 0 1 0 1 0 0 0 0 0 1 1 0 0 0 5 20 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 35 
		//125 -33 21 0 1 0 1 0 0 0 0 0 0 1 0 0 0 5 0 0 121 
		//编码前：
		MqttNetMsgBase base=new MqttNetMsg();
		byte[] byt = cmd.getPayload(query,base);
		log.info("编码前：");
		EncodeUtil.print(byt);
	}
	public static void main(String[] args) throws Exception {
		print();
	}


	

}
