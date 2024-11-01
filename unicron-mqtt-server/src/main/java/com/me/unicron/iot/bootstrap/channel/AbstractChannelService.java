package com.me.unicron.iot.bootstrap.channel;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.me.unicron.iot.bootstrap.bean.MqttChannel;
import com.me.unicron.iot.bootstrap.bean.RetainMessage;
import com.me.unicron.iot.bootstrap.BaseApi;
import com.me.unicron.iot.bootstrap.ChannelService;
import com.me.unicron.iot.bootstrap.channel.cache.CacheMap;
import com.me.unicron.iot.enums.SessionStatus;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 抽象类
 *
 * @author lianyadong
 * @create 2023-12-12 20:01
 **/
@Slf4j
public abstract class AbstractChannelService extends PublishApiSevice implements ChannelService ,BaseApi {


    protected AttributeKey<Boolean> _login = AttributeKey.valueOf("login");

    protected   AttributeKey<String> _deviceId = AttributeKey.valueOf("deviceId");

    protected  static char SPLITOR = '/';

    protected ExecutorService executorService =Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);


    protected static CacheMap<String,MqttChannel> cacheMap= new CacheMap<>();


    protected static ConcurrentHashMap<String ,MqttChannel> mqttChannels = new ConcurrentHashMap<>(); // deviceId - mqChannel 登录


    protected  static  ConcurrentHashMap<String,ConcurrentLinkedQueue<RetainMessage>> retain = new ConcurrentHashMap<>(); // topic - 保留消息



    protected  static  Cache<String, Collection<MqttChannel>> mqttChannelCache = CacheBuilder.newBuilder().maximumSize(100).build();

    public AbstractChannelService(ScanRunnable scanRunnable) {
        super(scanRunnable);
    }


    protected  Collection<MqttChannel> getChannels(String topic,TopicFilter topicFilter){
            try {
                return  mqttChannelCache.get(topic, () -> topicFilter.filter(topic));
            } catch (Exception e) {
                log.info(String.format("guava cache key topic [%s] channel   value== [%s] ",topic,e.toString()));
            }
            return null;
    }


    @FunctionalInterface
    interface TopicFilter{
        Collection<MqttChannel> filter(String topic);
    }

    public boolean deleteChannel(String topic,MqttChannel mqttChannel){
        mqttChannelCache.invalidate(topic);
        return  cacheMap.delete(getTopic(topic),mqttChannel);
    }
	
    protected boolean addChannel(String topic,MqttChannel mqttChannel)
    {
        mqttChannelCache.invalidate(topic);
        
        return  cacheMap.putData(getTopic(topic),mqttChannel);
    }
    
    //sofalala
    public boolean addMqttChannel(String deviceId,MqttChannel mqttChannel)
    {
		return true;
    }
    //
    public void deleteMqttChannel(String deviceId,MqttChannel mqttChannel)
    {
    	
    	mqttChannelCache.invalidate(deviceId);
        cacheMap.delete(getTopic(deviceId),mqttChannel);
        mqttChannels.remove(mqttChannel);
    }

    /**
     * 获取channel
     */
    public MqttChannel getMqttChannel(String deviceId){
    	if(deviceId==null){
    		
    	}
        return mqttChannels.get(deviceId);

    }

    /**
     * 获取channelId
     */
    public String  getDeviceId(Channel channel){
        return channel.attr(_deviceId).get();
    }



    protected String[] getTopic(String topic)  {
        return StringUtils.split(topic,SPLITOR);
    }



}
