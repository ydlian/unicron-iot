package com.me.unicron.iot.bootstrap.channel.security.impl;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.me.epower.component.ChargeConstant;
import com.me.epower.direct.entity.downward.AuthRequest_801;
import com.me.unicron.EncodeUtil;
import com.me.unicron.RSACoder;
import com.me.unicron.common.server.ServerList;
import com.me.unicron.iot.bean.ClientConnectionInfo;
import com.me.unicron.iot.bootstrap.ChannelService;
import com.me.unicron.iot.bootstrap.channel.StationManagementService;
import com.me.unicron.iot.bootstrap.channel.security.ChannelSecurityService;
import com.me.unicron.iot.bootstrap.channel.security.SecurityCheckService;
import com.me.unicron.iot.gateway.service.util.PubChannelUtil;
import com.me.unicron.iot.ip.IpUtils;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.impl.CMD_801;
import com.me.unicron.iot.mqtt.ClientConnectionService;
import com.me.unicron.iot.serverQos.CurrentQos;
import com.me.unicron.protocol.CharsetDef;
import com.me.unicron.station.service.ISecurityService;

import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SecurityCheckServiceImpl implements SecurityCheckService {
	@Autowired
	private ChannelSecurityService channelSecurityService;
	@Autowired
	private StationManagementService stationManagementService;
	@Autowired
	ISecurityService iSecurityService;
	
	@Scheduled(cron = "0 0/30 * * * ?")
	public void checkThisNodeConnectionSecurity() {
		ConcurrentHashMap<String, ClientConnectionInfo> allChannels = ClientConnectionService.getOnlineChannels();
		if (allChannels != null) {
			for (String equipmentId : allChannels.keySet()) {
				ClientConnectionInfo client = allChannels.get(equipmentId);
				boolean securityCheckResult=iSecurityService.isSecurytyCheckPass(equipmentId);
				if(!securityCheckResult){
					log.info("[FAIL]checkThisNodeConnectionSecurity:securityCheckResult={}||equipmentId={}",securityCheckResult,equipmentId);
				}else{
					log.info("[PASS]checkThisNodeConnectionSecurity:securityCheckResult={}||equipmentId={}",securityCheckResult,equipmentId);
				}
				
				String username = client.getUserName();
				if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(client.getClientPublicKey())) {
					String data = equipmentId + ChargeConstant.AUTH_CONSTANT;
					try {
						String encodeData = RSACoder.encodeByPublicKey(data, client.getClientPublicKey());
						AuthRequest_801 authRequest_801 = new AuthRequest_801();
						authRequest_801.setEncodeData(encodeData);
						byte[] charsequenceToBytes = EncodeUtil.charsequenceToByte(encodeData);
						authRequest_801.setEncodeDataLength(String.valueOf(charsequenceToBytes.length));
						authRequest_801.setEquipmentId(equipmentId);
						authRequest_801.setEncodeType("1");
						ChannelService channelService = PubChannelUtil.getChannelService(equipmentId,
								stationManagementService);
						if (channelService != null) {
							CMD_801 cmd_801 = new CMD_801();
							MqttNetMsg base = new MqttNetMsg();
							base.setResponse(false);
							byte[] byte801 = cmd_801.getPayload(authRequest_801, base);
							int index=EncodeUtil.byteToInt(base.getIndex());
							
							log.info("checkThisNodeConnectionSecurity:equipmentId={}||cmd=801||index={}",equipmentId,
									index);
							if(byte801!=null){
								String msg = new String(byte801, CharsetDef.CHARSET);
								byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
								log.info("发送801认证命令={}",EncodeUtil.printHex(byte801));
								
								channelService.push(authRequest_801.getEquipmentId(), CurrentQos.QoS, sendbytes);
								iSecurityService.updateCheckStatus(equipmentId, ""+index, "0");
							}else{
								log.info("发送801认证命令失败！");
							}
							
						}
					} catch (UnsupportedEncodingException e) {
						log.error("authRequest error", e);
						e.printStackTrace();

					}

				} else {
					log.info("checkThisNodeConnectionSecurity:username is null||equipmentId={}||clientPublicKey={}", equipmentId,client.getClientPublicKey());
				}

			}

		}

	}

}
