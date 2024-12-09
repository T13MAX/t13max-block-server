package com.t13max.game.entity;

import com.t13max.game.entity.module.EntityModules;
import com.t13max.game.pos.Position;
import com.t13max.game.world.World;
import com.t13max.game.world.chunk.Chunk;
import com.t13max.persist.data.entity.EntityData;

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

    EntityData getEntityData();

    void enterWorld(World world);

    void leaveWorld(World world);

    void changePosition(Position newPos);

    void onChunkChanged(Chunk leaveChunk, Chunk enterChunk);

    boolean isMovable();

    World getWorld();

    float getModelRadius();
}
