package com.t13max.game.event;

import com.t13max.common.event.IEvent;
import com.t13max.common.event.IEventEnum;
import com.t13max.game.entity.IEntity;

/**
 * @author t13max
 * @since 15:54 2024/12/11
 */
public class OnEntityCombatStatusChange implements IEvent {

    private final IEntity entity;

    private final boolean inCombat;

    public OnEntityCombatStatusChange(IEntity entity, boolean inCombat) {
        this.entity = entity;
        this.inCombat = inCombat;
    }

    @Override
    public IEventEnum getEventEnum() {
        return WorldEventEnum.OnEntityCombatStatusChange;
    }
}
