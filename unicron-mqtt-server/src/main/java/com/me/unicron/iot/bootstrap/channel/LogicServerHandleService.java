package com.me.unicron.iot.bootstrap.channel;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.me.epower.direct.entity.ServerInfo;
import com.me.epower.direct.entity.chargecloud.monitor.ConnectCenterRecord;
import com.me.epower.direct.entity.chargecloud.monitor.HeartBeatRecord;
import com.me.epower.direct.entity.downward.BMSRequest_302;
import com.me.epower.direct.entity.downward.StationQueryLogicInfoResponse_113;
import com.me.epower.direct.entity.upward.AuthResponse_802;
import com.me.epower.direct.entity.upward.BeatHeartResponse_102;
import com.me.epower.direct.entity.upward.CleanResponse_20;
import com.me.epower.direct.entity.upward.ControlCommandResponse_6;
import com.me.epower.direct.entity.upward.DelayFee_1308;
import com.me.epower.direct.entity.upward.FileVersion_1104;
import com.me.epower.direct.entity.upward.HistoryOrderInfoResponse_402;
import com.me.epower.direct.entity.upward.HttpUpdate_1102;
import com.me.epower.direct.entity.upward.IntParaSetResponse_2;
import com.me.epower.direct.entity.upward.LockResponse_24;
import com.me.epower.direct.entity.upward.LogReportResponse_408;
import com.me.epower.direct.entity.upward.OrderInfoResponse_202;
import com.me.epower.direct.entity.upward.ServiceFee_1306;
import com.me.epower.direct.entity.upward.SignupResponse_106;
import com.me.epower.direct.entity.upward.StartChargeResponse_8;
import com.me.epower.direct.entity.upward.StationChargeUnitQueryResponse_14;
import com.me.epower.direct.entity.upward.StationEventReport_108;
import com.me.epower.direct.entity.upward.StationFaultReport_118;
import com.me.epower.direct.entity.upward.StationFaultReport_118_ext;
import com.me.epower.direct.entity.upward.StationLocalLogQueryResponse_112;
import com.me.epower.direct.entity.upward.StationPowerDispatchResult_16;
import com.me.epower.direct.entity.upward.StationPowerStategyDispatchQueryResponse_18;
import com.me.epower.direct.entity.upward.StationStatInfoQueryResponse_110;
import com.me.epower.direct.entity.upward.StationStatInfoResponse_104;
import com.me.epower.direct.entity.upward.StopChargeCommandResponse_12;
import com.me.epower.direct.entity.upward.StringhParaSetResponse_4;
import com.me.epower.direct.entity.upward.UpdateProcess_1106;
import com.me.epower.direct.repositories.chargecloud.monitor.ConnectCenterRecordRepository;
import com.me.epower.direct.repositories.chargecloud.monitor.SignupRecordRepository;
import com.me.unicron.EncodeUtil;
import com.me.unicron.common.server.ServerList;
import com.me.unicron.date.DateUtils;
import com.me.unicron.iot.bootstrap.ChannelService;
import com.me.unicron.iot.bootstrap.bean.StationInfo;
import com.me.unicron.iot.bootstrap.coder.server.ServerDecoder;
import com.me.unicron.iot.centerserver.ServerPoolService;
import com.me.unicron.iot.gateway.service.ResponseHandleService;
import com.me.unicron.iot.ip.IpUtils;
import com.me.unicron.iot.logicserver.LogicServerQos;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.impl.CMD_113;
import com.me.unicron.iot.message.parser.impl.CMD_parser_102;
import com.me.unicron.iot.message.parser.impl.CMD_parser_104;
import com.me.unicron.iot.message.parser.impl.CMD_parser_106;
import com.me.unicron.iot.message.parser.impl.CMD_parser_108;
import com.me.unicron.iot.message.parser.impl.CMD_parser_110;
import com.me.unicron.iot.message.parser.impl.CMD_parser_1102;
import com.me.unicron.iot.message.parser.impl.CMD_parser_1104;
import com.me.unicron.iot.message.parser.impl.CMD_parser_1106;
import com.me.unicron.iot.message.parser.impl.CMD_parser_112;
import com.me.unicron.iot.message.parser.impl.CMD_parser_116;
import com.me.unicron.iot.message.parser.impl.CMD_parser_118;
import com.me.unicron.iot.message.parser.impl.CMD_parser_12;
import com.me.unicron.iot.message.parser.impl.CMD_parser_1302;
import com.me.unicron.iot.message.parser.impl.CMD_parser_1304;
import com.me.unicron.iot.message.parser.impl.CMD_parser_1306;
import com.me.unicron.iot.message.parser.impl.CMD_parser_1308;
import com.me.unicron.iot.message.parser.impl.CMD_parser_14;
import com.me.unicron.iot.message.parser.impl.CMD_parser_16;
import com.me.unicron.iot.message.parser.impl.CMD_parser_18;
import com.me.unicron.iot.message.parser.impl.CMD_parser_2;
import com.me.unicron.iot.message.parser.impl.CMD_parser_20;
import com.me.unicron.iot.message.parser.impl.CMD_parser_202;
import com.me.unicron.iot.message.parser.impl.CMD_parser_24;
import com.me.unicron.iot.message.parser.impl.CMD_parser_302;
import com.me.unicron.iot.message.parser.impl.CMD_parser_4;
import com.me.unicron.iot.message.parser.impl.CMD_parser_402;
import com.me.unicron.iot.message.parser.impl.CMD_parser_408;
import com.me.unicron.iot.message.parser.impl.CMD_parser_410;
import com.me.unicron.iot.message.parser.impl.CMD_parser_6;
import com.me.unicron.iot.message.parser.impl.CMD_parser_8;
import com.me.unicron.iot.message.parser.impl.CMD_parser_802;
import com.me.unicron.iot.mqtt.ClientConnectionService;
import com.me.unicron.iot.mqtt.ServerMqttHandlerService;
import com.me.unicron.iot.util.ServerDecoderUtil;
import com.me.unicron.protocol.CharsetDef;
import com.me.unicron.station.service.IStationService;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

