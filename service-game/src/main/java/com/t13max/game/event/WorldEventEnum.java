package com.t13max.game.event;

import com.t13max.common.event.IEventEnum;

/**
 * 事件枚举
 * 理论上 他应该下划线链接 但是这样写省事啊!
 *
 * @author t13max
 * @since 15:35 2024/12/11
 */
public enum WorldEventEnum implements IEventEnum {

    OnPlayerHit,
    OnEntityBeHit,
    OnPlayerKill,
    OnEntityCombatStatusChange,
    OnPlayerEntityRelive,
    ;
}
