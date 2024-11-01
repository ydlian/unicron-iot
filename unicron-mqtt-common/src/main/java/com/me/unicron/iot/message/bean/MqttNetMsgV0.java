package com.me.unicron.iot.message.bean;

import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;

//ver 1.0.0.0 
public class MqttNetMsgV0 extends MqttNetMsgBase{
    /*
     * 字段 起始域 长度域 版本域 序列号域 命令代码（CODE） 数据域 校验和域 字节数 2 (字节) 2 (字节) 1 (字节) 1 (字节) 2 (字节) N (字节) 1 (字节) 取值范围：0～0x7fff 0～0x7fff 0x01 0～0x7f 0x0000～0x7fff 0～0x7F 7ddf 0001 01 0000 006a 数据部分 00
     */

    /*
     * 签到报文： aa e4 83 00 10 00 6a 00 00 00 00 00 32 30 31 37 30 38 32 31 31 31 30 33 30 30 30 31 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 01 00 00 23 01 02 00 00 00 01 0f 00 00 02 1e 03
     * 00 00 00 00 20 18 02 23 15 48 05 ff 20 18 02 23 15 48 05 ff 20 18 02 23 15 48 05 ff 20 18 02 23 15 48 05 ff 38 39 38 36 30 36 31 37 30 33 30 30 33 33 38 39 34 32 38 31 00 00 00 00 00 00 00 00
     * 00 00 00 00 ee
     * 
     * 签到回应报文：
     * 
     */
    public static final byte HEADER1 = (byte) 0x7d;
    public static final byte HEADER2 = (byte) 0xdf;
    public static final int HEADER_ZH = 0xAABB;
    
    
    //起始域
    private static final byte[] herder = new byte[] { HEADER1, HEADER2 };
    //长度域
    private static byte length[] = new byte[2];
    //版本号
    private static final byte[] version = new byte[] { (byte) 0x01 };
    private static byte[] index = new byte[1];
    private static byte[] cmdCode = new byte[2];
    
    private static byte[] data;//N bytes
    private static byte[] checkSum = new byte[1];

    private static byte[] defaultControlParam = new byte[] { (byte) 0x55, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

    @Override
    public byte[] fillPayload2CmdNo(byte[] data,int totalLen,short cmd_no){
		
    	System.arraycopy(getHerder(), 0, data, 0, 2);
        //长度域
        byte[] length = EncodeUtil.shortToByte((short) totalLen);
        System.arraycopy(length, 0, data, 2, 2);
        //版本域
        byte[] ver = getVersion();
        System.arraycopy(ver, 0, data, 4, 1);
        //序列号域
        byte[] index = getIndex();
        System.arraycopy(index, 0, data, 5, 1);
        //命令域
        System.arraycopy(EncodeUtil.shortToByte((short) cmd_no), 0, data, 6, 2);
        
		return data;
	}
    @Override
    public int getLength(int dataLen) {
        return getCodeLeftLength() + dataLen + 1;
    }
    
    @Override
	public int getCodeLeftLength() {
		// TODO Auto-generated method stub
		return 2 + 2 + 1 + 1 + 2;
	}

    @Override
	public int getBytesOffset() {
		// TODO Auto-generated method stub
		return 0;
	}
    
    public static byte[] getLength() {
        return length;
    }

    public static void setLength(byte[] length) {
    	MqttNetMsgV0.length = length;
    }

    public static byte[] getIndex() {
        return index;
    }

    public static void setIndex(byte[] index) {
    	MqttNetMsgV0.index = index;
    }

    public static byte[] getCmdCode() {
        return cmdCode;
    }

    public static void setCmdCode(byte[] cmdCode) {
    	MqttNetMsgV0.cmdCode = cmdCode;
    }

    public static byte[] getCheckSum() {
        return checkSum;
    }

    public static void setCheckSum(byte[] checkSum) {
    	MqttNetMsgV0.checkSum = checkSum;
    }

    public static byte[] getHerder() {
        return herder;
    }
    
    public static byte[] getDefaultControlParam() {
        return defaultControlParam;
    }

    public static byte[] getVersion() {
        return version;
    }

    public String toString() {
        return "";
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String toHexString(int num, int len) {
        String s = Integer.toHexString(num);
        if (s.length() < len) {
            int q = len - s.length();
            for (int i = 0; i < q; i++) {
                s += "0";
            }
        }
        return s;

    }

    public String byteToHexString(byte[] byteData) {
        String s = "";
        for (int i = 0; i < byteData.length; i++) {
            s = s + "" + byteData[i];
        }

        return s;

    }

    public static String stringToAscii(String value) {
        StringBuffer sbu = new StringBuffer();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i != chars.length - 1) {
                sbu.append((int) chars[i]).append("");
            } else {
                sbu.append((int) chars[i]);
            }
        }
        return sbu.toString();
    }

    public static String asciiToString(String value) {
        StringBuffer sbu = new StringBuffer();
        String[] chars = value.split(",");
        for (int i = 0; i < chars.length; i++) {
            sbu.append((char) Integer.parseInt(chars[i]));
        }
        return sbu.toString();
    }

    @Override
	public byte[] getServerProtocolVersion() {
		// TODO Auto-generated method stub
		//协议版本
		return version;
	}

    
    

}
