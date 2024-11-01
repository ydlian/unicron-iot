package com.me.unicron.iot.bootstrap.channel;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.mysql.jdbc.Buffer;
import com.orange.comp.commonapi.gift.entity.UploadResponse;
import com.me.epower.direct.entity.chargecloud.logBmsRequestParam;
import com.me.epower.entity.OperatorDevice;
import com.me.epower.entity.UpLoadLogGiftInfo;
import com.me.epower.repositories.OperatorDeviceRepository;
import com.me.epower.repositories.UpLoadLogGiftInfoRepository;
import com.me.unicron.date.DateUtils;
import com.me.unicron.iot.bean.ClientConnectionInfo;
import com.me.unicron.iot.mqtt.ClientConnectionService;

import lombok.extern.slf4j.Slf4j;
import springfox.documentation.spring.web.json.Json;

@Service
@Slf4j
public class 	LogSaveService {

	@Autowired
	UpLoadLogGiftInfoRepository upLoadLogGiftInfoRepository;
	
	@Autowired
	OperatorDeviceRepository operatorDeviceRepository;

	private final static String zhongHengUserName = "913300002539163407";
	private final static String zhiChongUserName = "911101083397675346";

	public boolean saveLogGiftAddr(String deviceid, UploadResponse uploadResponse) {
		if (StringUtils.isBlank(deviceid) || uploadResponse == null) {
			return false;
		}
		List<OperatorDevice> list = operatorDeviceRepository.findOperatorDeviceByDeviceid(deviceid);
		if (list == null || list.isEmpty()) {
			log.info("logUpload can not find device message,deviceid:{}",deviceid);
			return false;
		}
		UpLoadLogGiftInfo upLoadLogGiftInfo = new UpLoadLogGiftInfo();
		upLoadLogGiftInfo.setDeviceid(deviceid);
		upLoadLogGiftInfo.setGiftaddr(uploadResponse.getDownload_url_https());
		upLoadLogGiftInfo.setFilename(uploadResponse.getResource_key());
		upLoadLogGiftInfo.setUptime(DateUtils.getCurDateTime());
		upLoadLogGiftInfoRepository.save(upLoadLogGiftInfo);
		log.info("save log up message,upLoadLogGiftInfo:{} ",JSON.toJSONString(upLoadLogGiftInfo));
		return true;
	}

	private void zhiChongLogGetDateInfo(UpLoadLogGiftInfo upLoadLogGiftInfo, BufferedReader bufferedReader)
			throws IOException {
		// [2023-09-23-10:09:14][C1:5-C2:5][net:4G][tem:59][cc1:4-cc2:4][asu:28]TcpService---rece
		// cmd:301
		String data = bufferedReader.readLine();// 读取数据
		String regex = "\\d{4}[-]\\d{2}[-]\\d{2}[-]\\d{2}:\\d{2}:\\d{2}";
		Pattern p = Pattern.compile(regex);

		String startTime = null;
		String endTime = null;
		while (data != null) {// 循环读取数据
			Matcher matcher = p.matcher(data);
			if (matcher.find()) {
				if (StringUtils.isBlank(startTime)) {
					startTime = matcher.group(0);
				}
				endTime = matcher.group(0);
			}
			data = bufferedReader.readLine();
		}
		if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
			log.info("get zhiChong log message,starttime:{},endtime:{}", startTime, endTime);
			Date start = DateUtils.StringToDate(startTime, "yyyy-MM-dd-HH:mm:ss");
			Date enDate = DateUtils.StringToDate(endTime, "yyyy-MM-dd-HH:mm:ss");
		}

	}

	private void zhongHengLogGetDateInfo(UpLoadLogGiftInfo upLoadLogGiftInfo, BufferedReader bufferedReader)
			throws IOException {
		// 2023/09/26 07:06:20 枪2无卡或APP启动
		String data = bufferedReader.readLine();// 读取数据
		String regex = "\\d{4}[/]\\d{2}[/]\\d{2}\\s*\\d{2}:\\d{2}:\\d{2}";
		Pattern p = Pattern.compile(regex);

		String startTime = null;
		String endTime = null;
		while (data != null) {// 循环读取数据
			Matcher matcher = p.matcher(data);
			if (matcher.find()) {
				if (StringUtils.isBlank(startTime)) {
					startTime = matcher.group(0);
				}
				endTime = matcher.group(0);
			}
			data = bufferedReader.readLine();
		}
		if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
			log.info("get zhongHeng log message,starttime:{},endtime:{}", startTime, endTime);
			Date start = DateUtils.StringToDate(startTime, "yyyy/MM/dd HH:mm:ss");
			Date enDate = DateUtils.StringToDate(endTime, "yyyy/MM/dd HH:mm:ss");
		}

	}

}
