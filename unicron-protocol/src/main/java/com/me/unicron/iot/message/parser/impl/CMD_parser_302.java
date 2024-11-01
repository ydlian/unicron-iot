package com.me.unicron.iot.message.parser.impl;

import com.me.unicron.iot.message.parser.BaseParser;
import com.me.epower.direct.entity.downward.BMSRequest_302;
import com.me.unicron.EncodeUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMD_parser_302 implements BaseParser {

    public BMSRequest_302 parse(byte[] bytes) {
        byte[] shortByte = new byte[2];
        byte[] intByte = new byte[4];
        byte[] longByte = new byte[8];
        byte[] byte32 = new byte[32];
        BMSRequest_302 bmsRequest_302 = new BMSRequest_302();
        try {
            for (int i = 0; i < bytes.length;) {
                System.arraycopy(bytes, i, shortByte, 0, 2);
                String index = String.valueOf(EncodeUtil.byteToShort(shortByte));
                bmsRequest_302.setIndex(index);
                i += 2;

                System.arraycopy(bytes, i, shortByte, 0, 2);
                String gun_no = String.valueOf(EncodeUtil.byteToShort(shortByte));
                bmsRequest_302.setGun_no(gun_no);
                i += 2;

                System.arraycopy(bytes, i, byte32, 0, 32);
                String equipmentId = String.valueOf(EncodeUtil.byteToCharsequence(byte32, true));
                bmsRequest_302.setEquipmentId(equipmentId);
                i += 32;

                byte work_stat = bytes[i];
                bmsRequest_302.setWork_stat(EncodeUtil.byteToValue(work_stat));
                i++;

                byte car_connect_stat = bytes[i];
                bmsRequest_302.setCar_connect_stat(EncodeUtil.byteToValue(car_connect_stat));
                i++;

                byte[] byte3 = new byte[3];
                System.arraycopy(bytes, i, byte3, 0, 3);
                bmsRequest_302.setBRM_BMS_connect_version(EncodeUtil.bytesToValue(byte3));
                i += 3;

                byte BR_battery_type = bytes[i];
                bmsRequest_302.setBR_battery_type(EncodeUtil.byteToValue(BR_battery_type));
                i++;

                System.arraycopy(bytes, i, intByte, 0, 4);
                bmsRequest_302.setBRM_battery_power(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;

                System.arraycopy(bytes, i, intByte, 0, 4);
                bmsRequest_302.setBRM_battery_voltage(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;

                System.arraycopy(bytes, i, intByte, 0, 4);
                bmsRequest_302.setBRM_battery_supplier(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;

                System.arraycopy(bytes, i, intByte, 0, 4);
                bmsRequest_302.setBRM_battery_seq(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;

                System.arraycopy(bytes, i, shortByte, 0, 2);
                bmsRequest_302.setBRM_battery_produce_year(String.valueOf(EncodeUtil.byteToShort(shortByte)));
                i += 2;

                byte BRM_battery_produce_month = bytes[i];
                bmsRequest_302.setBRM_battery_produce_month(EncodeUtil.byteToValue(BRM_battery_produce_month));
                i++;

                byte BRM_battery_produce_day = bytes[i];
                bmsRequest_302.setBRM_battery_produce_day(EncodeUtil.byteToValue(BRM_battery_produce_day));
                i++;

                System.arraycopy(bytes, i, intByte, 0, 4);
                bmsRequest_302.setBRM_battery_charge_count(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;

                byte BRM_battery_property_identification = bytes[i];
                bmsRequest_302.setBRM_battery_property_identification(EncodeUtil.byteToValue(BRM_battery_property_identification));
                i++;

                i++;

                //车辆 VIN
                byte[] vinByte = new byte[17];
                System.arraycopy(bytes, i, vinByte, 0, 17);
                bmsRequest_302.setBRM_vin(EncodeUtil.byteToCharsequence(vinByte, true));
                i += 17;

                System.arraycopy(bytes, i, longByte, 0, 8);
                bmsRequest_302.setBRM_BMS_version(String.valueOf(EncodeUtil.byteToLong(longByte)));
                i += 8;

                System.arraycopy(bytes, i, intByte, 0, 4);
                bmsRequest_302.setBCP_max_voltage(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;

                System.arraycopy(bytes, i, intByte, 0, 4);
                bmsRequest_302.setBCP_max_current(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;

                System.arraycopy(bytes, i, intByte, 0, 4);
                bmsRequest_302.setBCP_max_power(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;

                System.arraycopy(bytes, i, intByte, 0, 4);
                bmsRequest_302.setBCP_total_voltage(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;

                byte BCP_max_temperature = bytes[i];
                bmsRequest_302.setBCP_max_temperature(EncodeUtil.byteToValue(BCP_max_temperature));
                i++;

                System.arraycopy(bytes, i, shortByte, 0, 2);
                bmsRequest_302.setBCP_battery_soc(String.valueOf(EncodeUtil.byteToShort(shortByte)));
                i += 2;

                System.arraycopy(bytes, i, intByte, 0, 4);
                bmsRequest_302.setBCP_battery_soc_current_voltage(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;

                byte BRO_BMS_isReady = bytes[i];
                int tmp = BRO_BMS_isReady & 0xff;
                String tmpStr = Integer.toHexString(tmp);

                bmsRequest_302.setBRO_BMS_isReady(tmpStr);
                i++;

                System.arraycopy(bytes, i, intByte, 0, 4);
                bmsRequest_302.setBCL_voltage_need(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;

                System.arraycopy(bytes, i, intByte, 0, 4);
                bmsRequest_302.setBCL_current_need(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;

                byte BCL_charge_mode = bytes[i];
                bmsRequest_302.setBCL_charge_mode(EncodeUtil.byteToValue(BCL_charge_mode));
                i++;

                System.arraycopy(bytes, i, intByte, 0, 4);
                bmsRequest_302.setBCS_test_voltage(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;

                System.arraycopy(bytes, i, intByte, 0, 4);
                bmsRequest_302.setBCS_test_current(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;

                System.arraycopy(bytes, i, intByte, 0, 4);
                bmsRequest_302.setBCS_max_single_voltage(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;

                byte BCS_max_single_no = bytes[i];
                bmsRequest_302.setBCS_max_single_no(EncodeUtil.byteToValue(BCS_max_single_no));
                i++;

                System.arraycopy(bytes, i, shortByte, 0, 2);
                bmsRequest_302.setBCS_current_soc(String.valueOf(EncodeUtil.byteToShort(shortByte)));
                i += 2;

                System.arraycopy(bytes, i, intByte, 0, 4);
                bmsRequest_302.setLast_charge_time(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;

                byte BSM_single_no = bytes[i];
                bmsRequest_302.setBSM_single_no(EncodeUtil.byte2UnsignValue(BSM_single_no));
                i++;

                byte BSM_max_temperature = bytes[i];
                bmsRequest_302.setBSM_max_temperature(EncodeUtil.byte2UnsignValue(BSM_max_temperature));
                i++;

                byte BSM_max_temperature_check_no = bytes[i];
                bmsRequest_302.setBSM_max_temperature_check_no(EncodeUtil.byte2UnsignValue(BSM_max_temperature_check_no));
                i++;

                byte BSM_min_temperature = bytes[i];
                bmsRequest_302.setBSM_min_temperature(EncodeUtil.byte2UnsignValue(BSM_min_temperature));
                i++;

                byte BSM_min_temperature_check_no = bytes[i];
                bmsRequest_302.setBSM_min_temperature(EncodeUtil.byte2UnsignValue(BSM_min_temperature_check_no));
                i++;

                byte BSM_voltage_too_high_or_too_low = bytes[i];
                bmsRequest_302.setBSM_voltage_too_high_or_too_low(EncodeUtil.byteToValue(BSM_voltage_too_high_or_too_low));
                i++;

                byte BSM_car_battery_soc_too_high_or_too_low = bytes[i];
                bmsRequest_302.setBSM_car_battery_soc_too_high_or_too_low(EncodeUtil.byteToValue(BSM_car_battery_soc_too_high_or_too_low));
                i++;

                byte BSM_car_battery_charge_over_current = bytes[i];
                bmsRequest_302.setBSM_car_battery_charge_over_current(EncodeUtil.byteToValue(BSM_car_battery_charge_over_current));
                i++;

                byte BSM_battery_temperature_too_high = bytes[i];
                bmsRequest_302.setBSM_battery_temperature_too_high(EncodeUtil.byteToValue(BSM_battery_temperature_too_high));
                i++;

                byte BSM_battery_insulation_state = bytes[i];
                bmsRequest_302.setBSM_battery_insulation_state(EncodeUtil.byteToValue(BSM_battery_insulation_state));
                i++;

                byte BSM_battery_connect_state = bytes[i];
                bmsRequest_302.setBSM_battery_connect_state(EncodeUtil.byteToValue(BSM_battery_connect_state));
                i++;

                byte BSM_allow_charge = bytes[i];
                bmsRequest_302.setBSM_allow_charge(EncodeUtil.byteToValue(BSM_allow_charge));
                i++;

                byte BST_BMS_soc_target = bytes[i];
                bmsRequest_302.setBST_BMS_soc_target(EncodeUtil.byteToValue(BST_BMS_soc_target));
                i++;

                byte BST_BMS_voltage_target = bytes[i];
                bmsRequest_302.setBST_BMS_voltage_target(EncodeUtil.byteToValue(BST_BMS_voltage_target));
                i++;

                byte BST_single_voltage_target = bytes[i];
                bmsRequest_302.setBST_single_voltage_target(EncodeUtil.byteToValue(BST_single_voltage_target));
                i++;

                byte BST_finish = bytes[i];
                bmsRequest_302.setBST_finish(EncodeUtil.byteToValue(BST_finish));
                i++;

                byte BST_isolation_error = bytes[i];
                bmsRequest_302.setBST_isolation_error(EncodeUtil.byteToValue(BST_isolation_error));
                i++;

                byte BST_connect_over_temperature = bytes[i];
                bmsRequest_302.setBST_connect_over_temperature(EncodeUtil.byteToValue(BST_connect_over_temperature));
                i++;

                byte BST_BMS_over_temperature = bytes[i];
                bmsRequest_302.setBST_BMS_over_temperature(EncodeUtil.byteToValue(BST_BMS_over_temperature));
                i++;

                byte BST_connect_error = bytes[i];
                bmsRequest_302.setBST_connect_error(EncodeUtil.byteToValue(BST_connect_error));
                i++;

                byte BST_battery_over_temperature = bytes[i];
                bmsRequest_302.setBST_battery_over_temperature(EncodeUtil.byteToValue(BST_battery_over_temperature));
                i++;

                byte BST_high_voltage_relay_error = bytes[i];
                bmsRequest_302.setBST_high_voltage_relay_error(EncodeUtil.byteToValue(BST_high_voltage_relay_error));
                i++;

                byte BST_point2_test_error = bytes[i];
                bmsRequest_302.setBST_point2_test_error(EncodeUtil.byteToValue(BST_point2_test_error));
                i++;

                byte BST_other_error = bytes[i];
                bmsRequest_302.setBST_other_error(EncodeUtil.byteToValue(BST_other_error));
                i++;

                byte BST_current_too_high = bytes[i];
                bmsRequest_302.setBST_current_too_high(EncodeUtil.byteToValue(BST_current_too_high));
                i++;

                byte BST_voltage_too_high = bytes[i];
                bmsRequest_302.setBST_voltage_too_high(EncodeUtil.byteToValue(BST_voltage_too_high));
                i++;

                System.arraycopy(bytes, i, shortByte, 0, 2);
                bmsRequest_302.setBST_stop_soc(String.valueOf(EncodeUtil.byteToShort(shortByte)));
                i += 2;

                System.arraycopy(bytes, i, intByte, 0, 4);
                bmsRequest_302.setBSD_battery_low_voltage(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;

                System.arraycopy(bytes, i, intByte, 0, 4);
                bmsRequest_302.setBSD_battery_high_voltage(String.valueOf(EncodeUtil.byteToInt(intByte)));
                i += 4;

                byte BSD_battery_low_temperature = bytes[i];
                bmsRequest_302.setBSD_battery_low_temperature(EncodeUtil.byteToValue(BSD_battery_low_temperature));
                i++;

                byte BSD_battery_high_temperature = bytes[i];
                bmsRequest_302.setBSD_battery_high_temperature(EncodeUtil.byteToValue(BSD_battery_high_temperature));
                i++;

                byte error_68 = bytes[i];
                bmsRequest_302.setError_68(EncodeUtil.byteToValue(error_68));
                i++;

                byte error_69 = bytes[i];
                bmsRequest_302.setError_69(EncodeUtil.byteToValue(error_69));
                i++;

                byte error_70 = bytes[i];
                bmsRequest_302.setError_70(EncodeUtil.byteToValue(error_70));
                i++;

                byte error_71 = bytes[i];
                bmsRequest_302.setError_71(EncodeUtil.byteToValue(error_71));
                i++;

                byte error_72 = bytes[i];
                bmsRequest_302.setError_72(EncodeUtil.byteToValue(error_72));
                i++;

                byte error_73 = bytes[i];
                bmsRequest_302.setError_73(EncodeUtil.byteToValue(error_73));
                i++;

                byte error_74 = bytes[i];
                bmsRequest_302.setError_74(EncodeUtil.byteToValue(error_74));
                i++;

                byte error_75 = bytes[i];
                bmsRequest_302.setError_75(EncodeUtil.byteToValue(error_75));
                i++;

                return bmsRequest_302;
            }
            return bmsRequest_302;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("CMD_parser_402 parse error{}", e);
        }
        return null;
    }

}