/**
 * LogicServer事件处理service
 *
 * @author lianyadong
 * @create 2023-11-21 13:59
 **/
@Slf4j
@Component
public class LogicServerHandleService {
	@Autowired
	ConnectCenterRecordRepository connectCenterRecordRepository;
	// @Autowired
	// HeartBeatRecordRepository heartBeatRecordRepository;
	@Autowired
	IStationService iStationService;
	@Autowired
	SignupRecordRepository signupRecordRepository;

	@Autowired
	private LogicServerQos logicServerQos;

	@Autowired
	private ServerDecoder serverDecoder;

	// @Autowired
	// private ServerDecoderV0 serverDecoderV0;

	@Autowired
	ServerPoolService serverPoolService;

	@Autowired
	private StationManagementService stationManagementService;

	@Autowired
	private ResponseHandleService responseHandleService;

	@Autowired
	ClientConnectionService clientConnectionService;

	public boolean saveHeartbeat(Channel channel, String equipmentId, String msg) {
		HeartBeatRecord heartBeatRecord = new HeartBeatRecord();
		heartBeatRecord.setBeat_time(DateUtils.getCurDateTime());
		heartBeatRecord.setEquipment_id(equipmentId);
		heartBeatRecord.setMsg(msg);
		heartBeatRecord.setNodeserver(IpUtils.getHost());
		heartBeatRecord.setClientip(channel.remoteAddress().toString());
		// heartBeatRecordRepository.save(heartBeatRecord);
		iStationService.saveHeartBeatData(heartBeatRecord);
		return true;

		// List<HeartBeatRecord>
		// list=heartBeatRecordRepository.findByEquipment_idByMaxID(equipmentId);
		// if(list!=null && list.size()>0){
		// Date lastTime=list.get(0).getBeat_time();
		// Date nowTime=DateUtils.getCurDateTime();
		// long interval=DateUtils.getIntervalSecond(lastTime, nowTime);
		// if(Math.abs(interval)>30){
		// heartBeatRecordRepository.save(heartBeatRecord);
		// return true;
		// }
		//
		// }
		// return false;

	}

