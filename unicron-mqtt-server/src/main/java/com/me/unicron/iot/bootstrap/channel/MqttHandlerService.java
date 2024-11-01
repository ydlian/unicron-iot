package com.me.unicron.iot.bootstrap.channel;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.me.epower.direct.entity.chargecloud.monitor.HeartBeatRecord;
import com.me.epower.direct.enums.ConnectorStatusEnum;
import com.me.epower.direct.repositories.chargecloud.monitor.HeartBeatRecordRepository;
import com.me.epower.entity.OperatorDevice;
import com.me.epower.entity.OperatorKey;
import com.me.epower.repositories.OperatorDeviceRepository;
import com.me.epower.repositories.OperatorKeyRepository;
import com.me.unicron.EncodeUtil;
import com.me.unicron.RSACoder;
import com.me.unicron.common.server.ServerList;
import com.me.unicron.date.DateUtils;
import com.me.unicron.iot.bean.ClientConnectionInfo;
import com.me.unicron.iot.bootstrap.BaseApi;
import com.me.unicron.iot.bootstrap.ChannelService;
import com.me.unicron.iot.bootstrap.bean.MqttChannel;
import com.me.unicron.iot.bootstrap.bean.SendMqttMessage;
import com.me.unicron.iot.bootstrap.channel.security.ChannelSecurityService;
import com.me.unicron.iot.constant.OrangeConst;
import com.me.unicron.iot.enums.ConfirmStatus;
import com.me.unicron.iot.ip.IpUtils;
import com.me.unicron.iot.mqtt.ClientConnectionService;
import com.me.unicron.iot.mqtt.ServerMqttHandlerService;
import com.me.unicron.station.service.IStationService;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttSubAckPayload;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息处理service
 *
 * @author lianyadong
 * @create 2023-11-21 11:13
 **/
@Component
@Slf4j
public class MqttHandlerService extends ServerMqttHandlerService implements BaseApi {

	@Autowired
	ChannelService mqttChannelService;

	
	@Autowired
    private StationManagementService stationManagementService;
	
	@Autowired
	ClientConnectionService clientConnectionService;
	
	 @Autowired
	 private StringRedisTemplate stringRedisTemplate;
	 @Autowired
	 ChannelSecurityService channelSecurityService;
	 
	@Autowired
	OperatorDeviceRepository operatorDeviceRepository;
	
	@Autowired
    private IStationService iStationService;
	
