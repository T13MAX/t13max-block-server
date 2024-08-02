package com.t13max.game.world.module;

import com.t13max.game.entity.IEntity;
import com.t13max.game.world.World;

/**
 * AOI模块
 *
 * @author: t13max
 * @since: 17:19 2024/7/25
 */
public class WorldAoiMod extends WorldModule {

    public WorldAoiMod(World world) {
        super(world);
    }

    /**
     * 实体进入区块 需要
     *
     * @Author t13max
     * @Date 17:07 2024/8/2
     */
    @Override
    public void enterWorld(IEntity entity) {
        super.enterWorld(entity);
    }

    @Override
    public void leaveWorld(IEntity entity) {
        super.leaveWorld(entity);
    }
}
