package com.me.unicron.iot.gateway.service.impl;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.me.epower.component.ChargeConstant;
import com.me.epower.component.CmdExcuteResult;
import com.me.epower.component.TResponse;
import com.me.epower.direct.entity.chargecloud.CleanRequestParam;
import com.me.epower.direct.entity.chargecloud.ControlQueryParam;
import com.me.epower.direct.entity.chargecloud.FileVersionQueryParam;
import com.me.epower.direct.entity.chargecloud.HttpUpdateParam;
import com.me.epower.direct.entity.chargecloud.ScreenInfoParam;
import com.me.epower.direct.entity.chargecloud.logBmsRequestParam;
import com.me.epower.direct.entity.constant.RedisConstant;
import com.me.epower.direct.entity.downward.AuthRequest_801;
import com.me.epower.direct.entity.downward.ChargeControlQuery_5;
import com.me.epower.direct.entity.downward.CleanRequest_19;
import com.me.epower.direct.entity.downward.CleanRequest_21;
import com.me.epower.direct.entity.downward.FileVersionQuery_1107;
import com.me.epower.direct.entity.downward.Httpupdate_1101;
import com.me.epower.direct.entity.downward.LogRequest_409;
import com.me.epower.direct.entity.downward.StationIntPara_1;
import com.me.epower.direct.entity.downward.StationStringPara_3;
import com.me.epower.direct.enums.StationClusterCmd;
import com.me.unicron.EncodeUtil;
import com.me.unicron.RSACoder;
import com.me.unicron.date.DateUtils;
import com.me.unicron.iot.bootstrap.ChannelService;
import com.me.unicron.iot.bootstrap.channel.StationManagementService;
import com.me.unicron.iot.gateway.EnvConst;
import com.me.unicron.iot.gateway.service.InitService;
import com.me.unicron.iot.gateway.service.util.PubChannelUtil;
import com.me.unicron.iot.gateway.worker.impl.CommandWorker;
import com.me.unicron.iot.kafka.KafkaProducer;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.impl.CMD_3;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class InitServiceImpl implements InitService {

    private static final String TYPE_SET = "1";

    private static final String TYPE_QUERY = "0";

    @Autowired
    KafkaProducer kafkaProducer;

    @Autowired
    CommandWorker commandWorker;

    @Autowired
    private StationManagementService stationManagementService;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 后台服务器设置充电桩整形工作参数
     */
    public TResponse<String> setIntPara(StationIntPara_1 stationIntPara_1) {
        stationIntPara_1.setCmd_type(TYPE_SET);
        return sendIntParaRequest(stationIntPara_1);
    }

    /**
     * 后台服务器查询充电桩整形工作参数
     */
    public TResponse<String> queryIntPara(StationIntPara_1 stationIntPara_1) {
        stationIntPara_1.setCmd_type(TYPE_QUERY);
        return sendIntParaRequest(stationIntPara_1);
    }

    private TResponse<String> sendIntParaRequest(StationIntPara_1 stationIntPara_1) {
        String equipmentId = stationIntPara_1.getEquipmentId();

        /*
         * ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService); if(channelService==null){ return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
         * }
         * 
         * CMD_1 cmd_1 = new CMD_1(); byte[] byte1 = cmd_1.getPayload(stationIntPara_1); try { String msg = new String(byte1, CharsetDef.CHARSET); byte[] sendbytes =
         * msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8); log.info("发送设置充电桩整形工作参数:"); EncodeUtil.printHex(byte1); channelService.push(equipmentId, logicServerQos.getQos(), sendbytes); } catch
         * (UnsupportedEncodingException e) { return TResponse.valueOf(TResponse.Status.GENERAL_ERROR); }
         */
        StationClusterCmd cmdNo = StationClusterCmd.RW_INT_PARA;
        stationIntPara_1.setBaseEquipmentId(equipmentId);
        stationIntPara_1.setCmdNo(cmdNo);
        long timestamp = System.currentTimeMillis();
        stationIntPara_1.setTimestamp(timestamp);
		
        ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
        if (channelService != null) {
            log.info("本节点处理命令！");
            boolean result = commandWorker.doRWIntPara(channelService, stationIntPara_1);
            if (!result) {
                return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
            }
        } else {

            String msg = stationIntPara_1.toString();
            kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
            log.info("命令投递成功={}", msg);
        }
        return TResponse.valueOf(TResponse.Status.OK);
    }

    /**
     * 后台服务器设置充电桩字符串形工作参数
     */
    public TResponse<CmdExcuteResult> setStringPara(StationStringPara_3 stationStringPara_3) {
        stationStringPara_3.setCmd_type(TYPE_SET);
        return sendStringParaRequest(stationStringPara_3);
    }

    /**
     * 后台服务器设置充电桩字符串形工作参数
     */
    public TResponse<CmdExcuteResult> queryStringPara(StationStringPara_3 stationStringPara_3) {
        stationStringPara_3.setCmd_type(TYPE_QUERY);
        return sendStringParaRequest(stationStringPara_3);
    }

    public TResponse<CmdExcuteResult> sendStringParaRequest(StationStringPara_3 stationStringPara_3) {
        String equipmentId = stationStringPara_3.getEquipmentId();
        /*
         * ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService); if(channelService==null){ return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
         * } try {
         * 
         * String cmd_type = stationStringPara_3.getCmd_type(); if(TYPE_SET.equals(cmd_type)){ String str = stationStringPara_3.getData_body(); byte[] byteStr =
         * str.getBytes(CharsetDef.NETTY_CHARSET_UTF8); String strISO = new String(byteStr,CharsetDef.CHARSET); stationStringPara_3.setData_body(strISO); } CMD_3 cmd_3 = new CMD_3(); byte[] byte3 =
         * cmd_3.getPayload(stationStringPara_3); String msg = new String(byte3, CharsetDef.CHARSET); byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8); log.info("发送设置充电桩字符串形工作参数:");
         * EncodeUtil.printHex(byte3); channelService.push(equipmentId, logicServerQos.getQos(), sendbytes); } catch (Exception e) { log.error("sendStringParaRequest error",e); return
         * TResponse.valueOf(TResponse.Status.GENERAL_ERROR); }
         */

        StationClusterCmd cmdNo = StationClusterCmd.RW_STRING_PARA;
        stationStringPara_3.setBaseEquipmentId(equipmentId);
        stationStringPara_3.setCmdNo(cmdNo);
        long timestamp = System.currentTimeMillis();
        stationStringPara_3.setTimestamp(timestamp);
        
        //返回发送的字节和最终结果查询ID
        CmdExcuteResult ret=new CmdExcuteResult();
        ret.setQueryid(stationStringPara_3.getQueryid());
        ret.setCmddate(DateUtils.Date2yyyyMMddHHmmss(new Date(timestamp)));
        CMD_3 cmd_3 = new CMD_3();
        MqttNetMsgBase base = new MqttNetMsg();
        byte[] byte3 = cmd_3.getPayload(stationStringPara_3, base);
        ret.setSenddata(EncodeUtil.printHex(byte3));
        
        ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
        if (channelService != null) {
            log.info("本节点处理命令！");
            boolean result = commandWorker.doRWStringPara(channelService, stationStringPara_3);
            if (!result) {
                return TResponse.valueOf(TResponse.Status.GENERAL_ERROR,ret);
            }
        } else {
            String msg = stationStringPara_3.toString();
            kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
            log.info("字符型命令投递成功={}", msg);
        }
        
        return TResponse.valueOf(TResponse.Status.OK,ret);
    }

    @Override
    public TResponse<String> controlRequest(ControlQueryParam controlQueryParam) {
        String equipmentId = controlQueryParam.getEquipmentId();
        ChargeControlQuery_5 chargeControlQuery_5 = new ChargeControlQuery_5();
        chargeControlQuery_5.setGun_no(controlQueryParam.getGun_no());
        chargeControlQuery_5.setStart_no(controlQueryParam.getStart_no());
        chargeControlQuery_5.setCmd_size(controlQueryParam.getCmd_size());
        String cmd_length = controlQueryParam.getCmd_length();
        chargeControlQuery_5.setCmd_length(cmd_length);
        String cmd_params = controlQueryParam.getCmd_params();
        int num = Integer.parseInt(cmd_length);
        /*
         * ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService); if(channelService==null){ return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
         * }
         */
        if (StringUtils.isBlank(cmd_params))
            return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
        byte[] byte4 = new byte[num];
        byte[] emptybyte = new byte[4];
        byte[] byte55 = MqttNetMsg.getDefaultControlParam();
        byte[] byte1 = EncodeUtil.intToByte(1);
        for (int i = 0; i < cmd_params.length(); i++) {
            char c = cmd_params.charAt(i);
            switch (c) {
            case '0':
                System.arraycopy(emptybyte, 0, byte4, 4 * i, 4);
                break;
            case '1':
                System.arraycopy(byte1, 0, byte4, 4 * i, 4);
                break;
            case '5':
                System.arraycopy(byte55, 0, byte4, 4 * i, 4);
                break;
            }
        }
        chargeControlQuery_5.setData_body(byte4);

        /*
         * CMD_5 cmd_5 = new CMD_5(); log.info("chargeControlQuery_5{}",chargeControlQuery_5); byte[] byteResult = cmd_5.getPayload(chargeControlQuery_5); try { String msg = new String(byteResult,
         * CharsetDef.CHARSET); byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8); log.info("发送控制命令:"); EncodeUtil.printHex(byteResult); channelService.push(equipmentId,
         * logicServerQos.getQos(), sendbytes); } catch (UnsupportedEncodingException e) { return TResponse.valueOf(TResponse.Status.GENERAL_ERROR); }
         */
        StationClusterCmd cmdNo = StationClusterCmd.STATION_CTR;
        chargeControlQuery_5.setBaseEquipmentId(equipmentId);
        chargeControlQuery_5.setCmdNo(cmdNo);
        long timestamp = System.currentTimeMillis();
        chargeControlQuery_5.setTimestamp(timestamp);

        ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
        if (channelService != null) {
            log.info("本节点处理命令！");
            boolean result = commandWorker.doStationControl(channelService, chargeControlQuery_5);
            if (!result) {
                return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
            }
        } else {

            String msg = chargeControlQuery_5.toString();
            kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
            log.info("命令投递成功={}", msg);
        }

        return TResponse.valueOf(TResponse.Status.OK);
    }

    @Override
    public TResponse<String> httpUpdate(HttpUpdateParam httpUpdateParam) {
        String equipmentId = httpUpdateParam.getEquipmentId();
        /*
         * ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService); if(channelService==null){ return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
         * }
         */
        Httpupdate_1101 httpupdate_1101 = new Httpupdate_1101();
        httpupdate_1101.setBaseEquipmentId(equipmentId);

        httpupdate_1101.setMd5(httpUpdateParam.getMd5());
        httpupdate_1101.setUrl(httpUpdateParam.getUrl());
        long timestamp = System.currentTimeMillis();
        httpupdate_1101.setTimestamp(timestamp);
        /*
         * CMD_1101 cmd_1101 = new CMD_1101(); log.info("httpUpdateParam{}",httpUpdateParam); byte[] byteResult = cmd_1101.getPayload(httpupdate_1101); try { String msg = new String(byteResult,
         * CharsetDef.CHARSET); byte[] sendbytes = msg.getBytes(CharsetDef.NETTY_CHARSET_UTF8); log.info("发送HTTP升级命令:"); EncodeUtil.printHex(byteResult); channelService.push(equipmentId,
         * logicServerQos.getQos(), sendbytes);
         * 
         * } catch (UnsupportedEncodingException e) { return TResponse.valueOf(TResponse.Status.GENERAL_ERROR); }
         */
        StationClusterCmd cmdNo = StationClusterCmd.HTTP_UPDATE;
        httpupdate_1101.setBaseEquipmentId(equipmentId);
        httpupdate_1101.setCmdNo(cmdNo);
        ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
        if (channelService != null) {
            log.info("本节点处理命令！");
            boolean result = commandWorker.doHttpupdate(channelService, httpupdate_1101);
            if (!result) {
                return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
            }
        } else {
            String msg = httpupdate_1101.toString();
            kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
            log.info("命令投递成功={}", msg);
        }

        return TResponse.valueOf(TResponse.Status.OK);
    }

    @Override
    public TResponse<String> fileVersionQuery(FileVersionQueryParam fileVersionQueryParam) {
        String equipmentId = fileVersionQueryParam.getEquipmentId();
        FileVersionQuery_1107 fileVersionQuery_1107 = new FileVersionQuery_1107();
        fileVersionQuery_1107.setBaseEquipmentId(equipmentId);
        fileVersionQuery_1107.setCmdNo(StationClusterCmd.FILE_VERSION_QUERY);
        long timestamp = System.currentTimeMillis();
        fileVersionQuery_1107.setTimestamp(timestamp);
		
        ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
        if (channelService != null) {
            log.info("本节点处理命令！");
            boolean result = commandWorker.queryFileVersion(channelService, fileVersionQuery_1107);
            if (!result) {
                return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
            }
        } else {
            String msg = fileVersionQuery_1107.toString();
            kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
            log.info("文件版本查询命令投递成功={}", msg);
        }

        return TResponse.valueOf(TResponse.Status.OK);
    }

    @Override
    public TResponse<String> cleanStringRequest(CleanRequestParam cleanRequestParam) {
        String equipmentId = cleanRequestParam.getEquipmentId();
        StationClusterCmd cmdNo = StationClusterCmd.CLEAN_STRING_REQUEST;
        CleanRequest_21 cleanRequest_21 = new CleanRequest_21();
        cleanRequest_21.setEquipmentId(equipmentId);
        cleanRequest_21.setClean_no(cleanRequestParam.getClean_no());
        cleanRequest_21.setBaseEquipmentId(equipmentId);
        cleanRequest_21.setCmdNo(cmdNo);
        long timestamp = System.currentTimeMillis();
        cleanRequest_21.setTimestamp(timestamp);
		
        ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
        if (channelService != null) {
            log.info("本节点处理命令！");
            boolean result = commandWorker.doCleanStringPara(channelService, cleanRequest_21);
            if (!result) {
                return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
            }
        } else {
            String msg = cleanRequest_21.toString();

            kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
            log.info("命令投递成功={}", msg);
        }

        return TResponse.valueOf(TResponse.Status.OK);
    }

    @Override
    public TResponse<String> cleanIntRequest(CleanRequestParam cleanRequestParam) {
        String equipmentId = cleanRequestParam.getEquipmentId();
        StationClusterCmd cmdNo = StationClusterCmd.CLEAN_INT_REQUEST;
        CleanRequest_19 cleanRequest_19 = new CleanRequest_19();
        cleanRequest_19.setEquipmentId(equipmentId);
        cleanRequest_19.setClean_no(cleanRequestParam.getClean_no());
        cleanRequest_19.setBaseEquipmentId(equipmentId);
        cleanRequest_19.setCmdNo(cmdNo);
        long timestamp = System.currentTimeMillis();
        cleanRequest_19.setTimestamp(timestamp);
        ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
        if (channelService != null) {
            log.info("本节点处理命令！");
            boolean result = commandWorker.doCleanIntPara(channelService, cleanRequest_19);
            if (!result) {
                return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
            }
        } else {
            String msg = cleanRequest_19.toString();
            kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
            log.info("命令投递成功={}", msg);
        }

        return TResponse.valueOf(TResponse.Status.OK);
    }

    @Override
    public TResponse<String> logQuery(String equipmentId) {
        StationClusterCmd cmdNo = StationClusterCmd.LOG_QUERY;
        LogRequest_409 logRequest_409 = new LogRequest_409();
        logRequest_409.setCmdNo(cmdNo);
        logRequest_409.setBaseEquipmentId(equipmentId);
        logRequest_409.setEquipmentId(equipmentId);
        long timestamp = System.currentTimeMillis();
        logRequest_409.setTimestamp(timestamp);
		
        logRequest_409.setFilename("unicron" + System.currentTimeMillis());
        ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
        if (channelService != null) {
            log.info("本节点处理命令！");
            boolean result = commandWorker.queryLog(channelService, logRequest_409);
            if (!result) {
                return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
            }
        } else {
            String msg = logRequest_409.toString();
            kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
            log.info("命令投递成功={}", msg);
        }

        return TResponse.valueOf(TResponse.Status.OK);
    }

    @Override
    public TResponse<String> authRequest(String equipmentId) {
        String data = equipmentId + ChargeConstant.AUTH_CONSTANT;
        String encodeData = RSACoder.encodeByPublicKey(data, ChargeConstant.zhichongClientPublicKey);
        AuthRequest_801 req = new AuthRequest_801();
        req.setEncodeData(encodeData);
        byte[] charsequenceToBytes = EncodeUtil.charsequenceToByte(encodeData);
        req.setEncodeDataLength(String.valueOf(charsequenceToBytes.length));
        req.setEquipmentId(equipmentId);
        req.setEncodeType("1");
        req.setTimestamp(System.currentTimeMillis());
        log.info("命令投递authRequest={}", req);
        ChannelService channelService = PubChannelUtil.getChannelService(equipmentId, stationManagementService);
        if (channelService != null) {
            log.info("本节点处理命令！");
            boolean result = commandWorker.authRequest(channelService, req);
            if (!result) {
                return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
            }else{
                stringRedisTemplate.opsForValue().increment(RedisConstant.UNICRON_IS_SEND_AUTH + equipmentId, 1);
            }
        } else {
            String msg = req.toString();
            kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
            log.info("命令投递成功={}", msg);
            stringRedisTemplate.opsForValue().increment(RedisConstant.UNICRON_IS_SEND_AUTH + equipmentId, 1);
        }
        return TResponse.valueOf(TResponse.Status.OK);
    }

	@Override
	public TResponse<String> logQueryBms(logBmsRequestParam logBmsRequestParam) {
		StationClusterCmd cmdNo = StationClusterCmd.LOG_QUERY_BMS;
        LogRequest_409 logRequest_409 = new LogRequest_409();
        logRequest_409.setCmdNo(cmdNo);
        logRequest_409.setReserved2(logBmsRequestParam.getGunNum());
        logRequest_409.setBaseEquipmentId(logBmsRequestParam.getEquipmentId());
        logRequest_409.setEquipmentId(logBmsRequestParam.getEquipmentId());
        logRequest_409.setFilename("unicronbms" + System.currentTimeMillis());
        log.info("logQueryBms:"+JSON.toJSONString(logBmsRequestParam));
        ChannelService channelService = PubChannelUtil.getChannelService(logBmsRequestParam.getEquipmentId(), stationManagementService);
        if (channelService != null) {
            log.info("本节点处理命令！");
            boolean result = commandWorker.queryLogBms(channelService, logRequest_409);
            if (!result) {
                return TResponse.valueOf(TResponse.Status.GENERAL_ERROR);
            }
        } else {
            String msg = logRequest_409.toString();
            kafkaProducer.sendMsg(msg, EnvConst.CUR_ENV_TYPE);
            log.info("命令投递成功={}", msg);
        }

        return TResponse.valueOf(TResponse.Status.OK);
	}

	@Override
	public TResponse<String> setScreenInfo(ScreenInfoParam screenInfoParam) {
		// TODO Auto-generated method stub
		
		log.info("setScreenInfo:{}",JSON.toJSONString(screenInfoParam));
		
		StationStringPara_3 stationStringPara_3 = new StationStringPara_3();
		stationStringPara_3.setEquipmentId(screenInfoParam.getEquipmentId());
		stationStringPara_3.setCmd_type("1");
		stationStringPara_3.setPara_bytes_lenth("128");
		
		stationStringPara_3.setStart_addr("9");
		stationStringPara_3.setData_body(screenInfoParam.getBottomText());
		setStringPara(stationStringPara_3);
		
		stationStringPara_3.setStart_addr("16");
		stationStringPara_3.setData_body(screenInfoParam.getLeftQRText());
		setStringPara(stationStringPara_3);
		
		stationStringPara_3.setStart_addr("18");
		stationStringPara_3.setData_body(screenInfoParam.getRightQRText());
		setStringPara(stationStringPara_3);
		
		stationStringPara_3.setStart_addr("17");
		stationStringPara_3.setData_body(screenInfoParam.getLeftQR());
		setStringPara(stationStringPara_3);
		
		stationStringPara_3.setStart_addr("19");
		stationStringPara_3.setData_body(screenInfoParam.getRigtQR());
		setStringPara(stationStringPara_3);
		
		return TResponse.valueOf(TResponse.Status.OK);
	}


}
