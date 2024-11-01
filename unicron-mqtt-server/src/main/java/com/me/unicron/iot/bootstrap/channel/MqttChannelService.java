package com.me.unicron.iot.bootstrap.channel;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.bootstrap.bean.MqttChannel;
import com.me.unicron.iot.bootstrap.bean.RetainMessage;
import com.me.unicron.iot.bootstrap.bean.SendMqttMessage;
import com.me.unicron.iot.bootstrap.bean.SessionMessage;
import com.me.unicron.iot.bootstrap.bean.WillMeaasge;
import com.me.unicron.iot.bootstrap.coder.server.ServerDecoder;
import com.me.unicron.iot.bootstrap.coder.server.ServerDecoderV0;
import com.me.unicron.iot.enums.ConfirmStatus;
import com.me.unicron.iot.enums.SessionStatus;
import com.me.unicron.iot.enums.SubStatus;
import com.me.unicron.iot.exception.ConnectionException;
import com.me.unicron.iot.util.ByteBufUtil;
import com.me.unicron.iot.util.ServerDecoderUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectPayload;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttConnectVariableHeader;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;

/**
 * channel事件处理service
 *
 * @author 
 * @create 2023-11-21 13:59
 **/
@Slf4j
@Component
public class MqttChannelService extends AbstractChannelService{


	@Autowired
    private ServerDecoder serverDecoder;
	
	@Autowired
    private ServerDecoderV0 serverDecoderV0;
	
    @Autowired
    private ClientSessionService clientSessionService;
    
    //@Autowired
    //private ClientConnectionService clientConectionService;

    @Autowired
    private WillService willService;

    private final ScanRunnable scanRunnable;
    
    public MqttChannelService(ScanRunnable scanRunnable) {
        super(scanRunnable);
        this.scanRunnable = scanRunnable;
    }


    /**
     * 取消订阅
     */
    @Override
    public void unsubscribe(String deviceId, List<String> topics1) {
        Optional.ofNullable(mqttChannels.get(deviceId)).ifPresent(mqttChannel -> {
            topics1.forEach(topic -> {
                deleteChannel(topic,mqttChannel);
            });
        });
    }

