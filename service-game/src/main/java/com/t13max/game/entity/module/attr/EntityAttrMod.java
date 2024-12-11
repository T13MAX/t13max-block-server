package com.t13max.game.entity.module.attr;

import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.EntityModule;

/**
 * 实体属性模块
 *
 * @author: t13max
 * @since: 17:03 2024/7/25
 */
public class EntityAttrMod extends EntityModule {

    public EntityAttrMod(IEntity owner) {
        super(owner);
    }

    public int getTotalShieldValue() {
        return 0;
    }

    public void reduceShieldValue(float damage) {

    }

    public boolean isFullHp() {
        return false;
    }

    public float getAttr(int hpRecovery) {
        return 0;
    }

    public float modHp(float value) {
        return 0;
    }

    public float getHpMax() {
        return 0;
    }
}

