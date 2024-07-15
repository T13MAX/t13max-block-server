package com.t13max.game.world;

import com.t13max.game.consts.Const;
import com.t13max.game.entity.IBlockEntity;
import com.t13max.game.world.chunk.BlockChunk;
import com.t13max.util.Log;
import game.enums.WorldEnum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 世界
 *
 * @Author: t13max
 * @Since: 21:08 2024/7/14
 */
@Getter
@Setter
public class BlockWorld {

    //当前世界的执行线程
    private ExecutorService worldExecutor = Executors.newSingleThreadExecutor();
    //任务队列
    private BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    //停止标记
    private boolean stop = false;
    //记录上次tick时间
    private long lastTickMills;
    //世界类型
    private WorldEnum worldEnum = WorldEnum.DEF;
    //这个世界的实体集合
    private Map<Long, IBlockEntity> entityMap = new HashMap<>();
    //区块
    private BlockChunk blockChunk;

    public BlockWorld() {

    }

    /**
     * 加载世界
     *
     * @Author t13max
     * @Date 11:16 2024/7/15
     */
    public void load() {
        worldEnum = WorldEnum.DEF;
        blockChunk = new BlockChunk();
    }

    /**
     * 世界开始运行
     *
     * @Author t13max
     * @Date 10:39 2024/7/15
     */
    public void runWorld() {
        worldExecutor.execute(this::run);
    }

    public void run() {
        while (!stop) {

            long beginMills = System.currentTimeMillis();

            tick();

            long endMills = System.currentTimeMillis();

            this.lastTickMills = endMills;

            //运行时间
            long runMills = endMills - beginMills;

            try {
                long sleepMills = Math.max(0, Const.TICK_INTERVAL - runMills);
                Thread.sleep(sleepMills);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 执行tick
     *
     * @Author t13max
     * @Date 11:30 2024/7/15
     */
    public void tick() {

        if (taskQueue.isEmpty()) {
            return;
        }

        List<Runnable> list = new LinkedList<>();
        taskQueue.drainTo(list);
        for (Runnable runnable : list) {
            try {
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
                //tick异常处理
            }
        }
    }

    /**
     * 添加任务
     *
     * @Author t13max
     * @Date 11:25 2024/7/15
     */
    public boolean addTask(Runnable task) {
        return this.taskQueue.offer(task);
    }

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