    /**
     * 登录成功后 回复
     */
    private void replyLogin(Channel channel, String deviceId,MqttConnectMessage mqttConnectMessage) {
       
            MqttFixedHeader mqttFixedHeader1 = mqttConnectMessage.fixedHeader();
            MqttConnectVariableHeader mqttConnectVariableHeader = mqttConnectMessage.variableHeader();
            final MqttConnectPayload payload = mqttConnectMessage.payload();
            //
            //String deviceId = payload.clientIdentifier();
            MqttChannel build = MqttChannel.builder().channel(channel).cleanSession(mqttConnectVariableHeader.isCleanSession())
                    .deviceId(payload.clientIdentifier())
                    .sessionStatus(SessionStatus.OPEN)
                    .isWill(mqttConnectVariableHeader.isWillFlag())
                    .subStatus(SubStatus.NO)
                    .topic(new CopyOnWriteArraySet<>())
                    .message(new ConcurrentHashMap<>())
                    .receive(new CopyOnWriteArraySet<>())
                    .build();
            if (connectSuccess(deviceId, build)) { // 初始化存储mqttchannel
                if (mqttConnectVariableHeader.isWillFlag()) { // 遗嘱消息标志
                    boolean b = doIf(mqttConnectVariableHeader, mqttConnectVariableHeader1 -> (payload.willMessage() != null)
                            , mqttConnectVariableHeader1 -> (payload.willTopic() != null));
                    if (!b) {
                        throw new ConnectionException("will message and will topic is not null");
                    }
                    // 处理遗嘱消息
                    final WillMeaasge buildWill = WillMeaasge.builder().
                            qos(mqttConnectVariableHeader.willQos())
                            .willMessage(payload.willMessage())
                            .willTopic(payload.willTopic())
                            .isRetain(mqttConnectVariableHeader.isWillRetain())
                            .build();
                    willService.save(payload.clientIdentifier(), buildWill);
                } else {
                    willService.del(payload.clientIdentifier());
                    boolean b = doIf(mqttConnectVariableHeader, mqttConnectVariableHeader1 -> (!mqttConnectVariableHeader1.isWillRetain()),
                            mqttConnectVariableHeader1 -> (mqttConnectVariableHeader1.willQos() == 0));
                    if (!b) {
                        throw new ConnectionException("will retain should be  null and will QOS equal 0");
                    }
                }
                doIfElse(mqttConnectVariableHeader, mqttConnectVariableHeader1 -> (mqttConnectVariableHeader1.isCleanSession()), mqttConnectVariableHeader1 -> {
                    MqttConnectReturnCode connectReturnCode = MqttConnectReturnCode.CONNECTION_ACCEPTED;
                    MqttConnAckVariableHeader mqttConnAckVariableHeader = new MqttConnAckVariableHeader(connectReturnCode, false);
                    MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(
                            MqttMessageType.CONNACK, mqttFixedHeader1.isDup(), MqttQoS.AT_MOST_ONCE, mqttFixedHeader1.isRetain(), 0x02);
                    MqttConnAckMessage connAck = new MqttConnAckMessage(mqttFixedHeader, mqttConnAckVariableHeader);
                    channel.writeAndFlush(connAck);// 清理会话
                }, mqttConnectVariableHeader1 -> {
                    MqttConnectReturnCode connectReturnCode = MqttConnectReturnCode.CONNECTION_ACCEPTED;
                    MqttConnAckVariableHeader mqttConnAckVariableHeader = new MqttConnAckVariableHeader(connectReturnCode, true);
                    MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(
                            MqttMessageType.CONNACK, mqttFixedHeader1.isDup(), MqttQoS.AT_MOST_ONCE, mqttFixedHeader1.isRetain(), 0x02);
                    MqttConnAckMessage connAck = new MqttConnAckMessage(mqttFixedHeader, mqttConnAckVariableHeader);
                    channel.writeAndFlush(connAck);// 非清理会话

                });         //发送 session  数据
                ConcurrentLinkedQueue<SessionMessage> sessionMessages = clientSessionService.getByteBuf(payload.clientIdentifier());
                doIfElse(sessionMessages, messages -> messages != null && !messages.isEmpty(), byteBufs -> {
                    SessionMessage sessionMessage;
                    while ((sessionMessage = byteBufs.poll()) != null) {
                        switch (sessionMessage.getQoS()) {
                            case EXACTLY_ONCE:
                            	//sendQos0Msg(channel, sessionMessage.getTopic(), sessionMessage.getByteBuf());
                                sendQosConfirmMsg(MqttQoS.EXACTLY_ONCE,getMqttChannel(deviceId), sessionMessage.getTopic(), sessionMessage.getByteBuf());
                                break;
                            case AT_MOST_ONCE:
                                sendQos0Msg(channel, sessionMessage.getTopic(), sessionMessage.getByteBuf());
                                break;
                            case AT_LEAST_ONCE:
                            	//sendQos0Msg(channel, sessionMessage.getTopic(), sessionMessage.getByteBuf());
                                sendQosConfirmMsg(MqttQoS.AT_LEAST_ONCE,getMqttChannel(deviceId), sessionMessage.getTopic(), sessionMessage.getByteBuf());
                                break;
						default:
							break;
                        }
                    }

                });
            }
        
    }


    
    /**
     * qos2 第二步
     */
    @Override
    public void doPubrel(Channel channel, int messageId) {
        MqttChannel mqttChannel = getMqttChannel(getDeviceId(channel));
        doIfElse(mqttChannel,mqttChannel1 ->mqttChannel1.isLogin(),mqttChannel1 -> {
            mqttChannel1.removeRecevice(messageId);
            sendToPubComp(channel,messageId);
        });
    }



    /**
     * qos2 第三步
     */
    @Override
    public void doPubrec(Channel channel, int mqttMessage) {
        sendPubRel(channel,false,mqttMessage);
    }

