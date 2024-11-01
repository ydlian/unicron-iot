package com.me.unicron.iot.bootstrap;

import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.me.epower.direct.entity.ServerInfo;
import com.me.epower.direct.entity.constant.RedisConstant;
import com.me.epower.direct.repositories.ServerInfoRepository;
import com.me.unicron.common.server.ServerList;
import com.me.unicron.date.DateUtils;
import com.me.unicron.iot.bean.ClientConnectionInfo;
import com.me.unicron.iot.bean.ClientOfflineInfo;
import com.me.unicron.iot.bootstrap.channel.SpringUtil;
import com.me.unicron.iot.ip.IpUtils;
import com.me.unicron.iot.mq.service.DynamicMQCategoryService;
import com.me.unicron.iot.mqtt.ClientConnectionService;
import com.me.unicron.iot.properties.InitBean;
import com.me.unicron.security.MD5;
import com.me.unicron.station.service.IPreReleaseService;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;
import lombok.Setter;

/**
 * netty 服务启动类
 *
 * @author lianyadong
 * @create 2023-11-18 14:03
 **/
@Setter
@Getter
@lombok.extern.slf4j.Slf4j
@Component
public class NettyBootstrapServer extends AbstractBootstrapServer {

    private InitBean serverBean;
    private final int MAX_CONNECTION_LOAD = 50000;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    DynamicMQCategoryService dynamicMQCategoryService;
    @Autowired
    IPreReleaseService iPreReleaseService;
    @Autowired
    ClientConnectionService clientConnectionService;
    public InitBean getServerBean() {
        return serverBean;
    }

    public void setServerBean(InitBean serverBean) {
        this.serverBean = serverBean;
    }

    ServerInfoRepository serverInfoRepository;

    private NioEventLoopGroup bossGroup;

    private NioEventLoopGroup workGroup;

    ServerBootstrap bootstrap = null;// 启动辅助类