	@Autowired
	OperatorKeyRepository operatorKeyRepository;
	
	
	private boolean isDeviceRegited(String username,String equitmentId){
		if(username==null || equitmentId==null){
			return false;
		}
		List<OperatorDevice> list=operatorDeviceRepository.findOperatorDeviceByDeviceid(equitmentId);
		if(list==null || list.size()<1){
			log.info("[login_check_fail]设备不存在:equitmentId={}",equitmentId);
			return false;
		}else{
			OperatorDevice device=list.get(0);
			if(device.getOperatorid() !=null && username !=null &&
					device.getOperatorid().toLowerCase().equals(username.toLowerCase())){
				return true;
			}
			log.info("[login_check_fail]设备/用户不匹配:device={},username={}",JSONObject.toJSONString(device),username);
		}
		return false;
	}
	private ClientConnectionInfo checkUserAuth(MqttConnectMessage mqttConnectMessage) {
		String passwd = mqttConnectMessage.payload().password();
		String username = mqttConnectMessage.payload().userName();
		String equitmentId=mqttConnectMessage.payload().clientIdentifier();
		log.info("enter checkUserAuth:_username={},_passwd={},_equitmentId={},mqttConnectMessage={}",
				username,passwd,equitmentId,JSONObject.toJSON(mqttConnectMessage));
		
		ClientConnectionInfo newClient = new ClientConnectionInfo();
		newClient.setAuthCheckResult(false);
		if (org.apache.commons.lang.StringUtils.isBlank(passwd)
				|| org.apache.commons.lang.StringUtils.isBlank(username)) {
			
			return newClient;
		}
		
		
		ClientConnectionInfo keyInfo = channelSecurityService.getSecurityKeyPair(username);
		newClient.setServerPrivateKey(keyInfo.getServerPrivateKey());
		newClient.setServerPublicKey(keyInfo.getServerPublicKey());
		newClient.setServerPrivateKey(keyInfo.getServerPrivateKey());
		
		String clientPublicKey=keyInfo.getClientPublicKey();
		newClient.setClientPublicKey(clientPublicKey);
		newClient.setUserName(username);
		newClient.setDeviceID(equitmentId);
		
		if(!org.apache.commons.lang.StringUtils.isBlank(passwd)){
			passwd=EncodeUtil.replaceAllCRLF(passwd);
		}
		
		
		String checkPasswd = "";
		if(!org.apache.commons.lang.StringUtils.isBlank(passwd) &&
				!org.apache.commons.lang.StringUtils.isBlank(clientPublicKey)){
			try {
				checkPasswd = RSACoder.decodeByPublicKey(passwd,clientPublicKey);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			log.info("[login_check_fail]########passwd or clientPublicKey is null!########passwd={},clientPublicKey={}",passwd,clientPublicKey);
		}
		
	
		//C6S1171020ETZZKSSY
		boolean passCheckResult=false;
		if(StringUtils.isBlank(checkPasswd)){
			
		}else{
			if(checkPasswd.equals(equitmentId)){
				passCheckResult=true;
			}
			/*
			if(_hardPassMap.get(username)!=null && checkPasswd.equals(_hardPassMap.get(username))){
				passCheckResult=true;
			}*/
			
			
		}
		
		if(passCheckResult){
			//增强,未登记的电桩接入不了
			if(!isDeviceRegited(username,equitmentId)){
			 
				newClient.setAuthCheckResult(false);
				log.info("[login_check_fail]checkUserAuth fail:Device INVALID:_username={},_checkPasswd={},_equitmentId={}",username,checkPasswd,equitmentId);
			}else{
				newClient.setAuthCheckResult(true);
				log.info("[login_check_success]checkUserAuth success:_username={},_checkPasswd={},_equitmentId={}",username,checkPasswd,equitmentId);
			}
			
		}else{
			log.info("[login_check_fail]checkUserAuth fail:_username={},_checkPasswd={},_equitmentId={}",username,checkPasswd,equitmentId);
		}
		return newClient;
	}

	
	
	@Override
	public boolean login(Channel channel, MqttConnectMessage mqttConnectMessage) {
		//连接的设备标示

		String username = mqttConnectMessage.payload().userName();
		String equitmentId=mqttConnectMessage.payload().clientIdentifier();
		if(!isDeviceRegited(username,equitmentId)){
			//设备未登记过，关闭连接
			log.info("[login_check_fail] This Device is INVALID: equitmentId={},username={}",equitmentId,username);
			return false;
		}
		// 校验规则 自定义校验规则
		ClientConnectionInfo ckInfo=null;
		try{
			ckInfo=checkUserAuth(mqttConnectMessage);
		}catch(Exception e){
			e.printStackTrace();
		}
		if(ckInfo==null || !ckInfo.isAuthCheckResult()){
			//暂不校验
			log.info("[login_check_fail] Error password/username,username={},password={},connDeviceId={}",mqttConnectMessage.payload().userName(),mqttConnectMessage.payload().password(),equitmentId);
			//return false;
		}else{
			log.info("[login_check_fail] username={},password={},connDeviceId={}",mqttConnectMessage.payload().userName(),mqttConnectMessage.payload().password(),equitmentId);
		}
		// 通过通道->IP 查询此设备是否已经注册过
		//boolean isIpPortRegisted = clientConnectionService.checkIfChannelUsable(channel);
		
		log.info("[login_check_success]||_com_unicron_logic_server||traceid={}||设备建立连接={}",equitmentId,JSONObject.toJSON(ClientConnectionService.getOnlineChannels()));
		
		
		//ClientConnectionInfo newClient=clientConnectionService.creatConnection(ckInfo,channel, connDeviceId);
		//clientConnectionService.updateConnection(connDeviceId, newClient);
		
		/*
		StationInfo station = new StationInfo();
        String equipmentId=connDeviceId;
        //存储的时候需要去掉填充0
        equipmentId = EncodeUtil.cutCharsequence(equipmentId);
        station.setStationEquipId(equipmentId);
        station.setLastAliveSignalTime(System.currentTimeMillis());
        station.setStationOnlineStatus(true);
		stationManagementService.addStation(equipmentId, station);
		*/
		
		//登陆状态保存120秒，服务器重启时，心跳就会断开，状态超时
		iStationService.updateStationConnectStatus(equitmentId, "1",120);
		
		//stringRedisTemplate.opsForValue().set(RedisConstant.UNICRON_IS_DEVICE_ONLINE+connDeviceId, "1");
		//更新设备状态
        iStationService.updateEquipmentStatus(equitmentId, ConnectorStatusEnum.IDLE.getCode());
		mqttChannelService.loginSuccess(channel, equitmentId, mqttConnectMessage);
		// 注册设备
		clientConnectionService.creatConnection(ckInfo,channel, equitmentId);
			
				
		return true;
		

	}

	private void connectBack(Channel channel, MqttConnectReturnCode connectReturnCode) {
		MqttConnAckVariableHeader mqttConnAckVariableHeader = new MqttConnAckVariableHeader(connectReturnCode, true);
		MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE,
				false, 0x02);
		MqttConnAckMessage connAck = new MqttConnAckMessage(mqttFixedHeader, mqttConnAckVariableHeader);
		channel.writeAndFlush(connAck);
	}

	
	/**
	 * 发布
	 */
	@Override
	public void publish(Channel channel, MqttPublishMessage mqttPublishMessage) {
		mqttChannelService.publishSuccess(channel, mqttPublishMessage);
	}

