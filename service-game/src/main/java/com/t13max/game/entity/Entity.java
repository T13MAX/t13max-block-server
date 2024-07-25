package com.t13max.game.entity;

import com.t13max.game.pos.Position;
import com.t13max.game.world.World;
import lombok.Getter;

/**
 * 实体 顶级父类
 *
 * @author: t13max
 * @since: 15:53 2024/7/25
 */
@Getter
public abstract class Entity implements IEntity {

    private long id;

    //所在世界
    private World world;

    //所在位置
    private Position position;

    public Entity() {
    }

    public Entity(long id) {
        this.id = id;
    }

    /**
     * tick!
     *
     * @Author t13max
     * @Date 16:08 2024/7/25
     */
    @Override
    public void tick() {

    }

    public void enterWorld(World world) {

        if (this.world != null) {
            //错误处理
            return;
        }

        enterWorldBefore();

        this.world = world;

        enterWorldAfter();
    }

    public void leaveWorld(World world) {

        if (this.world == null) {
            return;
        }

        leaveWorldBefore();

        this.world = null;

        leaveWorldAfter();
    }

    protected void enterWorldBefore() {

    }

    protected void enterWorldAfter() {

    }

    protected void leaveWorldBefore() {

    }

    protected void leaveWorldAfter() {

    }

}
