package com.me.unicron.iot.bootstrap.handler;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.me.epower.direct.entity.downward.LoginFail_9001;
import com.me.epower.direct.enums.ConnectorStatusEnum;
import com.me.unicron.EncodeUtil;
import com.me.unicron.common.server.ServerList;
import com.me.unicron.iot.bean.ClientConnectionInfo;
import com.me.unicron.iot.bootstrap.ChannelService;
import com.me.unicron.iot.bootstrap.bean.MqttChannel;
import com.me.unicron.iot.bootstrap.channel.LogicServerHandleService;
import com.me.unicron.iot.bootstrap.channel.StationManagementService;
import com.me.unicron.iot.bootstrap.coder.server.ServerDecoder;
import com.me.unicron.iot.ip.IpUtils;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.packer.impl.CMD_9001;
import com.me.unicron.iot.message.parser.MsgParser;
import com.me.unicron.iot.mqtt.ClientConnectionService;
import com.me.unicron.iot.mqtt.MqttHander;
import com.me.unicron.iot.mqtt.MqttHandlerInterface;
import com.me.unicron.iot.mqtt.ServerMqttHandlerService;
import com.me.unicron.protocol.CharsetDef;
import com.me.unicron.station.service.IStationService;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认 mqtthandler处理
 *
 * @author lianyadong
 * @create 2023-11-20 13:58
 **/

@ChannelHandler.Sharable
@Slf4j
@Component
public class DefaultMqttHandler extends MqttHander {

    private final MqttHandlerInterface mqttHandlerApi;
    @Autowired
    private ServerDecoder serverDecoder;
    @Autowired
    private StationManagementService stationManagementService;
    @Autowired
    private ClientConnectionService clientConnectionService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private LogicServerHandleService logicServerHandleService;
    @Autowired
    private IStationService iStationService;
    
    @Autowired
	private StringRedisTemplate stringRedisTemplate;

    private static long msgCnt=0;
    private ConcurrentHashMap<String, String> _cache_datas = new ConcurrentHashMap<String, String>();

    public DefaultMqttHandler(MqttHandlerInterface mqttHandlerApi, MqttHandlerInterface mqttHandlerApi1) {
        super(mqttHandlerApi);
        this.mqttHandlerApi = mqttHandlerApi1;
    }

