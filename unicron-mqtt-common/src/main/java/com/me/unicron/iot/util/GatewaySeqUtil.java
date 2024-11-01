package com.me.unicron.iot.util;

import java.util.Random;

public class GatewaySeqUtil {

    final static String ddOperatorId = "101437000";
    
    public static String genSeq() {
        StringBuffer sb = new StringBuffer();
        sb.append(ddOperatorId).append(System.currentTimeMillis()).append(new Random().nextInt(90000) + 10000);
        return sb.toString();
    }
}
