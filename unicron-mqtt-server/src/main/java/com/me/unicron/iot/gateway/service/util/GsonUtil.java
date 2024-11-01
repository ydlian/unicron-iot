package com.me.unicron.iot.gateway.service.util;

import com.google.gson.Gson;

public class GsonUtil {
    
    // 静态内部类  
    private static class NestClass {  
        private static Gson instance;  
        static {  
            instance = new Gson();  
        }  
    }  
    public static Gson getInstance() {  
        return NestClass.instance;  
    } 
}
