package com.me.unicron.iot.exception;

/**
 * 连接异常
 *
 * @author lianyadong
 * @create 2023-11-23 14:34
 **/
public class ConnectionException extends  RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7801490315758950378L;

	public ConnectionException(String message) {
        super(message);
    }
}
