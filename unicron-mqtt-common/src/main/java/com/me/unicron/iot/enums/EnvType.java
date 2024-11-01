package com.me.unicron.iot.enums;

public enum EnvType {
	ENV_DEV(6, "开发测试"),
	ENV_ONLINE(8, "生产环境"),
    ;
    
    private final int value;
    private final String name;
    
    EnvType(int value, String name) {
        this.value = value;
        this.name = name;
    }
    public int getValue() {
        return this.value;
    }
    public String getName() {
        return this.name;
    }

    public static EnvType valueOf(int value) {
    	EnvType[] types = EnvType.values();
        for (EnvType type:types) {
            if (value == type.getValue()) {
                return type;
            }
        }
        return null;
    }
}
