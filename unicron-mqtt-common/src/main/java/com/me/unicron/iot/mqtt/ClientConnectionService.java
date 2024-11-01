package com.me.unicron.iot.mqtt;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.me.unicron.iot.bean.ClientConnectionInfo;
import com.me.unicron.iot.bean.ClientOfflineInfo;
import com.me.unicron.iot.ip.IpUtils;
import com.me.unicron.iot.util.SpringBeanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.me.epower.direct.entity.constant.RedisConstant;
import com.me.unicron.common.server.ServerList;
import com.me.unicron.date.DateUtils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ClientConnectionService {

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	private final static int MIN_SPAN_MSECONDS = 200;

	private final static String GLB_OFFINLE_KEY = RedisConstant.UNICRON_GLOBLE_OFFLINE_LIST;

	private final static String GLOBLE_CONNECT_LIST_CACHE_KEY = RedisConstant.UNICRON_GLOBLE_CONNECT_LIST;

	public static ConcurrentHashMap<String, ClientConnectionInfo> getOnlineChannels() {
		return onlineChannels;
	}

	protected static ConcurrentHashMap<String, ClientConnectionInfo> onlineChannels = new ConcurrentHashMap<>(); // deviceId
																													// ->
																													// Channel

	protected static ConcurrentHashMap<String, Long> _devicemap = new ConcurrentHashMap<String, Long>();
	private static final String IP_CUT_REG = "(\\/|\\\\){1,}";
	private static final String SEPRATOR=":";
	// 建立连接时
	public void printConnectionInfo() {
		String cacheKey = GLOBLE_CONNECT_LIST_CACHE_KEY;
		Map<Object, Object> globleResult = stringRedisTemplate.opsForHash().entries(cacheKey);

		log.info("全局客户端连接数={}，Globle={},本地客户端连接数={},本地连接={}", globleResult.size(), JSONObject.toJSON(globleResult),
				onlineChannels.size(), JSONObject.toJSON(onlineChannels));

	}

	public boolean isClientRealOnline(String theDeviceId) {
		if (onlineChannels.get(theDeviceId) != null) {
			return true;
		}
		String cacheKey = GLOBLE_CONNECT_LIST_CACHE_KEY;
		HashOperations<String, String, String> vo = stringRedisTemplate.opsForHash();
		if (vo.get(cacheKey, theDeviceId) != null) {
			return true;
		}
		return false;
	}

	public boolean isClientThisNodeOnline(String theDeviceId) {
		if (onlineChannels.get(theDeviceId) != null) {
			return true;
		}

		return false;
	}


	public boolean checkDeviceIfLocalNodeExist(Channel channel, String checkDeviceId) {

		boolean result = false;
		ClientConnectionInfo client = onlineChannels.get(checkDeviceId);
		if (client != null) {
			return true;
		}
		log.info("[通道重复检查]重复状态：{},{},{}", result, checkDeviceId, JSON.toJSONString(onlineChannels));
		return result;
	}

	private String getRemoteAddr(ChannelHandlerContext ctx) {
		String remoteAddr = ctx.channel().remoteAddress().toString();
		remoteAddr = remoteAddr.replaceAll(IP_CUT_REG, "");
		return remoteAddr;
	}

	public ClientConnectionInfo getThisNodeConnectInfo(String theDeviceId) {
		ClientConnectionInfo con = onlineChannels.get(theDeviceId);
		return con;
	}

	public ClientConnectionInfo getGlobleConnectInfo(String theDeviceId) {
		String cacheKey = GLOBLE_CONNECT_LIST_CACHE_KEY;
		HashOperations<String, String, String> vo = stringRedisTemplate.opsForHash();
		String data = vo.get(cacheKey, theDeviceId);
		ClientConnectionInfo client = null;
		try {
			client = JSON.parseObject(data, ClientConnectionInfo.class);
		} catch (Exception e) {
			client = null;
			e.printStackTrace();
		}

		return client;
	}

	public ClientConnectionInfo getClientConnectionInfoByRemoteIp(String ip) {
		if (StringUtils.isBlank(ip)) {
			return null;
		}
		log.info("RemoteIp:"+ip);
		try {
			Set<String> keys = onlineChannels.keySet();
			for (String key : keys) {
				ClientConnectionInfo clientConnectionInfo = onlineChannels.get(key);
				if (clientConnectionInfo != null && ip.equals(clientConnectionInfo.getIp())) {
					return clientConnectionInfo;
				}
			}
			String cacheKey = GLOBLE_CONNECT_LIST_CACHE_KEY;
			HashOperations<String, String, String> vo = stringRedisTemplate.opsForHash();
			Map<String, String> map = vo.entries(cacheKey);
			for (String string : map.keySet()) {
				String data = map.get(string);
				ClientConnectionInfo clientConnectionInfo = JSON.parseObject(data, ClientConnectionInfo.class);
				if (clientConnectionInfo != null && ip.equals(clientConnectionInfo.getIp())) {
					return clientConnectionInfo;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			log.info(e.getMessage());
		}

		return null;
	}

	private void forceWriteRedis(HashOperations<String, String, String> vo, String key, String data) {

	}

	// 建立连接时
	public ClientConnectionInfo creatConnection(ClientConnectionInfo inputInfo, Channel channel, String theDeviceId) {
		String remoteAddr = channel.remoteAddress().toString();
		remoteAddr = remoteAddr.replaceAll(IP_CUT_REG, "");
		// String channelKey=remoteAddr;

		ClientConnectionInfo client = new ClientConnectionInfo();
		client.setChannel(channel);
		String connectDateTime = DateUtils.getCurLongDateStr();
		client.setConnectDateTime(connectDateTime);
		client.setDeviceID(theDeviceId);
		client.setIpPort(remoteAddr);
		if (remoteAddr.indexOf(":") > 0) {
			client.setIp(remoteAddr.split(":")[0]);
		} else {
			client.setIp(remoteAddr);
		}
		String localIp = IpUtils.getHost();
		client.setLocalServerIp(localIp);

		client.setAuthCheckResult(inputInfo.isAuthCheckResult());
		client.setClientPublicKey(inputInfo.getClientPublicKey());
		client.setServerPublicKey(inputInfo.getServerPublicKey());
		client.setServerPrivateKey(inputInfo.getServerPrivateKey());
		client.setUserName(inputInfo.getUserName());
		client.setLastLiveTime(DateUtils.getCurDateTime());
		if (inputInfo.isOnline()) {
			client.setOnline(true);
		}

		String ip = IpUtils.getHost();
		if (ServerList.isLogicServer(ip)) {
			onlineChannels.put(theDeviceId, client);
			// String cacheKey = CACHE_KEY;
			HashOperations<String, String, String> vo = stringRedisTemplate.opsForHash();
			String val = vo.get(GLOBLE_CONNECT_LIST_CACHE_KEY, theDeviceId);
			forceWriteRedis(vo, theDeviceId, client.toString());
			log.info("本机是逻辑服务器：{},更新在线状态,key={},val={}", ip, theDeviceId, val);
			
			_devicemap.put(ip+SEPRATOR+theDeviceId, System.currentTimeMillis());
			creatConnection4Offline(client, channel, theDeviceId);
		}

		return client;
	}

	public void creatConnection4Offline(ClientConnectionInfo inputInfo, Channel channel, String theDeviceId) {

		if (inputInfo == null || StringUtils.isBlank(theDeviceId)) {
			return;
		}

		ClientOfflineInfo clientOfflineInfo = null;
		String deviceMessage = (String) stringRedisTemplate.opsForHash().get(GLB_OFFINLE_KEY, theDeviceId);
		Date dateNow = DateUtils.getCurDateTime();
		if (StringUtils.isNotBlank(deviceMessage)) {
			log.info("get create Redis Message success: {}", deviceMessage);
			clientOfflineInfo = JSON.parseObject(deviceMessage, ClientOfflineInfo.class);
			if (clientOfflineInfo == null) {
				return;
			}
			clientOfflineInfo.setConnectDateTime(dateNow);
			clientOfflineInfo.setLastLiveTime(dateNow);

			if ("离线".equals(clientOfflineInfo.getConnectionStatus())) {

				if ("未发送".equals(clientOfflineInfo.getSendStatus())) {
					// 未发送的告警信息，恢复在线后不在发送
					clientOfflineInfo.setSendStatus("已发送");
				} else {
					clientOfflineInfo.setSendStatus("未发送");
				}
			} else {
				clientOfflineInfo.setSendStatus("已发送");
			}
			clientOfflineInfo.setConnectionStatus("在线");
		} else {
			log.info("get create Redis Message fail,new client: {}", deviceMessage);
			clientOfflineInfo = new ClientOfflineInfo();
			clientOfflineInfo.setConnectDateTime(dateNow);
			clientOfflineInfo.setIp(inputInfo.getIp());
			clientOfflineInfo.setIpPort(inputInfo.getIpPort());
			clientOfflineInfo.setDeviceID(inputInfo.getDeviceID());
			clientOfflineInfo.setLastLiveTime(dateNow);
			clientOfflineInfo.setLocalServerIp(inputInfo.getLocalServerIp());
			clientOfflineInfo.setConnectionStatus("在线");
			clientOfflineInfo.setUserName(inputInfo.getUserName());
			clientOfflineInfo.setSendStatus("已发送");
		}
		clientOfflineInfo.setOfflineLongSendStatus("已发送");
		String jsonString = JSON.toJSONString(clientOfflineInfo, SerializerFeature.IgnoreErrorGetter);
		stringRedisTemplate.opsForHash().put(GLB_OFFINLE_KEY, theDeviceId, jsonString);
		log.info("create Redis {} ,info:{}", GLB_OFFINLE_KEY, jsonString);

		if (StringUtils.isBlank((String) stringRedisTemplate.opsForHash().get(GLB_OFFINLE_KEY, theDeviceId))) {
			log.info("get create Redis message {} error ,wirte again", jsonString);
			stringRedisTemplate.opsForHash().put(GLB_OFFINLE_KEY, theDeviceId, jsonString);
		}

	}

	public void updateOfflineConnection(String theDeviceId) {

		ClientConnectionInfo con = onlineChannels.get(theDeviceId);

		ClientOfflineInfo clientOfflineInfo = null;
		String deviceMessage = (String) stringRedisTemplate.opsForHash().get(GLB_OFFINLE_KEY, theDeviceId);
		Date dateNow = DateUtils.getCurDateTime();
		if (StringUtils.isNotBlank(deviceMessage)) {
			log.info("get update Redis Message success: {}", deviceMessage);
			clientOfflineInfo = JSON.parseObject(deviceMessage, ClientOfflineInfo.class);
			if (clientOfflineInfo == null) {
				return;
			}
			
		} else {
			log.info("get update Redis Message fail,client create redis fail: {}", theDeviceId);
			clientOfflineInfo = new ClientOfflineInfo();
			clientOfflineInfo.setConnectDateTime(dateNow);
			clientOfflineInfo.setIp(con.getIp());
			clientOfflineInfo.setIpPort(con.getIpPort());
			clientOfflineInfo.setDeviceID(con.getDeviceID());
			clientOfflineInfo.setLastLiveTime(dateNow);
			clientOfflineInfo.setLocalServerIp(con.getLocalServerIp());
			clientOfflineInfo.setUserName(con.getUserName());
			clientOfflineInfo.setSendStatus("已发送");
		}

		clientOfflineInfo.setOfflineLongSendStatus("已发送");
		clientOfflineInfo.setConnectionStatus("在线");
		clientOfflineInfo.setLastLiveTime(dateNow);

		String offlineInfo = JSON.toJSONString(clientOfflineInfo, SerializerFeature.IgnoreErrorGetter);
		stringRedisTemplate.opsForHash().put(GLB_OFFINLE_KEY, theDeviceId, offlineInfo);
		log.info("update Redis {} ,info:{}", GLB_OFFINLE_KEY, offlineInfo);
	}

	// 更新连接时
	public void updateConnection(Channel channel, String theDeviceId) {
		ClientConnectionInfo hisConnect = onlineChannels.get(theDeviceId);
		log.info("updateConnection:{}", theDeviceId);
		String ip = IpUtils.getHost();
		if (!ServerList.isLogicServer(ip)) {
			return;
		}
		ClientConnectionInfo newConnection = new ClientConnectionInfo();
		if (hisConnect != null && hisConnect.getChannel() != null) {
			newConnection = hisConnect;
			newConnection.setLocalServerIp(IpUtils.getHost());
			newConnection.setLastLiveTime(DateUtils.getCurDateTime());
			newConnection.setOnline(true);
			newConnection.setAuthCheckResult(true);
		} else {

			newConnection.setChannel(channel);
			newConnection.setDeviceID(theDeviceId);
			newConnection.setLocalServerIp(IpUtils.getHost());
			newConnection.setLastLiveTime(DateUtils.getCurDateTime());
			newConnection.setOnline(true);
			newConnection.setAuthCheckResult(true);
			onlineChannels.put(theDeviceId, newConnection);
		}
		HashOperations<String, String, String> vo = stringRedisTemplate.opsForHash();
		String newVal = newConnection.toString();
		String oldVal = vo.get(GLOBLE_CONNECT_LIST_CACHE_KEY, theDeviceId);
		// newConnection.setConnectDateTime(DateUtils.getCurLongDateStr());

		if (channel != null && !newVal.equals(oldVal)) {
			// 更新数据

			log.info("更新在线状态：{}", theDeviceId);
			// String cacheKey = CACHE_KEY;

			this.forceWriteRedis(vo, theDeviceId, newVal);

		} else {
			log.info("无需更新在线状态：theDeviceId={},channel={},client(oldVal)={}", theDeviceId, channel, oldVal);
		}

		updateOfflineConnection(theDeviceId);
	}

	public boolean distroyConnection(String deviceId) {
		if(deviceId==null){
			return true;
		}
		String ip = IpUtils.getHost();
		Long lasttime = _devicemap.get(ip+SEPRATOR+deviceId);
		if (lasttime != null) {
			long sec = (System.currentTimeMillis() - lasttime);
			if (sec < MIN_SPAN_MSECONDS && ServerList.isLogicServer(ip)) {
				log.info("距离上次连接时间过短，暂不清理连接：{},ip={}", deviceId,ip);
				return false;
			}
		}
		return removeBadConnection(deviceId);
	}

	public boolean removeBadConnection(String deviceId) {

		HashOperations<String, String, String> vo = stringRedisTemplate.opsForHash();
		// HashOperations<String, String, String> vo = stringRedisTemplate.opsForHash();
		String val = vo.get(GLOBLE_CONNECT_LIST_CACHE_KEY, deviceId);
		if (StringUtil.isNullOrEmpty(deviceId)) {
			return false;
		}
		log.info("清理连接前：deviceId={},key={},hash val={}", deviceId, GLOBLE_CONNECT_LIST_CACHE_KEY, val);
		if (onlineChannels.get(deviceId) == null) {
			log.info("已经被清理过：={}", deviceId);
		} else {
			log.info("清理本地连接：={}", deviceId);
		}
		try {
			onlineChannels.remove(deviceId);

			// String cacheKey = CACHE_KEY;
			if (stringRedisTemplate == null) {
				stringRedisTemplate = SpringBeanUtils.getBean(StringRedisTemplate.class);
			}
			if (stringRedisTemplate != null) {

				if (vo != null) {
					vo.delete(GLOBLE_CONNECT_LIST_CACHE_KEY, deviceId);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

}
