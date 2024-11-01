package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.me.epower.direct.entity.downward.StartChargeQuery_7;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.message.packer.BaseCMD;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.math.NumberUtils;

//
/**
 * @author lianyadong
 * @func：(CODE=7)后台服务器下发充电桩开启充电控制命令
 */
@Slf4j
public class CMD_7 implements BaseCMD<StartChargeQuery_7> {

    //默认为查询时长度
    private static final int BODY_LENGTH = 72;
    private static final int CMD_NO = 7;
    //private static short inc = 0;

    @Override
    public byte[] getPayload(StartChargeQuery_7 startChargeQuery_7,MqttNetMsgBase base) {
        int bodyLenth = BODY_LENGTH;
        String gun_no = startChargeQuery_7.getPortId();
        if (StringUtils.isBlank(gun_no) || !NumberUtils.isNumber(gun_no)) {
        	log.info("gun_no not set!");
        }

        String charge_valid_type = startChargeQuery_7.getCharge_valid_type();
        if (StringUtils.isBlank(charge_valid_type) || !NumberUtils.isNumber(charge_valid_type)) {
        	log.info("charge_valid_type not set!");
        }

        String charge_policy = startChargeQuery_7.getCharge_policy();
        if (StringUtils.isBlank(charge_policy) || !NumberUtils.isNumber(charge_policy)) {
        	log.info("charge_policy not set!");
        }

        String charge_policy_para = startChargeQuery_7.getCharge_policy_para();
        if (StringUtils.isBlank(charge_policy_para) || !NumberUtils.isNumber(charge_policy_para)) {
        	log.info("charge_policy_para not set!");
        }

        String book_start_time = startChargeQuery_7.getBook_start_time();

        String book_timeout_time = startChargeQuery_7.getBook_timeout_time();

        String user_id = startChargeQuery_7.getUser_id();
        if (StringUtils.isBlank(user_id)) {
        	log.info("user_id not set!");
        }

        String offline_charge_flag = startChargeQuery_7.getOffline_charge_flag();
        if (StringUtils.isBlank(offline_charge_flag) || !NumberUtils.isNumber(offline_charge_flag)) {
            //throw new MqttParaException("offline_charge_flag not set!");
        }

        String offline_charge_amount = startChargeQuery_7.getOffline_charge_amount();
        if (StringUtils.isBlank(offline_charge_amount) || !NumberUtils.isNumber(offline_charge_amount)) {
            //throw new MqttParaException("offline_charge_amount not set!");
        }

        bodyLenth = BODY_LENGTH;
        int totalLen = base.getLength(bodyLenth);
        byte[] data = new byte[totalLen];

        data=base.fillPayload2CmdNo(data, totalLen, (short) CMD_NO);
        int bytesOffset = base.getBytesOffset();

        //body部分，4字节保留
        byte[] reserved = new byte[4];
        System.arraycopy(reserved, 0, data, 8+bytesOffset, 4);
        //充电枪口
        byte[] byt_gun_no = new byte[] { EncodeUtil.ValueToByte(gun_no) };
        System.arraycopy(byt_gun_no, 0, data, 12+bytesOffset, 1);

        //充电生效类型 4
        byte[] charge_valid_type_byt = new byte[4];

        charge_valid_type_byt = EncodeUtil.intToByte(Integer.parseInt(charge_valid_type));

        System.arraycopy(charge_valid_type_byt, 0, data, 13+bytesOffset, 4);

        //预留
        byte[] reseved3_byte = new byte[4];
        System.arraycopy(reseved3_byte, 0, data, 17+bytesOffset, 4);

        //充电策略
        byte[] charge_policy_byte = new byte[4];

        charge_policy_byte = EncodeUtil.intToByte(Integer.parseInt(charge_valid_type));

        System.arraycopy(charge_policy_byte, 0, data, 21+bytesOffset, 4);

        //充电策略参数
        byte[] charge_policy_para_byte = new byte[4];
        charge_policy_para_byte = EncodeUtil.intToByte(Integer.parseInt(charge_policy_para));
        System.arraycopy(charge_policy_para_byte, 0, data, 25+bytesOffset, 4);

        //预约启动时间
        byte[] book_start_time_byt = new byte[8];

        book_start_time_byt = EncodeUtil.longToByte(Long.parseLong(book_start_time));

        System.arraycopy(book_start_time_byt, 0, data, 29+bytesOffset, 8);

        //预约超时时间（保留）
        byte[] book_timeout_time_byt = new byte[] { EncodeUtil.ValueToByte(book_timeout_time) };

        System.arraycopy(book_timeout_time_byt, 0, data, 37+bytesOffset, 1);

        //用户识别号(App) 32
        byte[] user_id_assic = new byte[32];
        try {
            if(startChargeQuery_7.getUser_id()!=null){
                byte[] databyte = startChargeQuery_7.getUser_id().getBytes(CharsetDef.CHARSET);
                System.arraycopy(databyte, 0, user_id_assic, 0, databyte.length);
            }
        } catch (UnsupportedEncodingException e) {
            log.error("CMD_7 parse error", e);
        }
        System.arraycopy(user_id_assic, 0, data, 38+bytesOffset, 32);

        //断网充电标志 1
        byte[] offline_charge_flag_byt = new byte[] { EncodeUtil.ValueToByte(offline_charge_flag) };
        System.arraycopy(offline_charge_flag_byt, 0, data, 70+bytesOffset, 1);

        //离线可充电电量（保留）4
        byte[] offline_charge_amount_byt = new byte[4];
        offline_charge_amount_byt = EncodeUtil.intToByte(Integer.parseInt(offline_charge_amount));
        System.arraycopy(offline_charge_amount_byt, 0, data, 71+bytesOffset, 4);
        
        String need_delayfee = startChargeQuery_7.getNeed_delayfee();
        byte[] needDelayfeeByte = EncodeUtil.intToByte(Integer.parseInt(need_delayfee));
        System.arraycopy(needDelayfeeByte, 0, data, 75+bytesOffset, 1);
        
        String delayfee_wait_time = startChargeQuery_7.getDelayfee_wait_time();
        byte[] delayfeeWaittimeByte = EncodeUtil.intToByte(Integer.parseInt(delayfee_wait_time));
        System.arraycopy(delayfeeWaittimeByte, 0, data, 76+bytesOffset, 4);

        byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
        System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + bodyLenth, 1);
        return data;
    }

    @Override
    public String pack(StartChargeQuery_7 startChargeQuery_7,MqttNetMsgBase base) {
        byte[] data = getPayload(startChargeQuery_7,base);
        try {
            return new String(data, CharsetDef.CHARSET);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    
    
    @Override
    public int getCmdNo() {
        // TODO Auto-generated method stub
        return CMD_NO;
    }

   

    public static void print() {
       
        Map<String, String> dataMap = new HashMap<String, String>();
        dataMap.put("gun_no", "1");
        dataMap.put("cmd_size", "1");
        dataMap.put("start_no", "1");
        //125 -33 37 0 1 0 1 0 0 0 0 0 1 1 0 0 0 4 16 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 26 
        //125 -33 41 0 1 0 1 0 0 0 0 0 1 1 0 0 0 5 20 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 35 
        //125 -33 21 0 1 0 1 0 0 0 0 0 0 1 0 0 0 5 0 0 121 
        //编码前：
        
    }

    public static void main(String[] args) throws Exception {
        print();
    }

}