	/**
	 * 订阅
	 */
	@Override
	public void subscribe(Channel channel, MqttSubscribeMessage mqttSubscribeMessage) {
		Set<String> topics = mqttSubscribeMessage.payload().topicSubscriptions().stream()
				.map(mqttTopicSubscription -> mqttTopicSubscription.topicName()).collect(Collectors.toSet());

		String deviceId = mqttChannelService.getDeviceId(channel);
//		if (deviceId == null) {
//			deviceId = clientConnectionService.getDeviceId(channel);
//		}
		log.debug("【服务器】回复订阅，设置Topic订阅成功：" + deviceId);
		mqttChannelService.suscribeSuccess(deviceId, topics);

		// by sofalala
		subBack(channel, mqttSubscribeMessage, topics.size());
	}

	private void subBack(Channel channel, MqttSubscribeMessage mqttSubscribeMessage, int num) {
		MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE,
				false, 0);
		MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader
				.from(mqttSubscribeMessage.variableHeader().messageId());
		List<Integer> grantedQoSLevels = new ArrayList<>(num);
		for (int i = 0; i < num; i++) {
			grantedQoSLevels.add(mqttSubscribeMessage.payload().topicSubscriptions().get(i).qualityOfService().value());
		}
		MqttSubAckPayload payload = new MqttSubAckPayload(grantedQoSLevels);
		MqttSubAckMessage mqttSubAckMessage = new MqttSubAckMessage(mqttFixedHeader, variableHeader, payload);
		channel.writeAndFlush(mqttSubAckMessage);
	}

	/**
	 * 关闭通道
	 */
	@Override
	public void close(Channel channel) {
		String deviceId = mqttChannelService.getDeviceId(channel);
//		if (deviceId == null) {
//			deviceId = clientConnectionService.getDeviceId(channel);
//		}
		if(deviceId!=null){
			mqttChannelService.closeSuccess(deviceId, false);
		}
		
		channel.close();
	}

	/**
	 * 回复pong消息
	 */
	@Override
	public void pong(Channel channel) {
		if (channel.isOpen() && channel.isActive() && channel.isWritable()) {
			
			MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0);
            channel.writeAndFlush(new MqttMessage(fixedHeader));
			String deviceId = mqttChannelService.getDeviceId(channel);
			log.info("收到来自：{} 的pong心跳",deviceId);
			
			HeartBeatRecord heartBeatRecord=new HeartBeatRecord();
	        heartBeatRecord.setBeat_time(DateUtils.getCurDateTime());
	        heartBeatRecord.setEquipment_id(deviceId);
	        heartBeatRecord.setMsg("pong");
	        heartBeatRecord.setNodeserver(IpUtils.getHost());
	        String clientip= channel.remoteAddress().toString();
			heartBeatRecord.setClientip(clientip);
			//.save(heartBeatRecord);
			iStationService.saveHeartBeatData(heartBeatRecord);
			
			iStationService.updateStationConnectStatus(deviceId, "1",120);
			//更新
			ClientConnectionInfo client=clientConnectionService.getThisNodeConnectInfo(deviceId);
			if(client!=null){
				client.setRepeatConnection(true);
			
				clientConnectionService.updateConnection(channel,deviceId);
				
			}
			
		}
	}

	/**
	 * 取消订阅
	 */
	@Override
	public void unsubscribe(Channel channel, MqttUnsubscribeMessage mqttMessage) {
		List<String> topics1 = mqttMessage.payload().topics();
		mqttChannelService.unsubscribe(mqttChannelService.getDeviceId(channel), topics1);
		unSubBack(channel, mqttMessage.variableHeader().messageId());
	}

	/**
	 * 回复取消订阅
	 */
	private void unSubBack(Channel channel, int messageId) {
		MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE,
				false, 0x02);
		MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(messageId);
		MqttUnsubAckMessage mqttUnsubAckMessage = new MqttUnsubAckMessage(mqttFixedHeader, variableHeader);
		channel.writeAndFlush(mqttUnsubAckMessage);
	}

	/**
	 * 消息回复确认(qos1 级别 保证收到消息 但是可能会重复)
	 */
	@Override
	public void puback(Channel channel, MqttMessage mqttMessage) {
		
		MqttMessageIdVariableHeader messageIdVariableHeader = (MqttMessageIdVariableHeader) mqttMessage
				.variableHeader();
		int messageId = messageIdVariableHeader.messageId();
		log.info("服务器消息回复确认:messageId={}",messageId);
		MqttChannel mqttchannel=mqttChannelService.getMqttChannel(mqttChannelService.getDeviceId(channel));
		if(mqttchannel==null){
			log.info("++++++++++MqttChannel is null!+++++++++++++++");
		}
		mqttchannel.getSendMqttMessage(messageId).setConfirmStatus(ConfirmStatus.COMPLETE); // 复制为空
	}

	/**
	 * disconnect 主动断线
	 */
	@Override
	public void disconnect(Channel channel) {
		String deviceId = mqttChannelService.getDeviceId(channel);
		if (deviceId == null) {
//			deviceId = clientConnectionService.getDeviceId(channel);
			
		}else{
			ClientConnectionInfo client=clientConnectionService.getThisNodeConnectInfo(deviceId);
			if(client!=null && !client.isRepeatConnection()){
				iStationService.updateStationConnectStatus(deviceId, "0",120);
			}
			
		}
		mqttChannelService.closeSuccess(deviceId, true);
	}

	/**
	 * qos2 发布收到
	 */
	@Override
	public void pubrec(Channel channel, MqttMessage mqttMessage) {

		//log.info("【服务器发布收到】");
		MqttMessageIdVariableHeader messageIdVariableHeader = (MqttMessageIdVariableHeader) mqttMessage
				.variableHeader();
		int messageId = messageIdVariableHeader.messageId();
		mqttChannelService.getMqttChannel(mqttChannelService.getDeviceId(channel)).getSendMqttMessage(messageId)
				.setConfirmStatus(ConfirmStatus.PUBREL); // 复制为空
		mqttChannelService.doPubrec(channel, messageId);
	}

	/**
	 * qos2 发布释放
	 */
	@Override
	public void pubrel(Channel channel, MqttMessage mqttMessage) {
		MqttMessageIdVariableHeader mqttMessageIdVariableHeader = (MqttMessageIdVariableHeader) mqttMessage
				.variableHeader();
		int messageId = mqttMessageIdVariableHeader.messageId();
		mqttChannelService.getMqttChannel(mqttChannelService.getDeviceId(channel)).getSendMqttMessage(messageId)
				.setConfirmStatus(ConfirmStatus.COMPLETE); // 复制为空
		mqttChannelService.doPubrel(channel, messageId);

	}

	/**
	 * qos2 发布完成
	 */
	@Override
	public void pubcomp(Channel channel, MqttMessage mqttMessage) {
		MqttMessageIdVariableHeader mqttMessageIdVariableHeader = (MqttMessageIdVariableHeader) mqttMessage
				.variableHeader();
		int messageId = mqttMessageIdVariableHeader.messageId();
		SendMqttMessage sendMqttMessage = mqttChannelService.getMqttChannel(mqttChannelService.getDeviceId(channel))
				.getSendMqttMessage(messageId);
		// byte[] byteBuf=new String("ABCDGdqwe").getBytes();
		// sendMqttMessage.setByteBuf(byteBuf);
		sendMqttMessage.setConfirmStatus(ConfirmStatus.COMPLETE); // 复制为空
	}

	@Override
	public void doTimeOut(Channel channel, IdleStateEvent evt) {
		log.info("【PingPongService：doTimeOut 心跳超时】" + channel.remoteAddress() + "【channel 关闭】");
		switch (evt.state()) {
		case READER_IDLE:
			close(channel);
		case WRITER_IDLE:
			close(channel);
		case ALL_IDLE:
			close(channel);
		}
	}

	@Override
	public void doLogic(Channel channel, MqttMessage mqttMessage) {
		// TODO Auto-generated method stub
		// 根据约定的协议做逻辑处理
		MqttMessage ms = mqttMessage;
		if (ms.payload() != null) {
			ByteBuf buf = (ByteBuf) ms.payload();
			log.info(buf.toString());

			byte[] req = new byte[buf.readableBytes()];
			buf.readBytes(req);
			try {
				String body = new String(req, "UTF-8");
				log.info("[服务器接收到]" + body.length() + "|" + body);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		mqttChannelService.publishSuccess(channel, (MqttPublishMessage) mqttMessage);
	}

	

}
