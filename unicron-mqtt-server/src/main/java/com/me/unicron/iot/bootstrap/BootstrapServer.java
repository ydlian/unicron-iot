package com.me.unicron.iot.bootstrap;

import com.me.unicron.iot.properties.InitBean;
import io.netty.channel.Channel;

/**
 * 启动类接口
 *
 * @author lianyadong
 * @create 2023-11-18 14:05
 **/
public interface BootstrapServer {

    void shutdown();

    void setServerBean(InitBean serverBean);

    void start();


}
