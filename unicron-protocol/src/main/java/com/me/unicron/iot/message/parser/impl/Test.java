package com.me.unicron.iot.message.parser.impl;

import java.io.UnsupportedEncodingException;

import com.me.unicron.EncodeUtil;

public class Test {

    public static void main(String[] args) {
        String CHARSET = "ISO-8859-1";//默认字符集
        
//        try {
//            String portId = "4";
//            byte[] portTypes = portId.getBytes(CHARSET);     
//            //1字节
//            //从byte数组到字符串
//            byte portType = portTypes[0];
//            String portTypeStr;
//            portTypeStr = new String( new byte[]{portType},CHARSET);
//            System.out.println("portTypeStr:"+portTypeStr);
//            //从字符串到byte
//            byte[] portTypes2 = portTypeStr.getBytes(CHARSET);            
//            System.out.println("portTypeStr:"+portTypes2);
//        } catch (UnsupportedEncodingException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        byte[] data = new byte[]{0,2,3,6,1};        
        String cardNum = "MA06K1098151981124806519270";
        
        
        //String cardNumAscii = EncodeUtil.stringToAscii(cardNum);
        byte[] cardNumAsciiBytes;
        try {
            cardNumAsciiBytes = cardNum.getBytes(CHARSET);
            byte[] byte32 = new byte[32];
            System.arraycopy(cardNumAsciiBytes, 0, byte32, 0, cardNumAsciiBytes.length);
            System.arraycopy(byte32, 0, data, 13, 32);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        

    }

}
