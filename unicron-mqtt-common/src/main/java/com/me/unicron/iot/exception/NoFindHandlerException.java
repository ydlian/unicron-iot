package com.me.unicron.iot.exception;

/**
 * @author lianyadong
 * @create 2023-01-03 16:25
 **/
public class NoFindHandlerException extends  RuntimeException{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 5292494164299065953L;

	public NoFindHandlerException(String message) {
        super(message);
    }
}
