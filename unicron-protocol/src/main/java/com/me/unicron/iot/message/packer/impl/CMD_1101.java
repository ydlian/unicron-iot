package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.epower.direct.entity.downward.Httpupdate_1101;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.protocol.CharsetDef;
import com.me.unicron.string.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author fzl
 *
 */
@Slf4j
public class CMD_1101 implements BaseCMD<Httpupdate_1101> {

    private static final int BODY_LENGTH = 162;
    private static final int CMD_NO = 1101;

    @Override
    public byte[] getPayload(Httpupdate_1101 httpupdate_1101,MqttNetMsgBase base) {
        int totalLen = base.getLength(BODY_LENGTH);
        byte[] data = new byte[totalLen];
        
        
        
        try {
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
            byte[] reserved = new byte[2];
            System.arraycopy(reserved, 0, data, 8+bytesOffset, 2);

            byte[] urlbyte = new byte[128];
            String url = httpupdate_1101.getUrl();
            if (StringUtils.isBlank(url)) {
                return data;
            }
            byte[] urlOrigByte = url.getBytes(CharsetDef.CHARSET);
            System.arraycopy(urlOrigByte, 0, urlbyte, 0, urlOrigByte.length);
            System.arraycopy(urlbyte, 0, data, 10+bytesOffset, 128);

            byte[] md5byte = new byte[32];
            String md5 = httpupdate_1101.getMd5();
            if (StringUtils.isBlank(md5)) {
                return data;
            }
            byte[] md5OrigByte = md5.getBytes(CharsetDef.CHARSET);
            System.arraycopy(md5OrigByte, 0, md5byte, 0, md5OrigByte.length);
            System.arraycopy(urlbyte, 0, data, 138+bytesOffset, 32);

            byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
            System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + BODY_LENGTH, 1);
            return data;
        } catch (Exception e) {
            log.error("CMD_1101 error", e);
        }
        return data;
    }

    
    @Override
    public int getCmdNo() {
        // TODO Auto-generated method stub
        return CMD_NO;
    }


	@Override
	public String pack(Httpupdate_1101 t,MqttNetMsgBase base) {
		// TODO Auto-generated method stub
		byte[] data = getPayload(t,base);
        try {
            return new String(data, CharsetDef.CHARSET);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
	}

}
