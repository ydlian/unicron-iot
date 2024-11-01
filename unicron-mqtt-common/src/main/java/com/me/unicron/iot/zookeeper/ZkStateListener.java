package com.me.unicron.iot.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;

/**
 * zookeeper 监听
 *
 * @author lianyadong
 * @create 2023-11-16 9:44
 **/
public interface ZkStateListener {

    default void connectedEvent(CuratorFramework curator, ConnectionState state) {
    }

    default void ReconnectedEvent(CuratorFramework curator, ConnectionState state) {
    }

    default void lostEvent(CuratorFramework curator, ConnectionState state) {
    }

}
