package com.me.unicron.iot.message.parser.impl;

import com.me.epower.direct.entity.upward.StationStatInfoResponse_104;
import com.me.unicron.EncodeUtil;
import com.me.unicron.iot.message.parser.BaseParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_parser_116 implements BaseParser {

    public static final int CMD_NO = 116;

    public StationStatInfoResponse_104 unpack(byte[] dataByte) {
        StationStatInfoResponse_104 resp = new StationStatInfoResponse_104();
        try {
            byte[] reserved1 = new byte[2];
            System.arraycopy(dataByte, 0, reserved1, 0, 2);
            resp.setReserved1(EncodeUtil.byteToShort(reserved1) + "");
            byte[] reserved2 = new byte[2];
            System.arraycopy(dataByte, 2, reserved2, 0, 2);
            resp.setReserved2(EncodeUtil.byteToShort(reserved2) + "");

            byte[] stationId = new byte[32];
            System.arraycopy(dataByte, 4, stationId, 0, 32);
            resp.setEquipmentId(EncodeUtil.byteToCharsequence(stationId, true));

            byte[] gun_cnt = new byte[1];
            System.arraycopy(dataByte, 36, gun_cnt, 0, 1);
            resp.setGun_cnt(EncodeUtil.byteToValue(gun_cnt[0]));

            byte[] charge_index_no = new byte[1];
            System.arraycopy(dataByte, 37, charge_index_no, 0, 1);
            resp.setCharge_index_no(EncodeUtil.byteToValue(charge_index_no[0]));

            byte[] gun_type = new byte[1];
            System.arraycopy(dataByte, 38, gun_type, 0, 1);
            resp.setGun_type(EncodeUtil.byteToValue(gun_type[0]) + "");

            byte[] work_stat = new byte[1];
            System.arraycopy(dataByte, 39, work_stat, 0, 1);
            resp.setWork_stat(EncodeUtil.byteToValue(work_stat[0]) + "");

            byte[] soc_percent = new byte[1];
            System.arraycopy(dataByte, 40, soc_percent, 0, 1);
            resp.setSoc_percent(EncodeUtil.byteToValue(soc_percent[0]) + "");

            byte[] alarm_stat = new byte[4];
            System.arraycopy(dataByte, 41, alarm_stat, 0, 4);
            resp.setAlarm_stat(EncodeUtil.byteToInt(alarm_stat) + "");

            byte[] car_connection_stat = new byte[1];
            System.arraycopy(dataByte, 45, car_connection_stat, 0, 1);
            resp.setCar_connection_stat(EncodeUtil.byteToValue(car_connection_stat[0]) + "");
            // 累计充电费用
            byte[] cumulative_charge_fee = new byte[4];
            System.arraycopy(dataByte, 46, cumulative_charge_fee, 0, 4);
            resp.setCumulative_charge_fee(EncodeUtil.byteToInt(cumulative_charge_fee) + "");

            byte[] reserved3 = new byte[4];
            System.arraycopy(dataByte, 50, reserved3, 0, 4);
            resp.setReserved3(EncodeUtil.byteToInt(reserved3) + "");

            byte[] reserved4 = new byte[4];
            System.arraycopy(dataByte, 54, reserved4, 0, 4);
            resp.setReserved4(EncodeUtil.byteToInt(reserved4) + "");

            byte[] dc_charge_voltage = new byte[2];
            System.arraycopy(dataByte, 58, dc_charge_voltage, 0, 2);
            resp.setDc_charge_voltage(EncodeUtil.byteToShort(dc_charge_voltage) + "");

            byte[] dc_charge_current = new byte[2];
            System.arraycopy(dataByte, 60, dc_charge_current, 0, 2);
            resp.setDc_charge_current(EncodeUtil.byteToShort(dc_charge_current) + "");

            byte[] bms_need_voltage = new byte[2];
            System.arraycopy(dataByte, 62, bms_need_voltage, 0, 2);
            resp.setBms_need_voltage(EncodeUtil.byteToShort(bms_need_voltage) + "");

            byte[] bms_need_current = new byte[2];
            System.arraycopy(dataByte, 64, bms_need_current, 0, 2);
            resp.setBms_need_current(EncodeUtil.byteToShort(bms_need_current) + "");

            byte[] bms_charge_mode = new byte[1];
            System.arraycopy(dataByte, 66, bms_charge_mode, 0, 1);
            resp.setBms_charge_mode(EncodeUtil.byteToValue(bms_charge_mode[0]) + "");

            byte[] dc_a_vol = new byte[2];
            System.arraycopy(dataByte, 67, dc_a_vol, 0, 2);
            resp.setDc_a_vol(EncodeUtil.byteToShort(dc_a_vol) + "");
            byte[] dc_b_vol = new byte[2];
            System.arraycopy(dataByte, 69, dc_b_vol, 0, 2);
            resp.setDc_b_vol(EncodeUtil.byteToShort(dc_b_vol) + "");

            byte[] dc_c_vol = new byte[2];
            System.arraycopy(dataByte, 71, dc_c_vol, 0, 2);
            resp.setDc_c_vol(EncodeUtil.byteToShort(dc_c_vol) + "");

            byte[] dc_a_cur = new byte[2];
            System.arraycopy(dataByte, 73, dc_a_cur, 0, 2);
            resp.setDc_a_cur(EncodeUtil.byteToShort(dc_a_cur) + "");

            byte[] dc_b_cur = new byte[2];
            System.arraycopy(dataByte, 75, dc_b_cur, 0, 2);
            resp.setDc_b_cur(EncodeUtil.byteToShort(dc_b_cur) + "");

            byte[] dc_c_cur = new byte[2];
            System.arraycopy(dataByte, 77, dc_c_cur, 0, 2);
            resp.setDc_c_cur(EncodeUtil.byteToShort(dc_c_cur) + "");
            // 剩余充电时间
            byte[] charge_full_time_left = new byte[2];
            System.arraycopy(dataByte, 79, charge_full_time_left, 0, 2);
            resp.setCharge_full_time_left(EncodeUtil.byteToShort(charge_full_time_left) + "");
            // 充电时长(秒)
            byte[] charged_sec = new byte[4];
            System.arraycopy(dataByte, 81, charged_sec, 0, 4);
            resp.setCharged_sec(EncodeUtil.byteToInt(charged_sec) + "");
            // 本次充电累计充电电量
            byte[] cum_charge_kwh_amount = new byte[4];
            System.arraycopy(dataByte, 85, cum_charge_kwh_amount, 0, 4);
            resp.setCum_charge_kwh_amount(EncodeUtil.byteToInt(cum_charge_kwh_amount) + "");

            byte[] before_charge_meter_kwh_num = new byte[4];
            System.arraycopy(dataByte, 89, before_charge_meter_kwh_num, 0, 4);
            resp.setBefore_charge_meter_kwh_num(EncodeUtil.byteToInt(before_charge_meter_kwh_num) + "");

            byte[] now_meter_kwh_num = new byte[4];
            System.arraycopy(dataByte, 93, now_meter_kwh_num, 0, 4);
            resp.setNow_meter_kwh_num(EncodeUtil.byteToInt(now_meter_kwh_num) + "");

            byte[] start_charge_type = new byte[1];
            System.arraycopy(dataByte, 97, start_charge_type, 0, 1);
            resp.setStart_charge_type(EncodeUtil.byteToValue(start_charge_type[0]) + "");

            byte[] charge_policy = new byte[1];
            System.arraycopy(dataByte, 98, charge_policy, 0, 1);
            resp.setCharge_policy(EncodeUtil.byteToValue(charge_policy[0]) + "");

            byte[] charge_policy_para = new byte[4];
            System.arraycopy(dataByte, 99, charge_policy_para, 0, 4);
            resp.setCharge_policy_para(EncodeUtil.byteToInt(charge_policy_para) + "");

            byte[] book_flag = new byte[1];
            System.arraycopy(dataByte, 103, book_flag, 0, 1);
            resp.setBook_flag(EncodeUtil.byteToValue(book_flag[0]) + "");

            byte[] charge_user_id = new byte[32];
            System.arraycopy(dataByte, 104, charge_user_id, 0, 32);
            resp.setCharge_user_id(EncodeUtil.byteToCharsequence(charge_user_id, true));

            byte[] book_timeout_min = new byte[1];
            System.arraycopy(dataByte, 136, book_timeout_min, 0, 1);
            resp.setBook_timeout_min(EncodeUtil.byteToValue(book_timeout_min[0]) + "");

            byte[] book_start_charge_time = new byte[8];
            System.arraycopy(dataByte, 137, book_start_charge_time, 0, 8);
            // 时间编码
            resp.setBook_start_charge_time(EncodeUtil.byteDateToDateStr(book_start_charge_time));

            byte[] before_charge_card_account = new byte[4];
            System.arraycopy(dataByte, 145, before_charge_card_account, 0, 4);
            resp.setBefore_charge_card_account(EncodeUtil.byteToInt(before_charge_card_account) + "");

            byte[] reserved5 = new byte[4];
            System.arraycopy(dataByte, 149, reserved5, 0, 4);
            resp.setReserved5(EncodeUtil.byteToInt(reserved5) + "");

            // 充电功率
            byte[] charge_power_kw = new byte[4];
            System.arraycopy(dataByte, 153, charge_power_kw, 0, 4);
            resp.setCharge_power_kw(EncodeUtil.byteToInt(charge_power_kw) + "");

            byte[] reserved6 = new byte[4];
            System.arraycopy(dataByte, 157, reserved6, 0, 4);
            resp.setReserved6(EncodeUtil.byteToInt(reserved6) + "");

            byte[] reserved7 = new byte[4];
            System.arraycopy(dataByte, 161, reserved7, 0, 4);
            resp.setReserved7(EncodeUtil.byteToInt(reserved7) + "");

            byte[] reserved8 = new byte[4];
            System.arraycopy(dataByte, 165, reserved8, 0, 4);
            resp.setReserved8(EncodeUtil.byteToInt(reserved8) + "");

            return resp;
        } catch (Exception e) {
            log.error("CMD_parser_104 parse error{}", e);
        }
        return null;

    }
}