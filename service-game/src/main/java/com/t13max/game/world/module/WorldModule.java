package com.t13max.game.world.module;

import com.t13max.game.entity.IEntity;
import com.t13max.game.pos.Position;
import com.t13max.game.world.World;
import lombok.Getter;

/**
 * 世界模块基类
 *
 * @author: t13max
 * @since: 15:47 2024/7/25
 */
@Getter
public abstract class WorldModule {

    //所属世界
    protected World owner;

    public WorldModule(World world) {
        this.owner = world;
        initData();
    }

    /**
     * tick世界模块
     *
     * @Author t13max
     * @Date 14:03 2024/8/14
     */
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

    /**
     * 实体进入世界
     * 上线 通过传送门进入某个世界
     *
     * @Author t13max
     * @Date 14:03 2024/8/14
     */
    public void enterWorld(IEntity entity) {

    }

    /**
     * 实体离开世界
     * 下线 通过传送门离开某个世界
     *
     * @Author t13max
     * @Date 14:03 2024/8/14
     */
    public void leaveWorld(IEntity entity) {

    }

    /**
     * 实体移动
     *
     * @Author t13max
     * @Date 14:10 2024/8/14
     */
    public void onEntityMoved(IEntity entity, Position oldPos, Position newPos) {

    }

}
