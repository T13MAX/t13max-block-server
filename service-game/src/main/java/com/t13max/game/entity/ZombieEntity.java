package com.t13max.game.entity;

import com.t13max.persist.data.entity.EntityData;
import com.t13max.persist.data.entity.ZombieData;

/**
 * 僵尸
 *
 * @author: t13max
 * @since: 14:15 2024/7/26
 */
public class ZombieEntity extends MobEntity {

    private final ZombieData zombieData;

    public ZombieEntity(ZombieData zombieData) {
        this.id = zombieData.getId();
        this.zombieData = zombieData;
    }

    @Override
    public EntityData getEntityData() {
        return zombieData;
    }
}
