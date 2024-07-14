package com.t13max.game.world;

import com.t13max.game.manager.ManagerBase;
import game.enums.WorldEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * 世界管理器
 *
 * @Author: t13max
 * @Since: 21:21 2024/7/14
 */
public class WorldManager extends ManagerBase {

    //world缓存 要不要线程安全?
    private Map<String, BlockWorld> worldMap = new HashMap<>();

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
        for (BlockWorld blockWorld : worldMap.values()) {
            blockWorld.onShutdown();
        }
    }

    @Override
    public void init() {
        //目前只初始化一个主世界

    }

    /**
     * 根据名字获取世界
     *
     * @Author: t13max
     * @Since: 21:25 2024/7/14
     */
    public BlockWorld getWorld(String name) {
        return this.worldMap.get(name);
    }

    /**
     * 根据枚举名获取世界
     *
     * @Author: t13max
     * @Since: 21:25 2024/7/14
     */
    public BlockWorld getWorld(WorldEnum worldEnum) {
        return this.worldMap.get(worldEnum.name());
    }

}