    /**
     * 连接成功后
     * @param deviceId
     * @param build
     */
    @Override
    public boolean connectSuccess(String deviceId, MqttChannel build) {
    	MqttChannel channel = mqttChannels.get(deviceId);
    	
    	log.info("[connectSuccess] deviceId:"+deviceId+",channe="+JSONObject.toJSONString(channel));
    	if(channel == null){
    		log.info("设备首次连接，存储通道信息：");
    		mqttChannels.put(deviceId,build);
            return true;
    	}
        return  Optional.ofNullable(mqttChannels.get(deviceId))
                .map(mqttChannel -> {
                    switch (mqttChannel.getSessionStatus()){
                        case OPEN:
                            return false;
                        case CLOSE:
                            switch (mqttChannel.getSubStatus()){
                                case YES: // 清除订阅  topic
                                    deleteSubTopic(mqttChannel);
                                case NO:
                            }
                    }
                    mqttChannels.put(deviceId,build);
                    return true;
                }).orElseGet(() -> {
                    mqttChannels.put(deviceId,build);
                    return  true;
                });
    }


    /**
     * 订阅成功后 (发送保留消息)
     */
    public void suscribeSuccess(String deviceId, Set<String> topics){
        doIfElse(topics,topics1->!CollectionUtils.isEmpty(topics1),strings -> {
        	
            MqttChannel mqttChannel = mqttChannels.get(deviceId);
            mqttChannel.setSubStatus(SubStatus.YES); // 设置订阅主题标识
            mqttChannel.addTopic(strings);
            executorService.execute(() -> {
                Optional.ofNullable(mqttChannel).ifPresent(mqttChannel1 -> {
                    if(mqttChannel1.isLogin()){
                        strings.parallelStream().forEach(topic -> {
                            addChannel(topic,mqttChannel);
                            addMqttChannel(deviceId,mqttChannel);//sofalala 20180313
                            sendRetain(topic,mqttChannel); // 发送保留消息
                        });
                    }
                });
            });
        });
    }


    /**
     *成功登陆 (发送会话消息)
     * @param channel
     * @param deviceId
     * @param mqttConnectMessage
     */
    @Override
    public void loginSuccess(Channel channel, String deviceId, MqttConnectMessage mqttConnectMessage) {
        channel.attr(_login).set(true);
        channel.attr(_deviceId).set(deviceId);
        log.debug("【loginSuccess】设备连接成功："+deviceId);
        replyLogin(channel, deviceId, mqttConnectMessage);
    }


