package com.t13max.game.entity;

import com.t13max.persist.data.entity.EntityData;
import com.t13max.persist.data.entity.PlayerData;

/**
 * 玩家实体
 *
 * @author: t13max
 * @since: 16:00 2024/7/25
 */
public class PlayerEntity extends LivingEntity {

    private final PlayerData playerData;

    public PlayerEntity(PlayerData playerData) {
        this.id = playerData.getId();
        this.playerData = playerData;
    }

    @Override
    public EntityData getEntityData() {
        return playerData;
    }
}
