package com.t13max.game.entity;

import com.t13max.game.entity.module.EntityModules;
import com.t13max.persist.data.entity.EntityData;

/**
 * @author: t13max
 * @since: 15:59 2024/7/25
 */
public class ItemEntity extends Entity{

    @Override
    public EntityModules getEntityModules() {
        return null;
    }

    @Override
    public EntityData getEntityData() {
        return null;
    }

    @Override
    public boolean isMovable() {
        return false;
    }

    @Override
    public boolean isDead() {
        return false;
    }

}
