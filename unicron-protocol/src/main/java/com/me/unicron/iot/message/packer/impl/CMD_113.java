package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import com.me.unicron.iot.message.packer.BaseCMD;
import org.apache.commons.lang.StringUtils;

import com.me.epower.direct.entity.downward.StationQueryLogicInfoResponse_113;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.exception.MqttParaException;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.math.NumberUtils;

/**
 * @author lianyadong
 * @func：(CODE=113)中心服务器应答充电桩索取逻辑服务器信息
 */
@Slf4j
public class CMD_113 implements BaseCMD<StationQueryLogicInfoResponse_113> {

    private static final int BODY_LENGTH = 136;
    private static final int CMD_NO = 113;
    @Override
    public byte[] getPayload(StationQueryLogicInfoResponse_113 query,MqttNetMsgBase base) {
        String ip = query.getIp();
        if (StringUtils.isBlank(ip)) {
            throw new MqttParaException("ip not set!");
        }

        String port = query.getPort();
        if (StringUtils.isBlank(port) || !NumberUtils.isNumber(port)) {
            throw new MqttParaException("port not set!");
        }
        int totalLen = base.getLength(BODY_LENGTH);
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

        System.arraycopy(EncodeUtil.shortToByte((short) CMD_NO), 0, data, 6, 2);
        */
        
        //body部分，4字节保留
        byte[] reserved = new byte[4];
        System.arraycopy(reserved, 0, data, 8+bytesOffset, 4);

        //ip 4byte
        byte[] ip128Byte=new byte[128];
        byte[] ipByte = EncodeUtil.charsequenceToByte(query.getIp());
        System.arraycopy(ipByte, 0, ip128Byte, 0, ipByte.length);
       
        System.arraycopy(ip128Byte, 0, data, 12+bytesOffset, 128);

        //port 4byte
        byte[] portByte = EncodeUtil.port2Byte(query.getPort());
        System.arraycopy(portByte, 0, data, 140+bytesOffset, 4);

        //校验和		
        byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
        System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + BODY_LENGTH, 1);
        return data;
    }

    @Override
    public String pack(StationQueryLogicInfoResponse_113 query,MqttNetMsgBase base) {
        // TODO Auto-generated method stub
        byte[] sendBytes = getPayload(query,base);
        // byte[] sendBytes= sendString .getBytes("UTF8");
        String sendMsg = "";
        try {
            
            sendMsg = new String(sendBytes, CharsetDef.CHARSET);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return sendMsg;
    }

    @Override
    public int getCmdNo() {
        // TODO Auto-generated method stub
        return CMD_NO;
    }

    public static void print() {
       
    }

    public static void main(String[] args) throws Exception {
        print();

    }

    

}