    /**
     * 发布消息成功 ()
     * @param channel
     * @param mqttPublishMessage
     */
    @Override
    public void publishSuccess(Channel channel, MqttPublishMessage mqttPublishMessage) {
        MqttFixedHeader mqttFixedHeader = mqttPublishMessage.fixedHeader();
        MqttPublishVariableHeader mqttPublishVariableHeader = mqttPublishMessage.variableHeader();
        MqttChannel mqttChannel = getMqttChannel(getDeviceId(channel));
        ByteBuf payload = mqttPublishMessage.payload();
        byte[] bytes = ByteBufUtil.copyByteBuf(payload); //
        int messageId = mqttPublishVariableHeader.messageId();
        executorService.execute(() -> {
            if (channel.hasAttr(_login) && mqttChannel != null) {
            	MqttQoS qos=mqttFixedHeader.qosLevel();
            	log.info("MqttChannelService:publishSuccess:qos={},messageId={}",qos,messageId);
                switch (mqttFixedHeader.qosLevel()) {
                    case AT_MOST_ONCE: // 至多一次
                        break;
                    case AT_LEAST_ONCE:
                    	
                        sendPubBack(channel, messageId);
                        break;
                    case EXACTLY_ONCE:
                    	//log.info("MqttChannelService:publishSuccess:EXACTLY_ONCE:messageId={}",messageId);
                        sendPubRec(mqttChannel, messageId);
                        break;
				default:
					break;
                }
                //
                if (!mqttChannel.checkRecevice(messageId)) {
                    push(mqttPublishVariableHeader.topicName(), mqttFixedHeader.qosLevel(), bytes);
                    mqttChannel.addRecevice(messageId);
                }
                if (mqttFixedHeader.isRetain() && mqttFixedHeader.qosLevel() != MqttQoS.AT_MOST_ONCE) { //是保留消息  qos >0
                    saveRetain(mqttPublishVariableHeader.topicName(),
                            RetainMessage.builder()
                                    .byteBuf(bytes)
                                    .qoS(mqttFixedHeader.qosLevel())
                                    .build(), false);
                } else if (mqttFixedHeader.isRetain() && mqttFixedHeader.qosLevel() == MqttQoS.AT_MOST_ONCE) { // 是保留消息 qos=0  清除之前保留消息 保留现在
                    saveRetain(mqttPublishVariableHeader.topicName(),
                            RetainMessage.builder()
                                    .byteBuf(bytes)
                                    .qoS(mqttFixedHeader.qosLevel())
                                    .build(), true);
                }
            }
        });

    }
    /**
     * 推送消息给订阅者
     */
    public  void push( String topic,MqttQoS qos, byte[] bytes){

    	int cmd_no = serverDecoder.getCmdNo(bytes);
    	String version=ServerDecoderUtil.getClientkProtocolVersion(bytes);
    	//推送的消息，强制使用Qos 0
    	//MqttQoS local_qos=MqttQoS.AT_MOST_ONCE;
    	
        Collection<MqttChannel> subChannels = getChannels(topic, topic1 -> cacheMap.getData(getTopic(topic1)));
        
        if(!CollectionUtils.isEmpty(subChannels)){
            subChannels.parallelStream().forEach(subChannel -> {
                switch (subChannel.getSessionStatus()){
                    case OPEN: // 在线
                        if(subChannel.isActive()){ // 防止channel失效  但是离线状态没更改
                        	
                        	log.info("push:发送字节,方向:服务器->充电桩,version={},cmd_no={},qos={},bytes={}",version,cmd_no,JSONObject.toJSON(qos),EncodeUtil.printHex(bytes));
                            
                            if(qos.value()!=MqttQoS.AT_MOST_ONCE.value() && bytes.length<=0){
                            	//log.info("push:发送给{},非AT_MOST_ONCE空消息，cmd_no={},大小={},{}",topic,cmd_no,bytes.length,EncodeUtil.printHex(bytes));
                            	//payload空消息就不发了
                            	break;
                            }else{
                            	switch (qos){
	                                case AT_LEAST_ONCE:
	                                	
	                                    sendQosConfirmMsg(MqttQoS.AT_LEAST_ONCE,subChannel,topic,bytes);
	                                    break;
	                                case AT_MOST_ONCE:
	                                    sendQos0Msg(subChannel.getChannel(),topic,bytes);
	                                    break;
	                                case EXACTLY_ONCE:
	                                	//log.info("MqttChannelService:push:EXACTLY_ONCE:messageId={}",messageId);
	                                    sendQosConfirmMsg(MqttQoS.EXACTLY_ONCE,subChannel,topic,bytes);
	                                    break;
	                                default:
	                                	  break;
	                            }
                            	//break;
                            }
                            
                        }
                        else{
                            if(!subChannel.isCleanSession()){
                                clientSessionService.saveSessionMsg(subChannel.getDeviceId(),
                                        SessionMessage.builder().byteBuf(bytes).qoS(qos).topic(topic).build() );
                                break;
                            }
                        }
                        break;
                    case CLOSE: // 连接 设置了 clean session =false
                        clientSessionService.saveSessionMsg(subChannel.getDeviceId(),
                                SessionMessage.builder().byteBuf(bytes).qoS(qos).topic(topic).build() );
                        break;
                }
            });
        }
    }

    /**
     * 关闭channel 操作
     * @param deviceId
     */
    @Override
    public void closeSuccess(String deviceId,boolean isDisconnect) {
        if(StringUtils.isNotBlank(deviceId)){
            executorService.execute(() -> {
                MqttChannel mqttChannel = mqttChannels.get(deviceId);
                Optional.ofNullable(mqttChannel).ifPresent(mqttChannel1 -> {
                    mqttChannel1.setSessionStatus(SessionStatus.CLOSE); // 设置关闭
                    mqttChannel1.close(); // 关闭channel
                    mqttChannel1.setChannel(null);
                    if(!mqttChannel1.isCleanSession()){ // 保持会话
                        // 处理 qos1 未确认数据
                        ConcurrentHashMap<Integer, SendMqttMessage> message = mqttChannel1.getMessage();
                        Optional.ofNullable(message).ifPresent(integerConfirmMessageConcurrentHashMap -> {
                            integerConfirmMessageConcurrentHashMap.forEach((integer, confirmMessage) -> doIfElse(confirmMessage, sendMqttMessage ->sendMqttMessage.getConfirmStatus()== ConfirmStatus.PUB, sendMqttMessage ->{
                                        clientSessionService.saveSessionMsg(mqttChannel.getDeviceId(), SessionMessage.builder()
                                                .byteBuf(sendMqttMessage.getByteBuf())
                                                .qoS(sendMqttMessage.getQos())
                                                .topic(sendMqttMessage.getTopic())
                                                .build()); // 把待确认数据转入session中
                                    }
                            ));

                        });
                    }
                    else{  // 删除sub topic-消息
                        mqttChannels.remove(deviceId); // 移除channelId  不保持会话 直接删除  保持会话 旧的在重新connect时替换
                        switch (mqttChannel1.getSubStatus()){
                            case YES:
                                deleteSubTopic(mqttChannel1);
                                break;
                            case NO:
                                break;
                        }
                    }
                    if(mqttChannel1.isWill()){     // 发送遗言
                        if(!isDisconnect){ // 不是disconnection操作
                            willService.doSend(deviceId);
                        }
                    }
                });
            });
        }
    }

