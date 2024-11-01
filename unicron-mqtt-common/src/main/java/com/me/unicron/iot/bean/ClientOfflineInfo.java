package com.me.unicron.iot.bean;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.math3.analysis.function.Abs;

import com.alibaba.fastjson.annotation.JSONField;
import com.esotericsoftware.minlog.Log;
import com.me.unicron.date.DateUtils;

import ch.qos.logback.core.net.AbstractSSLSocketAppender;
import lombok.Data;
import lombok.ToString;

@Data
public class ClientOfflineInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6840595808048689417L;
	private String connectionStatus;
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Date connectDateTime;
	private String ipPort;
	private String ip;
	private String deviceID;
	private String localServerIp;
	private String userName;
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Date lastLiveTime;
	private String sendStatus;
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Date offlineTime;
	private String offlineLongSendStatus = "";
	private String station = "";

	@JSONField(serialize = false)
	public String getWarnMessage() {
		String meString = null;
		try {
			meString = String.format("充电桩离线:{充电站:%s,充电桩编号:%s,状态:%s,连接时间:%s,最后活跃时间%s,IP地址:%s,逻辑服务器地址:%s,厂家编码:%s}",
					this.getStation(), this.getDeviceID(), this.getConnectionStatus(),
					DateUtils.DateToString(this.getConnectDateTime(), "yyyy-MM-dd HH:mm:ss"),
					DateUtils.DateToString(this.getLastLiveTime(), "yyyy-MM-dd HH:mm:ss"), this.getIpPort(),
					this.getLocalServerIp(), this.getUserName());

		} catch (Exception e) {
			// TODO: handle exception
			Log.info(e.getMessage());
		}
		return meString;
	}

	@JSONField(serialize = false)
	public String getRecoverMessage() {
		String meString = null;
		try {			
			long secondTime = Math.abs(DateUtils.getIntervalSecond(this.getOfflineTime(), new Date()));
			long hour = secondTime / 3600;
			long min = (secondTime - hour * 3600) / 60;
			long sec = secondTime % 60;

			meString = String.format(
					"充电桩恢复在线:{充电站:%s,充电桩编号:%s,恢复连接时间:%s,离线时间:%s,离线时长:%d时%d分%d秒,IP地址:%s,逻辑服务器地址:%s,厂家编码:%s}",
					this.getStation(), this.getDeviceID(),
					DateUtils.DateToString(this.getConnectDateTime(), "yyyy-MM-dd HH:mm:ss"),
					DateUtils.DateToString(this.getOfflineTime(), "yyyy-MM-dd HH:mm:ss"), hour, min, sec,this.getIpPort(),
					this.getLocalServerIp(), this.getUserName());

		} catch (Exception e) {
			// TODO: handle exception
			Log.info(e.getMessage());
		}
		return meString;
	}
}
