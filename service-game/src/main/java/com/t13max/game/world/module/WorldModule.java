package com.t13max.game.world.module;

import com.t13max.game.entity.IEntity;
import com.t13max.game.world.World;
import lombok.Getter;

/**
 * 世界模块基类
 *
 * @author: t13max
 * @since: 15:47 2024/7/25
 */
public abstract class WorldModule {

    @Getter
    protected World owner;

    public WorldModule(World world) {
        this.owner = world;
        initData();
    }

    public void tick() {

    }

    /**
     * 模块初始化
     *
     * @Author t13max
     * @Date 16:35 2024/7/25
     */
    protected void initData() {

    }

    public void enterWorld(IEntity entity) {


    }

    public void leaveWorld(IEntity entity) {


    }

}
