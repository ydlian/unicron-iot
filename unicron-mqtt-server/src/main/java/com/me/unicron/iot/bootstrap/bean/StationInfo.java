package com.me.unicron.iot.bootstrap.bean;

import com.me.epower.direct.entity.upward.SignupResponse_106;
import com.me.unicron.iot.bootstrap.ChannelService;
import com.me.unicron.iot.exception.LogicServerException;

public class StationInfo {
	public static final int MAX_TIMEOUT_MSEC=1000*60*10;
	private SignupResponse_106 signupInfo;
	private boolean stationOnlineStatus=true;//默认在线
	private String stationEquipId;
	private ChannelService channelService;
	private long lastAliveSignalTime;//最近一次接收存活信号的时间：签到／心跳
	
	public long getLastAliveSignalTime() {
		return lastAliveSignalTime;
	}
	public void setLastAliveSignalTime(long lastAliveSignalTime) {
		this.lastAliveSignalTime = lastAliveSignalTime;
	}
	public ChannelService getChannelService() {
		return channelService;
	}
	public void setChannelService(ChannelService channelService) {
		this.channelService = channelService;
	}
	public SignupResponse_106 getSignupInfo() {
		return signupInfo;
	}
	public void setSignupInfo(SignupResponse_106 signupInfo) {
		this.signupInfo = signupInfo;
	}
	
	public boolean isStationOnlineStatus() {
		//增加时间判断
		if(stationOnlineStatus == true){
			long curtime=System.currentTimeMillis();
			if(curtime-this.getLastAliveSignalTime()>MAX_TIMEOUT_MSEC){
				//上次存活时间距离当前时间超时，长时间无心跳设置offline
				this.stationOnlineStatus = false;
			}
			if(curtime-this.getLastAliveSignalTime()<0)
			{
				throw new LogicServerException("逻辑服务器时钟错误！");
			}
		}
		return this.stationOnlineStatus;
	}
	public void setStationOnlineStatus(boolean stationOnlineStatus) {
		this.stationOnlineStatus = stationOnlineStatus;
	}
	public String getStationEquipId() {
		return stationEquipId;
	}
	public void setStationEquipId(String stationEquipId) {
		this.stationEquipId = stationEquipId;
	}
	
}
