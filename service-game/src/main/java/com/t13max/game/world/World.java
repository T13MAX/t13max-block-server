package com.t13max.game.world;

import com.t13max.game.consts.Const;
import com.t13max.game.util.Log;
import com.t13max.game.world.module.WorldModules;
import com.t13max.persist.data.world.WorldData;
import com.t13max.util.TimeUtil;
import game.enums.WorldEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 世界
 *
 * @Author: t13max
 * @Since: 21:08 2024/7/14
 */
@Getter
@Setter
public class World {

    //世界模块集合
    private WorldModules worldModules;
    //任务队列
    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    //当前世界的执行线程
    private final ExecutorService worldExecutor = Executors.newSingleThreadExecutor();
    //停止标记
    private volatile boolean stop = false;
    //记录上次tick时间
    private long lastTickMills;
    //世界持久化数据
    private WorldData worldData;


    public World() {
        worldModules = new WorldModules(this);
    }

    public World(WorldData worldData) {
        this();
        this.worldData = worldData;
    }

    /**
     * 获取世界名字 唯一
     *
     * @Author t13max
     * @Date 17:31 2024/8/2
     */
    public String getName() {
        return worldData.getName();
    }

    /**
     * 加载世界
     *
     * @Author t13max
     * @Date 11:16 2024/7/15
     */
    public void load() {
        //worldEnum = WorldEnum.DEF_WORLD;
    }

    /**
     * 世界开始运行
     *
     * @Author t13max
     * @Date 10:39 2024/7/15
     */
    public void runWorld() {
        this.worldExecutor.execute(this::run);
    }

    /**
     * 执行tick
     *
     * @Author t13max
     * @Date 11:30 2024/7/15
     */
    public void tick() {

        this.worldModules.tick();
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
     * 死循环执行 有任务就执行
     *
     * @Author t13max
     * @Date 16:58 2024/7/25
     */
    private void run() {

        while (!stop) {

            long beginMills = System.currentTimeMillis();

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
                Log.game.error("{}停不下来啦! ", this.getName());
                worldExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 检测是否卡死
     *
     * @Author t13max
     * @Date 17:00 2024/7/25
     */
    public void checkStuck() {
        long lastTickMills = this.lastTickMills;
        if (TimeUtil.nowMills() - lastTickMills > 1000) {
            Log.game.error("延迟过高! world={}", this.getName());
            //后续增加其他处理
        }
    }
}
