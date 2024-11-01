package com.me.unicron.iot.auto;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.me.unicron.iot.enums.ProtocolEnum;
import com.me.unicron.iot.properties.InitBean;

import lombok.extern.slf4j.Slf4j;

/**
 * 自动化配置初始化服务
 *
 * @author lianyadong
 * @create 2023-11-29 19:52
 **/
@Configuration
@ConditionalOnClass
@EnableConfigurationProperties({InitBean.class})
@Slf4j
public class ServerAutoConfigure {

	public static InitServer initServer=null;
	
    private static  final  int _BLACKLOG =   1024;

    private static final  int  CPU =Runtime.getRuntime().availableProcessors();

    private static final  int  SEDU_DAY =10;

    private static final  int TIMEOUT =120;

    private static final  int BUF_SIZE=10*1024*1024;


    public static InitServer getInitServer() {
		return initServer;
	}

	public static void setInitServer(InitServer initServer) {
		ServerAutoConfigure.initServer = initServer;
	}

	public ServerAutoConfigure(){

    }

    @Bean
    @ConditionalOnMissingBean(name = "sacnScheduled")
    public ScanRunnable initRunable(@Autowired  InitBean serverBean){
        long time =(serverBean==null || serverBean.getPeriod()<5)?10:serverBean.getPeriod();
        log.info("[server]ScanRunnable...");
        ScanRunnable sacnScheduled = new SacnScheduled(time);
        Thread scanRunnable = new Thread(sacnScheduled);
        scanRunnable.setDaemon(true);
        scanRunnable.start();
        return sacnScheduled;
    }


    @Bean(initMethod = "open", destroyMethod = "close")
    @ConditionalOnMissingBean
    public InitServer initServer(InitBean serverBean){
        log.info("[server]initServer");
        if(!ObjectUtils.allNotNull(serverBean.getPort(),serverBean.getServerName())){
            throw  new NullPointerException("not set port");
        }
        if(serverBean.getBacklog()<1){
            serverBean.setBacklog(_BLACKLOG);
        }
        if(serverBean.getBossThread()<1){
            serverBean.setBossThread(CPU);
        }
        if(serverBean.getInitalDelay()<0){
            serverBean.setInitalDelay(SEDU_DAY);
        }
        if(serverBean.getPeriod()<1){
            serverBean.setPeriod(SEDU_DAY);
        }
        if(serverBean.getHeart()<1){
            serverBean.setHeart(TIMEOUT);
        }
        if(serverBean.getRevbuf()<1){
            serverBean.setRevbuf(BUF_SIZE);
        }
        if(serverBean.getWorkThread()<1){
            serverBean.setWorkThread(CPU*2);
        }
        if(serverBean.getProtocolEnum()==null){
            serverBean.setProtocolEnum(ProtocolEnum.MQTT);
        }
        setInitServer(new InitServer(serverBean));
        return getInitServer();
    }


}
