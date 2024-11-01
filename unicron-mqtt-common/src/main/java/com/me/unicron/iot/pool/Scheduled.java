package com.me.unicron.iot.pool;

import java.util.concurrent.ScheduledFuture;

/**
 * 接口
 *
 * @author lianyadong
 * @create 2023-12-14 10:47
 **/
@FunctionalInterface
public interface Scheduled {

    ScheduledFuture<?> submit(Runnable runnable);
}