	public void updateChannel(ChannelService channelService, String equipmentId) {
		if (channelService == null || StringUtils.isBlank(equipmentId)) {
			return;
		}
		StationInfo station = new StationInfo();
		List<StationInfo> stationList = stationManagementService.getStation(equipmentId);
		if (stationList != null && stationList.size() > 0) {
			station = stationList.get(0);
		} else {
			// 存储的时候需要去掉填充0
			equipmentId = equipmentId.trim();
			station.setStationEquipId(equipmentId);

		}
		station.setChannelService(channelService);
		station.setLastAliveSignalTime(System.currentTimeMillis());
		station.setStationOnlineStatus(true);
		// ChannelService channelService =
		// PubChannelUtil.getChannelService(equipmentId,
		// stationManagementService);
		stationManagementService.addStation(station.getStationEquipId(), station);
	}

	private void response114CMD(ChannelService channelService, ChannelHandlerContext channelHandlerContext,
			MqttNetMsgBase base, String stationEquipId) {
		
		// 113号回应请求
		StationQueryLogicInfoResponse_113 query = new StationQueryLogicInfoResponse_113();
		String thisServerIp = IpUtils.getHost();
		log.info("服务器{}收到充电桩索取逻辑服务器命令={}", thisServerIp, ServerDecoder.CMD_114);
		ServerInfo server = serverPoolService.getUseableLogicServer(stationEquipId);
		//中心服务器
		query.setIp(server.getIp());
		query.setPort(server.getPort() + "");
		if (ServerList.isLogicServer(thisServerIp)) {

			//逻辑服务器收到114，不处理，忽略
			return;
		} else {

		}

		String centerResp = new CMD_113().pack(query, base);
		// log.debug("centerResp 长度:" + centerResp.length() + "," +
		// server.getIp() + "," + server.getPort());
		try {
			// 有坑,谨慎使用字符编码！！！！
			byte[] sendbytes = centerResp.getBytes(CharsetDef.NETTY_CHARSET_UTF8);
			log.info("回复逻辑服务器地址：{}", JSONObject.toJSON(server));
			EncodeUtil.print(new CMD_113().getPayload(query, base));
			EncodeUtil.printHex(new CMD_113().getPayload(query, base));
			ConnectCenterRecord connectCenterRecord = new ConnectCenterRecord();
			connectCenterRecord.setConnect_time(DateUtils.getCurDateTime());
			connectCenterRecord.setEquipment_id(stationEquipId);
			connectCenterRecord.setMsg(centerResp);
			connectCenterRecord
					.setLogic_server(server.getIp() + ":" + server.getPort() + ",thisNode=" + IpUtils.getHost());
			connectCenterRecordRepository.save(connectCenterRecord);
			// 回复逻辑服务器地址
			channelService.push(stationEquipId, logicServerQos.getQos(), sendbytes);

			log.debug("中心服务器回复逻辑服务器地址,并关闭桩和中心服务器的连接：");
			Thread.sleep(6 * 1000);
			clientConnectionService.distroyConnection(stationEquipId);
			clientConnectionService.printConnectionInfo();
			// 如果是中心服务器，回复充电桩逻辑服务器地址后，需要关闭channel
			channelHandlerContext.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clientConnectionService.removeBadConnection(stationEquipId);

	}

	public void procPublishResponse(ChannelService channelService, byte[] dataByte,
			ChannelHandlerContext channelHandlerContext, String stationEquipId,
			ServerMqttHandlerService serverMqttHandlerService) {

		int cmd_no = serverDecoder.getCmdNo(dataByte);
		byte[] headStart = ServerDecoderUtil.getHeadStartCode(dataByte);
		byte[] headVersion = ServerDecoderUtil.getHeadVersion(dataByte);
		byte[] headIndex = ServerDecoderUtil.getHeadIndex(dataByte);
		// 区分厂家
		String headStartStr = ServerDecoderUtil.getHeadStartCodeString(dataByte);
		int index = ServerDecoderUtil.getClientkDataIndex(dataByte);
		String version = ServerDecoderUtil.getClientkProtocolVersion(dataByte);
		log.info(
				"dltag=_com_unicron_logic_server||traceid={}||[procPublish]命令编码={}||version={}||index={}||headStart={}",
				stationEquipId, cmd_no, version, index, headStartStr);
		log.info("消息解码:traceid={}||[procPublish]命令编码={}||dataBytes={}", stationEquipId, cmd_no,
				EncodeUtil.printHex(dataByte));
		byte[] receiveDataBody = serverDecoder.getBodyData(dataByte);
		if (receiveDataBody == null) {
			log.info("***************receiveDataBody is null!***************");
			return;
		}

		MqttNetMsgBase base = new MqttNetMsg();
		base.setResponse(true);
		base.setHeadCopyStart(headStart);
		base.setHeadCopyVersion(headVersion);
		base.setHeadCopyIndex(headIndex);

		Channel channel = channelHandlerContext.channel();
		// 有数据交互，更新一下
		String thisServerIp = IpUtils.getHost();
		if (ServerList.isLogicServer(thisServerIp)) {
			saveHeartbeat(channel, stationEquipId, "PUBLISH:ENH:" + cmd_no);
			clientConnectionService.updateConnection(channel, stationEquipId);
		}

		switch (cmd_no) {
		case ServerDecoder.CMD_114:// ?
			response114CMD(channelService, channelHandlerContext, base, stationEquipId);
			break;

		case ServerDecoder.CMD_2:

			CMD_parser_2 cmd_2 = new CMD_parser_2();
			IntParaSetResponse_2 resp_2 = cmd_2.unpack(receiveDataBody);
			responseHandleService.handleIntParaSet(resp_2, base);
			break;
		case ServerDecoder.CMD_4:
			CMD_parser_4 cmd_4 = new CMD_parser_4();
			StringhParaSetResponse_4 resp_4 = cmd_4.unpack(receiveDataBody);
			responseHandleService.handleStringParaSet(resp_4, base);
			break;
		case ServerDecoder.CMD_6:
			CMD_parser_6 cmd_6 = new CMD_parser_6();
			ControlCommandResponse_6 resp_6 = cmd_6.unpack(receiveDataBody);
			log.info("_dltag=[procPublishResponse]||后台控制命令执行异步通知：{},结果={}", resp_6.getStationId(),
					JSON.toJSONString(resp_6));
			responseHandleService.handleControlCmd(resp_6, base);
			break;
		case ServerDecoder.CMD_8:
			CMD_parser_8 cmd_8 = new CMD_parser_8();
			StartChargeResponse_8 resp_8 = cmd_8.unpack(headStartStr, version, receiveDataBody);

			log.info("_dltag=[procPublishResponse]||启动充电异步通知：{},启动充电结果={}", resp_8.getEquipmentId(),
					JSON.toJSONString(resp_8));
			responseHandleService.handleStartChargeResponse(resp_8, base);
			break;
		case ServerDecoder.CMD_12:
			CMD_parser_12 cmd_12 = new CMD_parser_12();
			StopChargeCommandResponse_12 resp_12 = cmd_12.unpack(receiveDataBody);
			log.info("_dltag=[procPublishResponse]||停止充电执行异步通知：{},结果={}", resp_12.getEquipmentId(),
					JSON.toJSONString(resp_12));
			responseHandleService.handleStopChargeBySeqCmd(resp_12, base);
			break;
		case ServerDecoder.CMD_14:
			CMD_parser_14 cmd_14 = new CMD_parser_14();
			StationChargeUnitQueryResponse_14 resp_14 = cmd_14.unpack(receiveDataBody);
			responseHandleService.handleLittleSowerStrategyDispatchCmd(resp_14, base);
			break;

		case ServerDecoder.CMD_16:
			CMD_parser_16 cmd_16 = new CMD_parser_16();
			StationPowerDispatchResult_16 stationPowerDispatchResult_16 = cmd_16.unpack(receiveDataBody);
			responseHandleService.handleSowerStrategyDispatchCmd(stationPowerDispatchResult_16, base);
			break;

		case ServerDecoder.CMD_18:
			CMD_parser_18 cmd_18 = new CMD_parser_18();
			StationPowerStategyDispatchQueryResponse_18 stategyDispatchQueryResponse_18 = cmd_18
					.unpack(receiveDataBody);
			responseHandleService.handleASKSowerStrategyDispatchCmd(stategyDispatchQueryResponse_18, base);
			break;

		case ServerDecoder.CMD_20:
			CMD_parser_20 cmd_20 = new CMD_parser_20();
			CleanResponse_20 cleanResponse_20 = cmd_20.unpack(receiveDataBody);
			responseHandleService.handleCleanResponse(cleanResponse_20, base);
			break;
		case ServerDecoder.CMD_22:
			CMD_parser_20 cmd_22 = new CMD_parser_20();
			CleanResponse_20 cleanResponse_22 = cmd_22.unpack(receiveDataBody);
			responseHandleService.handleCleanResponse(cleanResponse_22, base);
			break;
		case ServerDecoder.CMD_24:
			CMD_parser_24 cmd_24 = new CMD_parser_24();
			LockResponse_24 cleanResponse_24 = cmd_24.unpack(receiveDataBody);
			responseHandleService.handleLockResponse(cleanResponse_24, base);
			break;

		case ServerDecoder.CMD_102:
			// (CODE=102)充电桩上传心跳包信息
			CMD_parser_102 cmd_102 = new CMD_parser_102();
			BeatHeartResponse_102 resp_102 = cmd_102.unpack(receiveDataBody);

			responseHandleService.handleBeatHeartResponse(channel, resp_102, base);
			break;
		case ServerDecoder.CMD_104:
			// (CODE=104)充电桩状态信息包上报
			CMD_parser_104 cmd_104 = new CMD_parser_104();
			StationStatInfoResponse_104 resp_104 = cmd_104.unpack(version, receiveDataBody);
			log.debug("服务器接收104命令报文：{}", JSONObject.toJSON(resp_104));
			// 服务器回复104消息
			responseHandleService.handleChargeStatusUpload(resp_104, base);
			break;

		case ServerDecoder.CMD_106:// 2023.03.08
			// (CODE=106)充电桩签到信息上报
			log.debug("充电桩签到");
			CMD_parser_106 cmd_106 = new CMD_parser_106();
			SignupResponse_106 resp = cmd_106.unpack(receiveDataBody);

			StationInfo station = new StationInfo();
			station.setChannelService(channelService);
			String equipmentId = resp.getStationId();
			// 存储的时候需要去掉填充0
			equipmentId = EncodeUtil.cutCharsequence(equipmentId);
			resp.setStationId(equipmentId);
			station.setSignupInfo(resp);
			station.setStationEquipId(equipmentId);
			station.setLastAliveSignalTime(System.currentTimeMillis());
			station.setStationOnlineStatus(true);
			log.debug("ServerDecoder.CMD_106 存储,setStationEquipId={},channelService={}", station.getStationEquipId(),
					JSONObject.toJSON(station));
			stationManagementService.addStation(station.getStationEquipId(), station);
			responseHandleService.handleSignup(resp, base);
			break;
		case ServerDecoder.CMD_108:
			CMD_parser_108 cmd_108 = new CMD_parser_108();
			StationEventReport_108 resp_108 = cmd_108.unpack(receiveDataBody);
			responseHandleService.handleEventReport(resp_108, base, stationEquipId);
			break;

		case ServerDecoder.CMD_118:
			CMD_parser_118 cmd_118 = new CMD_parser_118();
			StationFaultReport_118_ext resp_118_ext = cmd_118.unpack(headStartStr, version, receiveDataBody);
			responseHandleService.handleFaultReport(resp_118_ext, base, stationEquipId);
			break;

		case ServerDecoder.CMD_110:
			CMD_parser_110 cmd_110 = new CMD_parser_110();
			StationStatInfoQueryResponse_110 resp_110 = cmd_110.unpack(receiveDataBody);
			break;
		case ServerDecoder.CMD_112:
			CMD_parser_112 cmd_112 = new CMD_parser_112();
			StationLocalLogQueryResponse_112 resp_112 = cmd_112.unpack(receiveDataBody);
			break;

		case ServerDecoder.CMD_116:
			CMD_parser_116 cmd_116 = new CMD_parser_116();
			StationStatInfoResponse_104 resp_116 = cmd_116.unpack(receiveDataBody);
			responseHandleService.handleQueryChargeStatusUpload(resp_116, base);
			break;

		case ServerDecoder.CMD_202:
			//
			CMD_parser_202 cmd_202 = new CMD_parser_202();
			OrderInfoResponse_202 resp_202 = cmd_202.unpack(headStartStr, version, receiveDataBody);
			responseHandleService.handleChargeOrderResponse(resp_202, "收到账单响应", base);
			break;

		case ServerDecoder.CMD_206:
			CMD_parser_202 cmd_206 = new CMD_parser_202();
			OrderInfoResponse_202 resp_206 = cmd_206.unpack(headStartStr, version, receiveDataBody);
			responseHandleService.handleChargeOrderResponse(resp_206, "收到206响应", base);
			break;

		case ServerDecoder.CMD_302:
			CMD_parser_302 cmd_302 = new CMD_parser_302();
			BMSRequest_302 resp_302 = cmd_302.parse(receiveDataBody);
			responseHandleService.handleBMSRequest(resp_302, stationEquipId, base);
			break;

		case ServerDecoder.CMD_402:
			CMD_parser_402 cmd_402 = new CMD_parser_402();
			HistoryOrderInfoResponse_402 resp_402 = cmd_402.parse(headStartStr, receiveDataBody);
			responseHandleService.handleHistoryChargeOrderResponse(resp_402, base);
			break;

		case ServerDecoder.CMD_408:
			CMD_parser_408 cmd_408 = new CMD_parser_408();
			LogReportResponse_408 resp_408 = cmd_408.unpack(receiveDataBody);
			responseHandleService.handleHistoryLog(resp_408, base);
			break;

		case ServerDecoder.CMD_410:
			CMD_parser_410 cmd_410 = new CMD_parser_410();
			LogReportResponse_408 resp_410 = cmd_410.unpack(receiveDataBody);
			responseHandleService.handleHistoryLog(resp_410, base);
			break;
		case ServerDecoder.CMD_510:
			CMD_parser_410 cmd_510 = new CMD_parser_410();
			LogReportResponse_408 resp_510 = cmd_510.unpack(receiveDataBody);
			responseHandleService.handleBmshistoryUpload(resp_510, base);
			break;

		case ServerDecoder.CMD_802:
			CMD_parser_802 cmd_802 = new CMD_parser_802();
			AuthResponse_802 resp_802 = cmd_802.parse(receiveDataBody);
			responseHandleService.handleAuthResponse(resp_802, base);
			break;

		case ServerDecoder.CMD_1302:
			CMD_parser_1302 cmd_1302 = new CMD_parser_1302();
			String s1302 = cmd_1302.parse(receiveDataBody);
			responseHandleService.handle1302(s1302, base);
			break;

		case ServerDecoder.CMD_1304:
			CMD_parser_1304 cmd_1304 = new CMD_parser_1304();
			String[] s1304 = cmd_1304.parse(receiveDataBody);
			responseHandleService.handle1304(s1304, base);
			break;

		case ServerDecoder.CMD_1306:
			CMD_parser_1306 cmd_1306 = new CMD_parser_1306();
			ServiceFee_1306 s1306 = cmd_1306.parse(receiveDataBody);
			responseHandleService.handle1306(s1306, base);
			break;

		case ServerDecoder.CMD_1308:
			CMD_parser_1308 cmd_1308 = new CMD_parser_1308();
			DelayFee_1308 s1308 = cmd_1308.parse(receiveDataBody);
			responseHandleService.handle1308(s1308, base);
			break;

		case ServerDecoder.CMD_1102:
			CMD_parser_1102 cmd_1102 = new CMD_parser_1102();
			HttpUpdate_1102 h1102 = cmd_1102.parse(receiveDataBody);
			responseHandleService.handle1102(h1102, base);
			break;

		case ServerDecoder.CMD_1104:
			CMD_parser_1104 cmd_1104 = new CMD_parser_1104();
			FileVersion_1104 h1104 = cmd_1104.parse(receiveDataBody);
			responseHandleService.handle1104(h1104, stationEquipId, base);
			break;

		case ServerDecoder.CMD_1106:
			CMD_parser_1106 cmd_1106 = new CMD_parser_1106();
			UpdateProcess_1106 h1106 = cmd_1106.parse(receiveDataBody);
			responseHandleService.handle1106(h1106, stationEquipId, base);
			break;

		default:
			break;

		}

	}
	/*
	 * public void procPINGREQ(ChannelService channelService, byte[] dataByte,
	 * ChannelHandlerContext channelHandlerContext, String topic,
	 * ServerMqttHandlerService serverMqttHandlerService) { int cmd_no =
	 * serverDecoder.getCmdNo(dataByte); log.debug("[procPINGREQ]命令编码：" +
	 * cmd_no);
	 * 
	 * }
	 */
}
