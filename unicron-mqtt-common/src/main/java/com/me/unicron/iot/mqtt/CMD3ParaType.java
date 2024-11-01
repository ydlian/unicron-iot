package com.me.unicron.iot.mqtt;


/**
 * @author lianyadong
 * CMD3ParaType Types.
 */
public enum CMD3ParaType {
	STATION_ID(1),//充电桩编码
    STDTIME(2),//标准时钟时间
    ADMIN_PASS(3),//管理员密码（保留）
    OP_PASS(4),//操作员密码（保留）
    MAC(5),//MAC地址
    XB_ID(6),//箱变编码
    GUN_INDEX(7),//充电枪口
    GUN_QRCODE(8),//充电枪二维码
    DISPLAY_TEXT(9),//显示屏底部文字显示
    RESERVED1(10),//保留
    RESERVED2(11),//保留
    PAY_QRCODE(12),
    CENTER_ADDRESS(13),
    CENTER_PORT(14),
    TOP_LOGO(15),
    LEFT_QR_CODE(16),
    RIGHT_QR_CODE(17),
    LEFT_QR(18),
    RIGHT_QR(19),
    ;//用户支付二维码（保留）
    
    private final int value;

    CMD3ParaType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static CMD3ParaType valueOf(int type) {
        for (CMD3ParaType t : values()) {
            if (t.value == type) {
                return t;
            }
        }
        throw new IllegalArgumentException("unknown CMD3ParaType type: " + type);
    }
}