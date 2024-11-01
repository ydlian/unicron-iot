package com.me.unicron.iot.properties;

import com.me.unicron.iot.mqtt.MqttHander;
import com.me.unicron.iot.enums.ProtocolEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * netty服务启动参数
 *
 * @author lianyadong
 * @create 2023-11-18 15:08
 **/
@ConfigurationProperties(prefix ="unicron.iot.server")
@Data
public class InitBean {

    public String getPreTestClientIDs() {
		return preTestClientIDs;
	}

	public void setPreTestClientIDs(String preTestClientIDs) {
		this.preTestClientIDs = preTestClientIDs;
	}

	private ProtocolEnum protocolEnum;
    private String ip;//公网IP，充电桩连接时使用
    private int port ;
    private boolean aliveLogicServer ;//标示是否是存活的逻辑服务器
    private String preTestClientIDs ;//走预发布测试的桩站ID白名单
    
    private String serverName ;
    private String envType ;
    private boolean keepalive ;

    
    private boolean reuseaddr ;


    private boolean tcpNodelay ;

    private int backlog ;

    private  int  sndbuf ;

    private int revbuf ;


    private int heart ;

    private boolean ssl ;

    private String jksFile;

    private String jksStorePassword;

    private String jksCertificatePassword;

    private Class<MqttHander> mqttHander ;


    private int  initalDelay ;

    private  int period ;

    private int bossThread;

    private int workThread;

	public ProtocolEnum getProtocolEnum() {
		return protocolEnum;
	}

	public void setProtocolEnum(ProtocolEnum protocolEnum) {
		this.protocolEnum = protocolEnum;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isAliveLogicServer() {
		return aliveLogicServer;
	}

	public void setAliveLogicServer(boolean aliveLogicServer) {
		this.aliveLogicServer = aliveLogicServer;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public boolean isKeepalive() {
		return keepalive;
	}

	public void setKeepalive(boolean keepalive) {
		this.keepalive = keepalive;
	}

	public boolean isReuseaddr() {
		return reuseaddr;
	}

	public void setReuseaddr(boolean reuseaddr) {
		this.reuseaddr = reuseaddr;
	}

	public boolean isTcpNodelay() {
		return tcpNodelay;
	}

	public void setTcpNodelay(boolean tcpNodelay) {
		this.tcpNodelay = tcpNodelay;
	}

	public int getBacklog() {
		return backlog;
	}

	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}

	public int getSndbuf() {
		return sndbuf;
	}

	public void setSndbuf(int sndbuf) {
		this.sndbuf = sndbuf;
	}

	public int getRevbuf() {
		return revbuf;
	}

	public void setRevbuf(int revbuf) {
		this.revbuf = revbuf;
	}

	public int getHeart() {
		return heart;
	}

	public void setHeart(int heart) {
		this.heart = heart;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public String getJksFile() {
		return jksFile;
	}

	public void setJksFile(String jksFile) {
		this.jksFile = jksFile;
	}

	public String getJksStorePassword() {
		return jksStorePassword;
	}

	public void setJksStorePassword(String jksStorePassword) {
		this.jksStorePassword = jksStorePassword;
	}

	public String getJksCertificatePassword() {
		return jksCertificatePassword;
	}

	public void setJksCertificatePassword(String jksCertificatePassword) {
		this.jksCertificatePassword = jksCertificatePassword;
	}

	public Class<MqttHander> getMqttHander() {
		return mqttHander;
	}

	public void setMqttHander(Class<MqttHander> mqttHander) {
		this.mqttHander = mqttHander;
	}

	public int getInitalDelay() {
		return initalDelay;
	}

	public void setInitalDelay(int initalDelay) {
		this.initalDelay = initalDelay;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public int getBossThread() {
		return bossThread;
	}

	public void setBossThread(int bossThread) {
		this.bossThread = bossThread;
	}

	public int getWorkThread() {
		return workThread;
	}

	public void setWorkThread(int workThread) {
		this.workThread = workThread;
	}
    
    

}