    /**
     * 服务开启
     */
    public void start() {
        //jdk.internal.misc.Unsafe.allocateUninitializedArray(int): unavailable
        initEventPool();
        bootstrap.group(bossGroup, workGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_REUSEADDR, serverBean.isReuseaddr())
                .option(ChannelOption.SO_BACKLOG, serverBean.getBacklog()).option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT).option(ChannelOption.SO_RCVBUF, serverBean.getRevbuf())
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    protected void initChannel(SocketChannel ch) throws Exception {
                        initHandler(ch.pipeline(), serverBean);
                    }
                }).childOption(ChannelOption.TCP_NODELAY, serverBean.isTcpNodelay()).childOption(ChannelOption.SO_KEEPALIVE, serverBean.isKeepalive())
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.bind(serverBean.getPort()).addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
            	log.info("channelFuture is Success:{}",IpUtils.getHost());
                if (serverBean.isAliveLogicServer()) {
                    ServerInfo entity = new ServerInfo();
                    entity.setPort(serverBean.getPort());
                    entity.setIp(IpUtils.getHost());
                    entity.setUniId(MD5.md5Str(entity.getIp() + entity.getPort(), "utf-8"));
                    //log.info("逻辑服务器地址写入："+entity.getIp()+","+entity.getPort());
                    //写入数据库
                    if (serverInfoRepository == null) {
                        ServerInfoRepository serverInfoRepositoryTemp = SpringUtil.getBean(ServerInfoRepository.class);
                        serverInfoRepository = serverInfoRepositoryTemp;
                    }
                    //改为集群化管理，无需写入单个IP地址，部署时通过DDL操作写入服务器集群信息
                    //serverInfoRepository.save(entity);
                    
                    setMQConsumerListenCate(serverBean);
                    if (stringRedisTemplate == null) {
            			StringRedisTemplate stringRedisTemplateTemp = SpringUtil.getBean(StringRedisTemplate.class);
            			stringRedisTemplate = stringRedisTemplateTemp;
                    }
                    if(stringRedisTemplate!=null){
                    	if(ServerList.logicServer3.equals(IpUtils.getHost())){
                    		//上完最后一个节点更新为完毕
                    		String servicename="LOGIC";
         					stringRedisTemplate.opsForValue().set(RedisConstant.UNICRON_CHARGE_CLOUD_SYSTEM_UPGRADING+servicename,"1");
                    	}
                    	
                    }
                    //下线本机上的所有连接
                    //offlineThisNodeHistoryConnection(serverBean);
                   
                }else{
                	//中心服务器
                	String preReleaseList=serverBean.getPreTestClientIDs();
                	if(!preReleaseList.endsWith(",")){
                		preReleaseList+=",";
                	}
                	if (iPreReleaseService == null) {
                		IPreReleaseService iPreReleaseServiceTemp = SpringUtil.getBean(IPreReleaseService.class);
                		iPreReleaseService = iPreReleaseServiceTemp;
                	}
                	String hisList=iPreReleaseService.getPreReleaseWhiteList();
                	if(hisList==null ||hisList.trim().equals("") ||hisList.equals(",") ){
                		iPreReleaseService.updatePreReleaseWhiteList(preReleaseList);
                	}
                	
                	
                }
                //服务器启动了
                log.info("服务端启动成功【isAliveLogicServer:" + serverBean.isAliveLogicServer() + "," + IpUtils.getHost() + ":" + serverBean.getPort() + "】");
                
            } else {
                log.error("服务端启动失败",channelFuture.cause());
                log.info("服务端启动失败【" + IpUtils.getHost() + ":" + serverBean.getPort() + "】");

            }
        });
    }

    private void offlineThisNodeHistoryConnection(InitBean serverBean){
    	if (serverBean.isAliveLogicServer()) {
    		if(clientConnectionService == null){
    			clientConnectionService= SpringUtil.getBean(ClientConnectionService.class);
    			
    		}
    		if(clientConnectionService != null && stringRedisTemplate!=null){
    			
    			String onlineKey = RedisConstant.UNICRON_GLOBLE_CONNECT_LIST;
    			HashOperations<String, String, String> vo = stringRedisTemplate.opsForHash();
    			Map<String, String> onlineMap = vo.entries(onlineKey);
    			for (String deviceid : onlineMap.keySet()){
    				
    				ClientConnectionInfo local=clientConnectionService.getThisNodeConnectInfo(deviceid);
    				log.info("[LOG]Clear history connection,deviceid={},{}",deviceid,JSONObject.toJSONString(local));
    				if(ServerList.logicServer1.equals(IpUtils.getHost())){
    				}
    				
    				if(local!=null){
    					try{
    						local.getChannel().close();
    					}catch(Exception e){
    						e.printStackTrace();
    					}finally{
    						vo.delete(onlineKey, deviceid);
    					}
    				}
    				
    				
    			}
    		}
    	}else{
    		log.info("++++++stringRedisTemplate null++++++++");
    	}
    }

    private void setMQConsumerListenCate(InitBean serverBean){
    	if (serverBean.isAliveLogicServer()) {
    		if (dynamicMQCategoryService == null) {
    			DynamicMQCategoryService dynamicMQCategoryServiceTemp = SpringUtil.getBean(DynamicMQCategoryService.class);
                dynamicMQCategoryService = dynamicMQCategoryServiceTemp;
            }
    		
    		if (stringRedisTemplate == null) {
    			StringRedisTemplate stringRedisTemplateTemp = SpringUtil.getBean(StringRedisTemplate.class);
    			stringRedisTemplate = stringRedisTemplateTemp;
            }
    		
    		String localListenTopic=dynamicMQCategoryService.initLocalListenTopic();
    		System.setProperty(DynamicMQCategoryService.DYN_STATION_LOCAL_TOPIC, localListenTopic);

    		if(stringRedisTemplate != null){
    			stringRedisTemplate.opsForValue().set(IpUtils.getHost(),localListenTopic);
    			updateOfflineRedis();
    		}
    		log.info("###设置监听###logic server:DYN_STATION_LOCAL_TOPIC={},localListenTopic={}",DynamicMQCategoryService.DYN_STATION_LOCAL_TOPIC,localListenTopic);
    	}else{
    		
    	}
    	
    	
    }
    
	private void updateOfflineRedis() {
		String offlineKey = RedisConstant.UNICRON_GLOBLE_OFFLINE_LIST;
		HashOperations<String, String, String> vo = stringRedisTemplate.opsForHash();
		Map<String, String> offlineMap = vo.entries(offlineKey);
		for (String key : offlineMap.keySet()) {
			String data = offlineMap.get(key);
			if (StringUtils.isBlank(data)) {
				continue;
			}

			ClientOfflineInfo clientOfflineInfo = JSON.parseObject(data, ClientOfflineInfo.class);
			if (clientOfflineInfo == null) {
				continue;
			}
			clientOfflineInfo.setSendStatus("已发送");
			clientOfflineInfo.setOfflineLongSendStatus("已发送");
		//	clientOfflineInfo.setOfflineTime(DateUtils.getCurDateTime());
			clientOfflineInfo.setLastLiveTime(DateUtils.getCurDateTime());

			String offlineInfo = JSON.toJSONString(clientOfflineInfo, SerializerFeature.IgnoreErrorGetter);
			vo.put(offlineKey, key, offlineInfo);
			log.info("update Redis {} ,info:{}", offlineKey, offlineInfo);
		}
	}
    /**
     * 初始化EnentPool 参数
     */
    private void initEventPool() {
        bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(serverBean.getBossThread(), new ThreadFactory() {

            private AtomicInteger index = new AtomicInteger(0);

            public Thread newThread(Runnable r) {
                return new Thread(r, "BOSS_" + index.incrementAndGet());
            }
        });
        workGroup = new NioEventLoopGroup(serverBean.getWorkThread(), new ThreadFactory() {

            private AtomicInteger index = new AtomicInteger(0);

            public Thread newThread(Runnable r) {
                return new Thread(r, "WORK_" + index.incrementAndGet());
            }
        });

    }

    /**
     * 关闭资源
     */
    public void shutdown() {
        if (workGroup != null && bossGroup != null) {
            try {
                bossGroup.shutdownGracefully().sync();// 优雅关闭
                workGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                log.info("服务端关闭资源失败【" + IpUtils.getHost() + ":" + serverBean.getPort() + "】");
            }
        }
    }

}
