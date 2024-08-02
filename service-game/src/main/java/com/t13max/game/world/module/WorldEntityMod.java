package com.t13max.game.world.module;

import com.t13max.game.entity.IEntity;
import com.t13max.game.world.World;

import java.util.HashMap;
import java.util.Map;

/**
 * 实体模块
 *
 * @author: t13max
 * @since: 16:37 2024/7/25
 */
public class WorldEntityMod extends WorldModule {

    //这个世界的实体集合
    private final Map<Long, IEntity> entityMap = new HashMap<>();

    public WorldEntityMod(World world) {
        super(world);
    }

    /**
     * 遍历所有实体 tick
     *
     * @Author t13max
     * @Date 17:05 2024/7/25
     */
    @Override
    public void tick() {
        this.entityMap.values().forEach(IEntity::tick);
    }

    @Override
    public void enterWorld(IEntity entity) {
        this.entityMap.put(entity.getId(), entity);
    }

    @Override
    public void leaveWorld(IEntity entity) {
        this.entityMap.remove(entity.getId());
    }


}
