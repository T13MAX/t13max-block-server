package com.t13max.game.world;

import com.t13max.common.manager.ManagerBase;
import com.t13max.game.consts.Const;
import com.t13max.game.world.task.TickTask;
import com.t13max.persist.manager.DataManager;
import com.t13max.persist.data.world.WorldData;
import game.enums.WorldEnum;
import org.bson.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 世界管理器
 *
 * @Author: t13max
 * @Since: 21:21 2024/7/14
 */
public class WorldManager extends ManagerBase {

    private ScheduledExecutorService worldManagerExecutor = Executors.newSingleThreadScheduledExecutor();

    //world缓存 要不要线程安全?
    private Map<String, World> worldMap = new HashMap<>();

    public static WorldManager inst() {
        return ManagerBase.inst(WorldManager.class);
    }

    /**
     * 停止所有的world
     *
     * @Author: t13max
     * @Since: 21:22 2024/7/14
     */
    @Override
    protected void onShutdown() {
        for (World world : worldMap.values()) {
            world.onShutdown();
        }
    }

    @Override
    public void init() {
        //目前只初始化一个主世界
        worldManagerExecutor.scheduleAtFixedRate(this::schedule, Const.SCHEDULE_INIT_DELAY, Const.TICK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * 定期执行
     *
     * @Author t13max
     * @Date 11:26 2024/7/15
     */
    private void schedule() {
        for (World world : worldMap.values()) {
            //检查卡死
            world.checkStuck();
            //添加tick任务
            world.addTask(new TickTask(world));
        }
    }

    /**
     * 根据名字获取世界
     *
     * @Author: t13max
     * @Since: 21:25 2024/7/14
     */
    public World getWorld(String name) {
        return this.worldMap.get(name);
    }

    /**
     * 根据枚举名获取世界
     *
     * @Author: t13max
     * @Since: 21:25 2024/7/14
     */
    public World getWorld(WorldEnum worldEnum) {
        return this.worldMap.get(worldEnum.name());
    }

    /**
     * 创建一个世界
     *
     * @Author t13max
     * @Date 15:13 2024/8/2
     */
    public World createWorld() {
        return null;
    }

    /**
     * 加载一个世界
     *
     * @Author t13max
     * @Date 15:31 2024/8/2
     */
    public World loadWorld(WorldEnum worldEnum) {
        return loadWorld(worldEnum.name());
    }

    public World loadWorld(String name) {
        WorldData worldData = DataManager.inst().findById(WorldData.class, new Document("_id", name));
        return new World(worldData);
    }

}
