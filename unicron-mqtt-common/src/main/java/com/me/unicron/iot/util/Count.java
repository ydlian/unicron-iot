package com.me.unicron.iot.util;

import java.util.concurrent.atomic.AtomicLong;

import com.me.unicron.EncodeUtil;

/**
 * @author lianyadong
 * func：线程安全,以原子方式将当前值加1
 */

public class Count {
	private static AtomicLong counter = new AtomicLong(System.currentTimeMillis());
	//private static AtomicLong counterAtomicLong;
	public static short getShortCount2() {
		// 线程安全,以原子方式将当前值加1
		short num = (short) (counter.getAndIncrement() % Short.MAX_VALUE);
		return num;
	}
	public static short getShortCount() {
		short num = (short)(System.nanoTime() & 0x000000000000ffffL);
		return num;
	}
	
	public static long getLongCount() {
		
		return System.nanoTime();
	}
	
	public static int getIntCount() {
		int num = (int)(System.nanoTime() & 0x00000000ffffffffL);
		return num;
	}
	public static void main(String[] args) {
		for(int i=0;i<Short.MAX_VALUE+100;i++){
			byte[] index = EncodeUtil.intToByte(Count.getIntCount());
			EncodeUtil.printHex(index);
		}
		
	}
}