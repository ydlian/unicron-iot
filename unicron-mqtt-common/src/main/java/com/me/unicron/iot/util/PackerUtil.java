package com.me.unicron.iot.util;

import org.apache.commons.lang.StringUtils;

import com.me.unicron.iot.exception.MqttParaException;

import net.logstash.logback.encoder.org.apache.commons.lang.math.NumberUtils;

public class PackerUtil {

	//byte数组转成long 
    public static long byteToLong(byte[] b) { 
        long s = 0; 
        long s0 = b[0] & 0xff;// 最低位 
        long s1 = b[1] & 0xff; 
        long s2 = b[2] & 0xff; 
        long s3 = b[3] & 0xff; 
        long s4 = b[4] & 0xff;// 最低位 
        long s5 = b[5] & 0xff; 
        long s6 = b[6] & 0xff; 
        long s7 = b[7] & 0xff; 
 
        // s0不变 
        s1 <<= 8; 
        s2 <<= 16; 
        s3 <<= 24; 
        s4 <<= 8 * 4; 
        s5 <<= 8 * 5; 
        s6 <<= 8 * 6; 
        s7 <<= 8 * 7; 
        s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7; 
        return s; 
    } 
    
    /** 
     * 注释：字节数组到int的转换！ 
     * 
     * @param b 
     * @return 
     */ 
    public static int byteToInt(byte[] b) { 
        int s = 0; 
        int s0 = b[0] & 0xff;// 最低位 
        int s1 = b[1] & 0xff; 
        int s2 = b[2] & 0xff; 
        int s3 = b[3] & 0xff; 
        s3 <<= 24; 
        s2 <<= 16; 
        s1 <<= 8; 
        s = s0 | s1 | s2 | s3; 
        return s; 
    } 
    
    /** 
     * 注释：字节数组到short的转换！ 
     * 
     * @param b 
     * @return 
     */ 
    public static short byteToShort(byte[] b) { 
        short s = 0; 
        short s0 = (short) (b[0] & 0xff);// 最低位 
        short s1 = (short) (b[1] & 0xff); 
        s1 <<= 8; 
        s = (short) (s0 | s1); 
        return s; 
    }
    
    /** 
     * 注释：字节数组到char的转换！ 
     * 
     * @param b 
     * @return 
     */ 
    public static String byteToCharsequence(byte[] b) { 
    		//Character.
    		String s="";
    		for(int i=0;i<b.length;i++){
    			char ch=(char) b[i];
    			s+=ch;
    		}
        return s; 
    }
    
	// long类型转成byte数组
	public static byte[] longToByte(long number) {
		long temp = number;
		byte[] b = new byte[8];
		for (int i = 0; i < b.length; i++) {
			b[i] = new Long(temp & 0xff).byteValue();// 将最低位保存在最低位
			temp = temp >> 8; // 向右移8位
		}
		return b;
	}
	
	
	/**
	 * 注释：short到字节数组的转换！
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] shortToByte(short number) {
		int temp = number;
		byte[] b = new byte[2];
		for (int i = 0; i < b.length; i++) {
			b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
			temp = temp >> 8; // 向右移8位
		}
		return b;
	}

	/**
	 * 注释：int到字节数组的转换！
	 * 
	 * @param number
	 * @return
	 */
	public static byte[] intToByte(int number) {
		int temp = number;
		byte[] b = new byte[4];
		for (int i = 0; i < b.length; i++) {
			b[i] = new Integer(temp & 0xff).byteValue();//将最低位保存在最低位
			temp = temp >> 8; // 向右移8位
		}
		return b;
	}

	public static byte getChecksum(byte[] data) {
		if (data.length <= 1) {
			return 0;
		}
		int sum = 0;
		for (int i = 0; i < data.length - 1; i++) {
			sum += data[i];
		}
		sum = sum % Byte.MAX_VALUE;
		return (byte)sum;
	}
	
	public static void printHex(byte[] byt){
		for (int i = 0; i < byt.length; i++) {
			String temp = Integer.toHexString((byte) byt[i]);
			if (temp.length() > 2) {
				temp = temp.substring(temp.length() - 2, temp.length());
			} else if (temp.length() == 1) {
				temp = "0" + temp;
			} else {

			}
			System.out.print(temp + " ");
		}
		System.out.println("\n");
	}
	
	public static void print(byte[] byt){
		System.out.println("打印byte数组:");
		for (int i = 0; i < byt.length; i++) {
			String temp = ""+byt[i];
			
			System.out.print(temp + " ");
		}
		System.out.println("\n");
	}
	public static byte[]  ip2Byte(String ip){
		
		byte[] b = new byte[4];
		if(StringUtils.isBlank(ip) || ip.indexOf(".")<0){
			return b;
		}
		String[] arr=ip.split("\\.");
		for(int i=0;i<arr.length;i++){
			b[i]=(byte) Short.parseShort(arr[i]);
		}
		return b;
	}
	

	public static byte[] port2Byte(String port) {
		// TODO Auto-generated method stub
		byte[] b = new byte[4];
		if(StringUtils.isBlank(port) || !NumberUtils.isNumber(port)){
			return b;
		}
		return intToByte(Integer.parseInt(port));
		
	}
	public static byte[] dataBody4BitString2Bytes(String data,int para_cnt){
		byte[] byt=new byte[para_cnt*4];
		//todo:填充数据
		String arr[]=data.split(",");
		if(arr.length!=para_cnt){
			throw new MqttParaException("参数长度不符");
		}
		for(int i=0;i<arr.length;i++){
			int val=Integer.parseInt(arr[i]);
			byte[] byte4=PackerUtil.intToByte(val);
			System.arraycopy(byte4, 0, byt, 4*i, 4);
		}
		
		return byt;
	}
	public static void main(String[] args) throws Exception {
		byte byt=(byte) 255;
		System.out.println(byt);
		print(intToByte(17));
		print(intToByte(Integer.MAX_VALUE));
		print(intToByte(Integer.MIN_VALUE));
	}


}
