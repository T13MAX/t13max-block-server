package com.t13max.util;

import lombok.experimental.UtilityClass;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: t13max
 * @since: 11:10 2024/4/16
 */
@UtilityClass
public class TempIdUtil {

    private final AtomicLong TEMP_ID = new AtomicLong(1000);

    /**
     * 获取下一个临时id
     *
     * @Author t13max
     * @Date 16:31 2024/5/28
     */
    public long getNextTempId() {
        return TEMP_ID.incrementAndGet();
    }

}
