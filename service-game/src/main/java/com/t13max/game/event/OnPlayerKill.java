package com.t13max.game.event;

import com.t13max.common.event.IEvent;
import com.t13max.common.event.IEventEnum;
import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.PlayerEntity;
import lombok.Getter;

/**
 * @author t13max
 * @since 15:45 2024/12/11
 */
@Getter
public class OnPlayerKill implements IEvent {

    private final PlayerEntity playerEntity;
    private final IEntity realKiller;
    private final IEntity target;

    public OnPlayerKill(PlayerEntity playerEntity, IEntity realKiller, IEntity target) {
        this.playerEntity = playerEntity;
        this.realKiller = realKiller;
        this.target = target;
    }

    @Override
    public IEventEnum getEventEnum() {
        return WorldEventEnum.OnPlayerKill;
    }
}
