package com.t13max.game.event;

import com.t13max.common.event.IEvent;
import com.t13max.common.event.IEventEnum;
import com.t13max.game.entity.IEntity;
import lombok.Getter;

/**
 * @author t13max
 * @since 15:34 2024/12/11
 */
@Getter
public class OnPlayerHit implements IEvent {

    private final IEntity target;

    public OnPlayerHit(IEntity target) {
        this.target = target;
    }

    @Override
    public IEventEnum getEventEnum() {
        return WorldEventEnum.OnPlayerHit;
    }
}