    private void serverRejectConnection(ServerMqttHandlerService serverMqttHandlerService,Channel channel){
    	
        serverMqttHandlerService.disconnect(channel);
        channel.close();
    }
    @Override
    public void doMessage(ChannelHandlerContext channelHandlerContext, MqttMessage mqttMessage) {
        Channel channel = channelHandlerContext.channel();
        msgCnt++;
        String stationEquipId = channelService.getDeviceId(channel);
        
        ServerMqttHandlerService serverMqttHandlerService = null;

        MqttFixedHeader mqttFixedHeader = mqttMessage.fixedHeader();

        try{
        	int msgType=mqttFixedHeader.messageType().value();
        	int msgQos=mqttFixedHeader.qosLevel().value();
        	int messageId = -1;
        	try{
	        	MqttMessageIdVariableHeader messageIdVariableHeader = (MqttMessageIdVariableHeader) mqttMessage
	    				.variableHeader();
	        	if(messageIdVariableHeader!=null){
	        		messageId = messageIdVariableHeader.messageId();
	        	}
        	}catch(Exception e){
        		messageId = -1;
        		//e.printStackTrace();
        	}
            log.info("[msg_rec_log],收到消息，类型={},msgType={},msgQos={},messageId={},消息体={}",
            		mqttFixedHeader.messageType(),msgType,msgQos,messageId,mqttMessage);
          
        	if(msgType<=0
        			|| msgQos<0 
        			|| msgQos>MqttQoS.EXACTLY_ONCE.value()){
            	return;
        	}
            
            
            if (mqttHandlerApi instanceof ServerMqttHandlerService) {
                serverMqttHandlerService = (ServerMqttHandlerService) mqttHandlerApi;
            } else {
                log.error("ServerMqttHandlerService not match!");
                return;
            }
        }catch(Exception e){
        	e.printStackTrace();
        	log.info("异常消息类型:{},mqttFixedHeader={}",e.getCause(),mqttFixedHeader);
        	return;
        }
        String thisServer=IpUtils.getHost();
        //
        try {
            if (mqttFixedHeader.messageType().equals(MqttMessageType.CONNECT)) {
            	
            	MqttConnectMessage connMsg = (MqttConnectMessage) mqttMessage;
                String deviceId = connMsg.payload().clientIdentifier();
            	//ClientConnectionInfo client=clientConnectionService.getConnectInfo(deviceId);

        		if(clientConnectionService.isClientThisNodeOnline(deviceId)){
            		if(ServerList.isLogicServer(thisServer)){
            			log.info("[login_check_fail],thisServer={},Repeated Connection,This Device is already online,please close it before reconnect!deviceId={}",thisServer,deviceId);
            			return;
            		}
            		
            	}
        		
                
                log.info("[cliet]开始建立连接：Starting connect....{},{},{}" , thisServer,channel.id() , deviceId);

                try {
                    onClientConnect(channelHandlerContext, connMsg);
                } catch (Exception e) {
                    log.error("connect error",e);
                }
                if (!serverMqttHandlerService.login(channel,(MqttConnectMessage) mqttMessage)) {
                    log.info("[#####[login_check_fail]######]客户端登陆检查失败,当前连接关闭，{},{}", JSONObject.toJSON(channel), deviceId);

                    MqttChannel conchannel = channelService.getMqttChannel(deviceId);
                   
                    CMD_9001 cmd_9001 = new CMD_9001();
                    LoginFail_9001 loginFail_9001=new LoginFail_9001();
                    loginFail_9001.setError_code(1001);
                    loginFail_9001.setUser_id(deviceId);
            		byte[] byte7 = cmd_9001.getPayload(loginFail_9001,new MqttNetMsg());
            		try {
            			String msg = new String(byte7, CharsetDef.CHARSET);
            			byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
            			channelService.push(deviceId, MqttQoS.AT_MOST_ONCE, sendbytes);
            		}catch(Exception e){
            			e.printStackTrace();
            		}
            		try{
            			Thread.sleep(3000);
            		}catch(Exception e){
            			e.printStackTrace();
            		}
            	
                    clientConnectionService.distroyConnection(deviceId);
                    if (conchannel != null) {
                        channelService.deleteMqttChannel(conchannel);
                    }
                    //拒绝连接
                    serverRejectConnection(serverMqttHandlerService,channel);
                }else{
                	
                	log.info("[login_check_success],{}",deviceId);
                	logicServerHandleService.updateChannel(channelService, deviceId);
                }
                log.info("[doMessage]connect 消息处理完毕,{}",deviceId);
                return;
            }//处理连接建立的消息
            
            
            String theDeviceId = channelService.getDeviceId(channel);
            if(theDeviceId==null){
            	log.info("[doMessage]theDeviceId==null,继续处理");
            	//return;
            }
            
            if(mqttFixedHeader.messageType().value()==MqttMessageType.PUBLISH.value()){
            	try{
                    byte[] dataByte = MsgParser.parseBytes(mqttMessage);
                    if(dataByte!=null && dataByte.length>0){
                    	String logstr=EncodeUtil.print(dataByte);
                        log.info("_com_unicron_logic_server||traceid={}||dataByte={}",theDeviceId,logstr);
                        boolean flag = serverDecoder.checkClientkMsg(dataByte);
                        if (!flag) {
                            //不合法的数据不处理
                            log.info("Invalid data.");
                        } else {
                        	log.info("msg check pass!");
                        	//携带报文头字节,全部的payload
                            logicServerHandleService.procPublishResponse(channelService, dataByte, channelHandlerContext, stationEquipId, serverMqttHandlerService);
                        }
                    }
                    
                  }catch(Exception e){
                	e.printStackTrace();
                	log.info("PUBLISH消息处理发现异常，{}",e.getCause());
                }finally{
                	serverMqttHandlerService.publish(channel, (MqttPublishMessage) mqttMessage);
                }
            }
        	
            
            MqttChannel theMqttChannel = channelService.getMqttChannel(theDeviceId);
            if (theDeviceId == null || theMqttChannel==null) {
                //serverRejectConnection(serverMqttHandlerService,channel);
            	log.info("[doMessage]theDeviceId == null || theMqttChannel==null,继续");
                //return;
            }
            
            if(!channelService.getMqttChannel(channelService.getDeviceId(channel)).isLogin()){
            	
            	log.info("[doMessage]NOT Login");
            	//return;
            }
            
            /*if (channelService.getMqttChannel(channelService.getDeviceId(channel)) != null 
            		&& channelService.getMqttChannel(channelService.getDeviceId(channel)).isLogin()) {*/
            	
                switch (mqttFixedHeader.messageType()) {
                case PUBLISH:
                	break;
                case SUBSCRIBE:
                    log.info("[++Server处理++SUBSCRIBE]:stationEquipId={}",stationEquipId); 
                    serverMqttHandlerService.subscribe(channel, (MqttSubscribeMessage) mqttMessage);
                    break;
                case PINGREQ:
                    
                    log.info("_com_unicron_logic_server||traceid={}||客户端心跳消息PINGREQ={}",theDeviceId,JSONObject.toJSONString(channel));                    
                    serverMqttHandlerService.pong(channel);
                    //ClientConnectionInfo newClient=new ClientConnectionInfo();
            		clientConnectionService.updateConnection(channel, stationEquipId);
                    
                    break;
                
                case DISCONNECT:
                    log.debug("客户端请求断开连接。");
                    MqttChannel conchannel = channelService.getMqttChannel(theDeviceId);
                    if (conchannel != null) {
                        channelService.deleteMqttChannel(conchannel);
                    }
                    stationManagementService.deleteStation(stationEquipId);
                    serverMqttHandlerService.disconnect(channel);
                    break;
                case UNSUBSCRIBE:
                    //log.debug("UNSUBSCRIBE");
                    serverMqttHandlerService.unsubscribe(channel, (MqttUnsubscribeMessage) mqttMessage);
                    break;
                case PUBACK:
                    //log.debug("PUBACK");
                    mqttHandlerApi.puback(channel, mqttMessage);
                    break;
                case PUBREC:
                    //log.debug("PUBREC");
                    mqttHandlerApi.pubrec(channel, mqttMessage);
                    break;
                case PUBREL:
                    mqttHandlerApi.pubrel(channel, mqttMessage);
                    break;
                case PUBCOMP:
                    mqttHandlerApi.pubcomp(channel, mqttMessage);
                    break;
                default:
                    break;
                }
            /*} else {
                //log.error("Something error!ID={} ", stationEquipId);
            }*/

        } catch (Exception e) {
            e.printStackTrace();
            log.info("[doMessage]捕获异常:{}",e.getCause());
        }

    }

