package com.t13max.game.event;

import com.t13max.common.event.IEvent;
import com.t13max.common.event.IEventEnum;
import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.combat.CombatContextResult;
import lombok.Getter;

/**
 * 实体被攻击
 *
 * @author t13max
 * @since 15:40 2024/12/11
 */
@Getter
public class OnEntityBeHit implements IEvent {
    private final IEntity owner;
    private final IEntity caster;
    private final CombatContextResult result;

    public OnEntityBeHit(IEntity owner, IEntity caster, CombatContextResult result) {
        this.owner = owner;
        this.caster = caster;
        this.result = result;
    }

    @Override
    public IEventEnum getEventEnum() {
        return WorldEventEnum.OnEntityBeHit;
    }
}
