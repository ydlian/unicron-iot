package com.me.unicron.iot.bootstrap.channel;

import com.me.unicron.iot.bootstrap.bean.SessionMessage;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 会话保留处理
 *
 * @author lianyadong
 * @create 2023-11-23 11:21
 **/
@Service
public class ClientSessionService {

    private static ConcurrentHashMap<String,ConcurrentLinkedQueue<SessionMessage>>  queueSession  = new ConcurrentHashMap<>();  // 连接关闭后 保留此session 数据  deviceId
    
    
    public  void saveSessionMsg(String deviceId, SessionMessage sessionMessage) {
        ConcurrentLinkedQueue<SessionMessage> sessionMessages = queueSession.getOrDefault(deviceId, new ConcurrentLinkedQueue<>());
        boolean flag;
        do{
             flag = sessionMessages.add(sessionMessage);
             try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        while (!flag);
        queueSession.put(deviceId,sessionMessages);
    }

    public  ConcurrentLinkedQueue<SessionMessage> getByteBuf(String deviceId){
        return queueSession.get(deviceId);
    }
}