    //建立连接时
    private void onClientConnect(ChannelHandlerContext ctx, MqttConnectMessage connMsg) {
        String theDeviceId = connMsg.payload().clientIdentifier();
        String remoteAddr = ctx.channel().remoteAddress().toString();
        log.info("【onClientConnect】{},{},正在建立链接", remoteAddr, theDeviceId);
    }



    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String theDeviceId = channelService.getDeviceId(ctx.channel());
        clientConnectionService.distroyConnection(theDeviceId);
        log.info("[DefaultMqttHandler:channelInactive]连接关闭！theDeviceId={},addr={}",theDeviceId,ctx.channel().remoteAddress().toString());
        //clientConnectionService.printConnectionInfo();
        if (theDeviceId != null) {
        	//stringRedisTemplate.opsForValue().set(RedisConstant.UNICRON_IS_DEVICE_ONLINE+theDeviceId, "0");
            log.info("【关闭连接：DefaultMqttHandler->channelInactive】ID={}", theDeviceId);
            //通道断开，设备离网
            iStationService.updateEquipmentStatus(theDeviceId, ConnectorStatusEnum.OFFLINE.getCode());
            //iStationService.updateStationConnectStatus(theDeviceId, "0");
            ClientConnectionInfo client=clientConnectionService.getThisNodeConnectInfo(theDeviceId);
			if(client!=null && !client.isRepeatConnection()){
				iStationService.updateStationConnectStatus(theDeviceId, "0",120);
			}
			
            MqttChannel conchannel = channelService.getMqttChannel(theDeviceId);
            if (conchannel != null) {
                channelService.deleteMqttChannel(conchannel);
            }
        }
        stationManagementService.deleteStation(theDeviceId);
        super.channelInactive(ctx);
    }
    /*
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    }*/

}
