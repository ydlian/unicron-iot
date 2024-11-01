package com.me.unicron.iot.message.packer.impl;

import java.io.UnsupportedEncodingException;

import com.me.unicron.iot.message.packer.BaseCMD;
import org.apache.commons.lang.StringUtils;

import com.me.epower.direct.entity.downward.StationStringPara_3;
import com.me.unicron.EncodeUtil;
import com.me.unicron.date.DateUtils;
import com.me.unicron.iot.message.bean.MqttNetMsg;
import com.me.unicron.iot.message.bean.base.MqttNetMsgBase;
import com.me.unicron.iot.mqtt.CMD3ParaType;
import com.me.unicron.protocol.CharsetDef;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.math.NumberUtils;

@Slf4j
public class CMD_3 implements BaseCMD<StationStringPara_3> {

	// 默认为查询时长度
	private static final int BODY_LENGTH = 11;
	private static final int CMD_NO = 3;

	// private static short inc = 0;
	private int calcLength(CMD3ParaType start_addr_val) {
		int len = 0;
		switch (start_addr_val) {
		case STATION_ID:
			len = 32;
			break;
		case STDTIME:
			len = 8;
			break;
		case ADMIN_PASS:
			len = 8;
			break;
		case OP_PASS:
			len = 8;
			break;
		case MAC:
			len = 6;
			break;
		case XB_ID:
			len = 16;
			break;
		case GUN_INDEX:
			len = 1;
			break;
		case GUN_QRCODE:
			len = 256;
			break;
		case DISPLAY_TEXT:
			len = 128;
			break;
		case RESERVED1:
			len = 16;
			break;
		case RESERVED2:
			len = 16;
			break;
		case PAY_QRCODE:
			len = 256;
			break;

		case CENTER_ADDRESS:
			len = 128;
			break;

		case CENTER_PORT:
			len = 4;
			break;

		case TOP_LOGO:
			len = 256;
			break;

		case LEFT_QR_CODE:
			len = 128;
			break;

		case RIGHT_QR_CODE:
			len = 128;
			break;

		case LEFT_QR:
			len = 128;
			break;

		case RIGHT_QR:
			len = 128;
			break;

		default:
			break;
		}
		return len;
	}

