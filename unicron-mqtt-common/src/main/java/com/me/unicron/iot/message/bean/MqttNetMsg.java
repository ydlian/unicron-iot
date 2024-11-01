package com.me.unicron.iot.message.bean;

import com.alibaba.fastjson.JSONObject;
import com.me.unicron.iot.util.Count;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;

public class MqttNetMsg extends MqttNetMsgBase{
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
	//服务器发出的指令携带的报文头:7dd0，智充发出的使用：7ddf，中恒：7dd8
	//被动应答的一方：按照原样返回收到的报文序号字段，即使用接收到的数值copy返回
	//主动发起指令的一方：发送本地生成的报文序号
    public static final byte HEADER1 = (byte) 0x7d;
    public static final byte HEADER2 = (byte) 0xd0;//桩20230710之前的为0x7ddf，升级后，更改为7dd0
    //起始域
    private byte[] header = new byte[] { HEADER1, HEADER2 };
    public byte[] getHeader() {
    	/*
    	if(EncodeUtil.byteToInt(this.getHeadCopyVersion())<=EncodeUtil.byteToInt(version_10)){
    		this.setHeader(new byte[]{(byte) 0x7d,(byte) 0xdf});
    	}else{
    		this.setHeader(new byte[]{(byte) 0x7d,(byte) 0xd0});
    	}*/
		return header;
	}

	public void setHeader(byte[] header) {
		this.header = header;
	}

	//长度域
    private byte length[] = new byte[2];
    //版本号 1->4,最新版本号,1.1.13.0
    private byte[] version = new byte[]{(byte)0x01,(byte)0x01,(byte)0x0d,(byte)0x0};
    //2023-07-10上线前的版本,1.1.11.0
    private byte[] version_710 = new byte[]{(byte)0x01,(byte)0x1,(byte)0x0b,(byte)0x0};
    
    public void setVersion(byte[] version) {
		this.version = version;
	}

	//序列号域 1->4
    private byte[] index = new byte[4];
    
    private byte[] cmdCode = new byte[2];
    private byte[] data;//N bytes
    private byte[] checkSum = new byte[1];

    private static byte[] defaultControlParam = new byte[] { (byte) 0x55, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

    @Override
    public byte[] fillPayload2CmdNo(byte[] data,int totalLen,short cmd_no){
		//如果是应答
    	if(isResponse){
    		//System.arraycopy(this.getHeadCopyStart(), 0, data, 0, 2);
    		//使用服务器标示头
    		System.arraycopy(getHeader(), 0, data, 0, 2);
    		//长度域
            byte[] length = EncodeUtil.shortToByte((short) totalLen);
            System.arraycopy(length, 0, data, 2, 2);
            //版本域
            byte[] ver = this.getHeadCopyVersion();
            this.setVersion(ver);
            System.arraycopy(ver, 0, data, 4, 4);
            //序列号域
            //使用原值copy
            byte[] index = this.getHeadCopyIndex();
            setIndex(index);
            super.setIndexVal(EncodeUtil.byteToUnsignedInt(index));
            System.arraycopy(index, 0, data, 8, 4);
    	}else{
    		//主动发起的数据填充
    		//使用服务器标示头
    		System.arraycopy(getHeader(), 0, data, 0, 2);
    		//长度域
            byte[] length = EncodeUtil.shortToByte((short) totalLen);
            System.arraycopy(length, 0, data, 2, 2);
            //版本域
            //本地协议版本
            byte[] ver = getVersion();
            this.setVersion(ver);
            System.arraycopy(ver, 0, data, 4, 4);
            //序列号域
            byte[] index = EncodeUtil.intToByte(Count.getIntCount());
            setIndex(index);
            super.setIndexVal(EncodeUtil.byteToUnsignedInt(index));
            System.arraycopy(index, 0, data, 8, 4);
    	}
		
        //命令域
        System.arraycopy(EncodeUtil.shortToByte(cmd_no), 0, data, 12, 2);
        
		return data;
	}

    @Override
    public int getLength(int dataLen) {
        return getCodeLeftLength() + dataLen + 1;
    }
    
    @Override
    public int getCodeLeftLength() {
        return 2 + 2 + 4 + 4 + 2;
    }
    
    @Override
	public int getBytesOffset() {
		// TODO Auto-generated method stub
		return 6;
	}
    
    public byte[] getLength() {
		return length;
	}

	public void setLength(byte[] length) {
		this.length = length;
	}

	public byte[] getIndex() {
		return index;
	}

	public void setIndex(byte[] index) {
		this.index = index;
	}

	public byte[] getCmdCode() {
		return cmdCode;
	}

	public void setCmdCode(byte[] cmdCode) {
		this.cmdCode = cmdCode;
	}

	public byte[] getCheckSum() {
		return checkSum;
	}

	public void setCheckSum(byte[] checkSum) {
		this.checkSum = checkSum;
	}

	public byte[] getVersion() {
		return version;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	
    
    public static byte[] getDefaultControlParam() {
        return defaultControlParam;
    }


    public String toString() {
        return JSONObject.toJSONString(this);
    }

    public byte[] getData() {
        return data;
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
		//最新的协议版本
		//byte[] ver=new byte[] { 0x01, 0x00,0x0c,0x00 };
		return version;
	}

	
    
    

}
