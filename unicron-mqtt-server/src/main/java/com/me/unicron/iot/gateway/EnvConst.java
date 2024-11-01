package com.me.unicron.iot.gateway;

import com.me.unicron.iot.auto.ServerAutoConfigure;
import com.me.unicron.iot.enums.EnvType;
import com.me.unicron.iot.properties.InitBean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnvConst {
	public static EnvType getEnvType(){
		EnvType type=EnvType.ENV_DEV;
		InitBean bean=ServerAutoConfigure.getInitServer().getServerBean();
		if(bean.getEnvType()!=null && bean.getEnvType().trim()
				.toLowerCase().equals("dev")){
			type=EnvType.ENV_DEV;
		}else{
			type=EnvType.ENV_ONLINE;
		}
		//log.info("当前环境:{}",type);
		return type;
		
	}
	
	public static boolean isLogicServer(){
		boolean result=true;
		InitBean bean=ServerAutoConfigure.getInitServer().getServerBean();
		result=bean.isAliveLogicServer();
		//log.info("当前是否逻辑服务器:{}",result);
		return result;
		
	}
	
	public static final EnvType CUR_ENV_TYPE=getEnvType();
}