	@Override
	// MqttNetMsg
	public byte[] getPayload(StationStringPara_3 resp, MqttNetMsgBase base) {
		int bodyLenth = BODY_LENGTH;

		String start_addr = resp.getStart_addr();
		if (StringUtils.isBlank(start_addr) || !NumberUtils.isNumber(start_addr)) {
			log.info("start_addr not set!");

		}

		String cmd_type = resp.getCmd_type();
		if (StringUtils.isBlank(cmd_type) || !NumberUtils.isNumber(cmd_type)) {
			log.info("cmd_type not set!");
		}
		short start_addr_val = (short) Integer.parseInt(start_addr);

		if (Integer.parseInt(cmd_type) == 1) {
			bodyLenth = BODY_LENGTH + calcLength(CMD3ParaType.valueOf(start_addr_val));
		}

		int totalLen = base.getLength(bodyLenth);
		byte[] data = new byte[totalLen];
		data = base.fillPayload2CmdNo(data, totalLen, (short) CMD_NO);
		int bytesOffset = base.getBytesOffset();

		/*
		 * System.arraycopy(MqttNetMsg.getHerder(), 0, data, 0, 2); byte[] length =
		 * EncodeUtil.shortToByte((short) totalLen); System.arraycopy(length, 0, data,
		 * 2, 2); byte[] ver = MqttNetMsg.getVersion(); System.arraycopy(ver, 0, data,
		 * 4, 1); byte[] index = MqttNetMsg.getIndex(); System.arraycopy(index, 0, data,
		 * 5, 1); // byte[] code=MqttNetMsg.getCmdCode();
		 * System.arraycopy(EncodeUtil.shortToByte((short) CMD_NO), 0, data, 6, 2);
		 */
		// body部分，4字节保留
		byte[] reserved = new byte[4];
		System.arraycopy(reserved, 0, data, 8 + bytesOffset, 4);
		// 命令类型
		byte[] cmd_type_byt = new byte[] { EncodeUtil.ValueToByte(cmd_type) };

		System.arraycopy(cmd_type_byt, 0, data, 12 + bytesOffset, 1);

		// 设置/查询参数启始地址
		byte[] start_addr_byt = EncodeUtil.intToByte(Integer.parseInt(resp.getStart_addr()));
		System.arraycopy(start_addr_byt, 0, data, 13 + bytesOffset, 4);

		// 设置参数字节数
		int para_byte_cnt = calcLength(CMD3ParaType.valueOf(start_addr_val));
		byte[] para_byte_size = new byte[2];
		if (Integer.parseInt(cmd_type) == 1) {
			System.arraycopy(EncodeUtil.shortToByte((short) para_byte_cnt), 0, data, 17 + bytesOffset, 2);
		} else {
			System.arraycopy(para_byte_size, 0, data, 17 + bytesOffset, 2);
		}
		// 设置数据
		if (Integer.parseInt(cmd_type) == 1) {
			byte[] bodyDataCont = new byte[para_byte_cnt];
			try {
				if ("14".equals(resp.getStart_addr())) {
					byte[] port = EncodeUtil.intToByte(Integer.parseInt(resp.getData_body()));
					System.arraycopy(port, 0, bodyDataCont, 0, 4);
				} else if ("2".equals(resp.getStart_addr())) {
					byte[] TimeByte = new byte[8];
					//byte[] TimeArray = EncodeUtil.strToBCDByte(resp.getData_body());
					if("0".equals(resp.getData_body())||StringUtils.isBlank(resp.getData_body())){
						//20231101142723
						resp.setData_body(DateUtils.getCurrentDateyyyyMMddHHmmss());
					}
					
					byte[] TimeArray = EncodeUtil.strToBCDByte(resp.getData_body());
					
					System.arraycopy(TimeArray, 0, TimeByte, 0, TimeArray.length);
					TimeByte[TimeByte.length - 1] = (byte) 0xff;
					log.info("StationStringPara_3:{},{}",resp.getEquipmentId(),EncodeUtil.printHex(TimeByte));
					System.arraycopy(TimeByte, 0, bodyDataCont, 0, TimeByte.length);
				} else {
					byte[] src = resp.getData_body().getBytes("UTF-8");
					String str = new String(src, CharsetDef.CHARSET);
					src = str.getBytes(CharsetDef.CHARSET);
					if (src.length > para_byte_cnt) {
						log.error("bodyDataCont length");
						System.arraycopy(src, 0, bodyDataCont, 0, para_byte_cnt);
					} else {
						System.arraycopy(src, 0, bodyDataCont, 0, src.length);
					}
				}

			} catch (Exception e) {
				log.error("bodyDataCont error", e);
			}
			System.arraycopy(bodyDataCont, 0, data, 19 + bytesOffset, para_byte_cnt);
		}

		byte[] checkSum = new byte[] { EncodeUtil.getChecksum(data) };
		System.arraycopy(checkSum, 0, data, base.getCodeLeftLength() + bodyLenth, 1);
		return data;
	}

	@Override
	public String pack(StationStringPara_3 resp, MqttNetMsgBase base) {
		// TODO Auto-generated method stub
		byte[] data = getPayload(resp, base);
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
		CMD_3 cmd = new CMD_3();
		StationStringPara_3 obj = new StationStringPara_3();
		obj.setCmd_type("9");
		obj.setData_body("XX出行");
				// 编码前：
		byte[] byt = cmd.getPayload(obj, new MqttNetMsg());
		log.info("编码前：");
		EncodeUtil.print(byt);
	}

	public static void main(String[] args) throws Exception {
		print();
	}

}