    /**
     * 清除channel 订阅主题
     * @param mqttChannel
     */
    public void  deleteSubTopic(MqttChannel mqttChannel){
        Set<String> topics = mqttChannel.getTopic();
        topics.parallelStream().forEach(topic -> {
            cacheMap.delete(getTopic(topic),mqttChannel);
        });
    }

    /**
     * 发送 遗嘱消息(有的channel 已经关闭 但是保持了 session  此时加入session 数据中 )
     * @param willMeaasge 遗嘱消息
     */
    public void sendWillMsg(WillMeaasge willMeaasge){
        Collection<MqttChannel> mqttChannels = getChannels(willMeaasge.getWillTopic(), topic -> cacheMap.getData(getTopic(topic)));
        if(!CollectionUtils.isEmpty(mqttChannels)){
            mqttChannels.forEach(mqttChannel -> {
                switch (mqttChannel.getSessionStatus()){
                    case CLOSE:
                        clientSessionService.saveSessionMsg(mqttChannel.getDeviceId(),
                                SessionMessage.builder()
                                        .topic(willMeaasge.getWillTopic())
                                        .qoS(MqttQoS.valueOf(willMeaasge.getQos()))
                                        .byteBuf(willMeaasge.getWillMessage().getBytes()).build());
                        break;
                    case OPEN:
                        writeWillMsg(mqttChannel,willMeaasge);
                        break;
                }
            });
        }
    }

    /**
     * 保存保留消息
     * @param topic 主题
     * @param retainMessage 信息
     */
    private void saveRetain(String topic, RetainMessage retainMessage, boolean isClean){
        ConcurrentLinkedQueue<RetainMessage> retainMessages = retain.getOrDefault(topic, new ConcurrentLinkedQueue<>());
        if(!retainMessages.isEmpty() && isClean){
            retainMessages.clear();
        }
        boolean flag;
        do{
            flag = retainMessages.add(retainMessage);
        }
        while (!flag);
        retain.put(topic, retainMessages);
    }

    /**
     * 发送保留消息
     */
    public  void sendRetain(String topic,MqttChannel mqttChannel){
        retain.forEach((_topic, retainMessages) -> {
            if(StringUtils.startsWith(_topic,topic)){
                Optional.ofNullable(retainMessages).ifPresent(pubMessages1 -> {
                    retainMessages.parallelStream().forEach(retainMessage -> {
                        log.info("【发送保留消息】"+mqttChannel.getChannel().remoteAddress()+":"+retainMessage.getString()+"【成功】");
                        switch (retainMessage.getQoS()){
                            case AT_MOST_ONCE:
                                sendQos0Msg(mqttChannel.getChannel(),_topic,retainMessage.getByteBuf());
                                break;
                            case AT_LEAST_ONCE:
                                sendQosConfirmMsg(MqttQoS.AT_LEAST_ONCE,mqttChannel,_topic,retainMessage.getByteBuf());
                                break;
                            case EXACTLY_ONCE:
                                sendQosConfirmMsg(MqttQoS.EXACTLY_ONCE,mqttChannel,_topic,retainMessage.getByteBuf());
                                break;
                        }
                    });
                });
            }
        });

    }


	@Override
	public void deleteMqttChannel(MqttChannel mqttChannel) {
		// TODO Auto-generated method stub
	}





}
