package com.me.unicron.iot.bootstrap.channel.connection;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.me.epower.direct.entity.chargecloud.HttpUpdateParam;
import com.me.epower.direct.entity.chargecloud.SoftUpdteInfo;
import com.me.epower.direct.entity.chargecloud.logBmsRequestParam;
import com.me.epower.direct.entity.chargecloud.monitor.SignupRecord;
import com.me.epower.direct.entity.constant.RedisConstant;
import com.me.epower.direct.entity.upward.SignupResponse_106;
import com.me.epower.direct.enums.ConnectorStatusEnum;
import com.me.epower.direct.repositories.chargecloud.monitor.SignupRecordRepository;
import com.me.unicron.common.server.ServerList;
import com.me.unicron.iot.gateway.service.InitService;
import com.me.unicron.iot.ip.IpUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StakeSoftUpdateTask {

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	private final String UPDATE_KEY = RedisConstant.UNICRON_STAKE_UPDATE_KEY;
	private final static String ONLINE_CACHE_KEY = RedisConstant.UNICRON_GLOBLE_CONNECT_LIST;
	private final static int softupdateRetryOutTimes = 3;
	private final String Md5String = "8b25841cdb7b4a2d";

	@Autowired
	private InitService initService;

	@Autowired
	SignupRecordRepository signupRecordRepository;

	@Scheduled(cron = "0 0/5 * * * ?")
	public void updateStakeSoftStatus() {

		String ip = IpUtils.getHost();
		boolean ok = ServerList.logicServer2.equals(ip);
		log.info("本机地址={},目标节点={},是否执行充电桩日志更新任务={}", ip, ServerList.logicServer2, ok);
		if (!ok)
			return;

		HashOperations<String, String, String> vo = stringRedisTemplate.opsForHash();
		Map<String, String> glbConnMap = vo.entries(UPDATE_KEY);
		Set<String> keySet = glbConnMap.keySet();
		for (String equipment : keySet) {

			String dataString = glbConnMap.get(equipment);
			SoftUpdteInfo softUpdteInfo = JSON.parseObject(dataString, SoftUpdteInfo.class);
			if (softUpdteInfo == null) {
				continue;
			}
			log.info("equipment:{} soft update message,message:{}", equipment, dataString);

			if (softUpdteInfo.getRetrytimes() > softupdateRetryOutTimes) {
				log.info("equipment:{} retry update soft out times,quit update", equipment);
				vo.delete(UPDATE_KEY, equipment);
				continue;
			}

			String softVersion = softUpdteInfo.getOriginalsoft();
			if (StringUtils.isBlank(softVersion) && (softUpdteInfo.getRetrytimes() > 0)) {
				log.info("equipment:{} cannot find softversion and update for one time,now quit update,message:{}",
						equipment, dataString);
				vo.delete(UPDATE_KEY, equipment);
				continue;
			}

			boolean onlineFlag = stringRedisTemplate.opsForHash().hasKey(ONLINE_CACHE_KEY, equipment);
			if (!onlineFlag) {
				log.info("equipment:{} offline,stop upate soft", equipment);
				continue;
			}

			List<SignupRecord> signup_temp = signupRecordRepository.findByEquipment_idByMaxId(equipment);
			SignupResponse_106 signupObj = new SignupResponse_106();
			if (signup_temp != null && signup_temp.size() > 0) {
				SignupRecord s = signup_temp.get(0);
				String data = s.getMsg();
				if (data != null) {
					try {
						signupObj = JSON.parseObject(data, SignupResponse_106.class);
					} catch (Exception e) {

					}
				}

				if (!softVersion.equals(signupObj.getStation_ver())) {
					log.info("equipment:{} already update success,current softversion:{}", equipment,
							signupObj.getStation_ver());
					vo.delete(UPDATE_KEY, equipment);
					continue;
				}
			}

			String connector1Id = equipment + "01";
			String connector2Id = equipment + "02";
			String connector1_status = stringRedisTemplate.opsForValue()
					.get(RedisConstant.UNICRON_CONNECTTOR_STATUS + connector1Id);
			String connector2_status = stringRedisTemplate.opsForValue()
					.get(RedisConstant.UNICRON_CONNECTTOR_STATUS + connector2Id);

			if (StringUtils.isBlank(connector1_status) || StringUtils.isBlank(connector2_status)) {

				log.info("equipment:{} cannot find all guns status,connector1_status.status:{},connector2_status.status:{}",
						equipment, StringUtils.isBlank(connector1_status), StringUtils.isBlank(connector2_status));

				continue;
			}

			if (!(ConnectorStatusEnum.IDLE.getCode().equals(connector1_status)
					|| ConnectorStatusEnum.UNCHARGE.getCode().equals(connector1_status))) {
				log.info("equipment:{} not in idle status,connectorId:{}", equipment, connector1Id);
				continue;
			}

			if (!(ConnectorStatusEnum.IDLE.getCode().equals(connector2_status)
					|| ConnectorStatusEnum.UNCHARGE.getCode().equals(connector2_status))) {
				log.info("equipment:{} not in idle status,connectorId:{}", equipment, connector2Id);
				continue;
			}
				
			log.info("equipment:{} in idle status,begin to update soft", equipment);
			HttpUpdateParam httpUpdateParam = new HttpUpdateParam();
			httpUpdateParam.setBaseEquipmentId(equipment);
			httpUpdateParam.setEquipmentId(equipment);
			httpUpdateParam.setMd5(Md5String);
			httpUpdateParam.setOperatorId("2");
			httpUpdateParam.setUrl(softUpdteInfo.getGifturl());
			initService.httpUpdate(httpUpdateParam);

			softUpdteInfo.setRetrytimes(softUpdteInfo.getRetrytimes() + 1);
			String updateString = JSON.toJSONString(softUpdteInfo, SerializerFeature.IgnoreErrorGetter);
			stringRedisTemplate.opsForHash().put(UPDATE_KEY, equipment, updateString);
			log.info("equipment:{} send soft update message,message:{}", equipment, updateString);
		}
	}
}
