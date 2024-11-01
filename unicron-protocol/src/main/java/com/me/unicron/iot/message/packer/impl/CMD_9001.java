package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;

import com.me.epower.direct.entity.downward.LoginFail_9001;
import com.me.epower.direct.entity.downward.StartChargeQuery_7;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.math.NumberUtils;

//
/**
 * @author 
 * @func：(CODE=9001)客户端登陆失败时，服务器会下发此命令，下发后间断一段时间断开当前连接
 */
@Slf4j
public class CMD_9001 implements BaseCMD<LoginFail_9001> {

    //默认为查询时长度
    private static final int BODY_LENGTH = 72;
    private static final int CMD_NO = 9001;
    //private static short inc = 0;

    @Override
    public byte[] getPayload(LoginFail_9001 loginFail_9001,MqttNetMsgBase base) {
        int bodyLenth = BODY_LENGTH;
       

        String user_id = loginFail_9001.getUser_id();
        if (StringUtils.isBlank(user_id)) {
        	log.info("user_id not set!");
        }

        bodyLenth = BODY_LENGTH;
        int totalLen = base.getLength(bodyLenth);
        byte[] data = new byte[totalLen];

        data=base.fillPayload2CmdNo(data, totalLen, (short) CMD_NO);
        int bytesOffset = base.getBytesOffset();

        //4字节,错误码
        byte[] errorCode = new byte[4];
        errorCode = EncodeUtil.intToByte(loginFail_9001.getError_code());
        System.arraycopy(errorCode, 0, data, 8+bytesOffset, 4);
       
        //用户识别号(App) 32
        byte[] user_id_assic = new byte[32];
        try {
            if(loginFail_9001.getUser_id()!=null){
                byte[] databyte = loginFail_9001.getUser_id().getBytes(CharsetDef.CHARSET);
                System.arraycopy(databyte, 0, user_id_assic, 0, databyte.length);
            }
        } catch (UnsupportedEncodingException e) {
            log.error("CMD_7 parse error", e);
        }
        System.arraycopy(user_id_assic, 0, data, 38+bytesOffset, 32);

      
        byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
        System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + bodyLenth, 1);
        return data;
    }

    @Override
    public String pack(LoginFail_9001 loginFail_9001,MqttNetMsgBase base) {
        byte[] data = getPayload(loginFail_9001,base);
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

   

    public static void print() {
       
        //125 -33 37 0 1 0 1 0 0 0 0 0 1 1 0 0 0 4 16 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 26 
        //125 -33 41 0 1 0 1 0 0 0 0 0 1 1 0 0 0 5 20 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 35 
        //125 -33 21 0 1 0 1 0 0 0 0 0 0 1 0 0 0 5 0 0 121 
        //编码前：
        
    }

    public static void main(String[] args) throws Exception {
        print();
    }

}
