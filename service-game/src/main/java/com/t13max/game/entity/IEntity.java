package com.t13max.game.entity;

import com.t13max.game.entity.module.EntityModules;
import com.t13max.game.pos.Position;

/**
 * 实体顶级接口
 *
 * @author: t13max
 * @since: 11:12 2024/7/15
 */
public interface IEntity extends EntityQuery {

    //id
    long getId();

    //获取模块合集 要不要提供这个接口呢? 毕竟有些实体没模块
    EntityModules getEntityModules();

    //实体tick
    void tick();

    //获取当前实体所在位置
    Position getPosition();
}
