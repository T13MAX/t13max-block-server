package com.t13max.game.world;

import com.t13max.util.Log;
import game.enums.WorldEnum;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 世界
 *
 * @Author: t13max
 * @Since: 21:08 2024/7/14
 */
public class BlockWorld {

    //当前世界的执行线程
    private ExecutorService worldExecutor = Executors.newSingleThreadExecutor();

    private boolean stop = false;

    private WorldEnum worldEnum = WorldEnum.DEF;


    /**
     * 被世界管理器统一调用
     *
     * @Author: t13max
     * @Since: 21:23 2024/7/14
     */
    public void onShutdown() {
        this.stop = true;
        worldExecutor.shutdown();
        try {
            boolean shutDown = worldExecutor.awaitTermination(5, TimeUnit.SECONDS);
            if (!shutDown) {
                Log.game.error("{}停不下来啦! ", worldEnum);
                worldExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
