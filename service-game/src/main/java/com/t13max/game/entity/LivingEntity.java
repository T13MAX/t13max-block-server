package com.t13max.game.entity;

import com.t13max.game.entity.module.EntityModules;
import lombok.Getter;

/**
 * 有生命的实体
 *
 * @author: t13max
 * @since: 15:57 2024/7/25
 */

@Getter
public abstract class LivingEntity extends Entity {

    //模块合集
    private EntityModules entityModules;


}
