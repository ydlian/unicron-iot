package com.me.unicron.iot.auto;

import com.me.unicron.iot.bootstrap.BootstrapServer;
import com.me.unicron.iot.bootstrap.NettyBootstrapServer;
import com.me.unicron.iot.mqtt.MqttHander;
import com.me.unicron.iot.properties.InitBean;

import lombok.extern.slf4j.Slf4j;

/**
 * 初始化服务
 *
 * @author lianyadong
 * @create 2023-11-29 20:12
 **/
@Slf4j
public class InitServer {

    private InitBean serverBean;

    public InitServer(InitBean serverBean) {
        this.serverBean = serverBean;
    }

    BootstrapServer bootstrapServer;

    public void open(){
        if(serverBean!=null){

            bootstrapServer = new NettyBootstrapServer();
            bootstrapServer.setServerBean(serverBean);
            bootstrapServer.start();
        }
    }


    public InitBean getServerBean() {
		return serverBean;
	}


	public void close(){
        if(bootstrapServer!=null){
            bootstrapServer.shutdown();
        }
    }

}
