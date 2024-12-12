package com.t13max.game.entity;

import com.t13max.game.consts.UnitBits;
import com.t13max.game.entity.module.EntityModules;
import com.t13max.util.TimeUtil;
import lombok.Getter;

import java.util.BitSet;

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
    public void pulse() {
        super.pulse();
        long currentTimeMillis = TimeUtil.nowMills();
        this.entityModules.pulse(currentTimeMillis);
    }

    @Override
    public void pulsePerSec() {
        super.pulsePerSec();
        long currentTimeMillis = TimeUtil.nowMills();
        this.entityModules.pulsePerSec(currentTimeMillis);
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

    @Override
    public boolean isDead() {
        return false;
    }
}
