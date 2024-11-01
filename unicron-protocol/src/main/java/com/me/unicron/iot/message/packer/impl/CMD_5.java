package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.me.epower.direct.entity.downward.ChargeControlQuery_5;
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
 * @func：(CODE=5)后台服务器下发充电桩控制命令
 */
@Slf4j
public class CMD_5 implements BaseCMD<ChargeControlQuery_5> {

    //默认为查询时长度
    private static final int BODY_LENGTH = 12;
    private static final int CMD_NO = 5;
    //private static short inc = 0;

    @Override
    public byte[] getPayload(ChargeControlQuery_5 chargeControlQuery_5,MqttNetMsgBase base) {
        int bodyLenth = BODY_LENGTH;

        String start_no = chargeControlQuery_5.getStart_no();
        if (StringUtils.isBlank(start_no) || !NumberUtils.isNumber(start_no)) {
        	log.info("start_no not set!");
        }

        //
        String cmd_size = chargeControlQuery_5.getCmd_size();
        if (StringUtils.isBlank(cmd_size) || !NumberUtils.isNumber(cmd_size)) {
        	log.info("cmd_size not set!");
        }

        String gun_no = chargeControlQuery_5.getGun_no();
        if (StringUtils.isBlank(gun_no) || !NumberUtils.isNumber(gun_no)) {
        	log.info("gun_no not set!");
        }

        //short start_no_val=(short) Integer.parseInt(start_no);

        bodyLenth = BODY_LENGTH + 4 * Integer.parseInt(cmd_size);

        int totalLen = base.getLength(bodyLenth);
        byte[] data = new byte[totalLen];

        data=base.fillPayload2CmdNo(data, totalLen, (short) CMD_NO);
        int bytesOffset = base.getBytesOffset();

        //body部分，4字节保留
        byte[] reserved = new byte[4];
        System.arraycopy(reserved, 0, data, 8+bytesOffset, 4);
        //命令类型
        byte[] byt_gun_no = new byte[]{EncodeUtil.ValueToByte(gun_no)};
        System.arraycopy(byt_gun_no, 0, data, 12+bytesOffset, 1);
        //命令启始标志
        byte[] start_no_byt = EncodeUtil.intToByte(Integer.parseInt(start_no));

        System.arraycopy(start_no_byt, 0, data, 13+bytesOffset, 4);

        //命令个数 
        byte[] para_byte_size = new byte[]{EncodeUtil.ValueToByte(cmd_size)};

        System.arraycopy(para_byte_size, 0, data, 17+bytesOffset, 1);

        //命令参数长度 4*Integer.parseInt(cmd_size)

        String cmd_length = chargeControlQuery_5.getCmd_length();
        short cmd_para_size_short = Short.parseShort(cmd_length);
        System.arraycopy(EncodeUtil.shortToByte(cmd_para_size_short), 0, data, 18+bytesOffset, 2);

        //具体命令参数
        byte[] cmd_data_byte = new byte[cmd_para_size_short];
		cmd_data_byte=chargeControlQuery_5.getData_body();
        System.arraycopy(cmd_data_byte, 0, data, 20+bytesOffset, cmd_para_size_short);

        byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
        System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + bodyLenth, 1);
        
        
        return data;
    }
    @Override
    public String pack(ChargeControlQuery_5 chargeControlQuery_5,MqttNetMsgBase base) {
        // TODO Auto-generated method stub
        byte[] data = getPayload(chargeControlQuery_5,base);
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

   

    public static void print() {
        CMD_5 cmd = new CMD_5();
        Map<String, String> dataMap = new HashMap<String, String>();
        dataMap.put("gun_no", "1");
        dataMap.put("cmd_size", "1");
        dataMap.put("start_no", "1");
        //125 -33 37 0 1 0 1 0 0 0 0 0 1 1 0 0 0 4 16 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 26 
        //125 -33 41 0 1 0 1 0 0 0 0 0 1 1 0 0 0 5 20 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 35 
        //125 -33 21 0 1 0 1 0 0 0 0 0 0 1 0 0 0 5 0 0 121 
        //编码前：
        //byte[] byt = cmd.getPayload(dataMap);
        log.info("编码前：");
        //EncodeUtil.print(byt);
    }

    public static void main(String[] args) throws Exception {
        print();
    }

}
