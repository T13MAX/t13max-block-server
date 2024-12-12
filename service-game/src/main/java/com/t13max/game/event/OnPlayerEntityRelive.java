package com.t13max.game.event;

import com.t13max.common.event.IEvent;
import com.t13max.common.event.IEventEnum;
import com.t13max.game.entity.PlayerEntity;
import lombok.Data;

/**
 * @author t13max
 * @since 11:10 2024/12/12
 */
@Data
public class OnPlayerEntityRelive implements IEvent {

    private final PlayerEntity playerEntity;

    public OnPlayerEntityRelive(PlayerEntity playerEntity) {
        this.playerEntity = playerEntity;
    }

    @Override
    public IEventEnum getEventEnum() {
        return WorldEventEnum.OnPlayerEntityRelive;
    }
}
