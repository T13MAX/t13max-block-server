package com.t13max.game.entity;

import com.t13max.game.entity.module.EntityModules;
import com.t13max.game.world.World;
import lombok.Getter;

/**
 * 有生命的实体
 *
 * @author: t13max
 * @since: 15:57 2024/7/25
 */

@Getter
public abstract class LivingEntity extends Entity {

    //模块合集
    protected final EntityModules entityModules;

    protected LivingEntity() {
        //提前暴露当前对象 但是只赋值 问题不大
        this.entityModules = new EntityModules(this);
    }


    @Override
    public void tick() {
        super.tick();
        this.entityModules.tick();
    }

    @Override
    protected void enterWorldAfter() {
        super.enterWorldAfter();
        this.entityModules.enterWorld();
    }

    @Override
    protected void leaveWorldAfter() {
        super.leaveWorldAfter();
        this.entityModules.leaveWorld();
    }
}
