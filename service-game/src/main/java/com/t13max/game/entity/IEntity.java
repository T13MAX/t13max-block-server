package com.t13max.game.entity;

import com.google.protobuf.Message;
import com.t13max.game.entity.module.EntityModules;
import com.t13max.game.pos.Position;
import com.t13max.game.pos.Vector3D;
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
    void pulse();

    //获取当前实体所在位置
    Vector3D getPosition();

    Position getDataPos();

    EntityData getEntityData();

    void pulsePerSec();

    void enterWorld(World world);

    void leaveWorld(World world);

    void changePosition(Vector3D newPos);

    void onChunkChanged(Chunk leaveChunk, Chunk enterChunk);

    void moveTowards(IEntity other, double distance);

    boolean isMovable();

    boolean getBit(int index);

    void setBit(int index, boolean apply);

    World getWorld();

    float getModelRadius();

    void changeSpeed(float selfSpeed, float targetSpeed);

    float getSpeed();

    long getTime();

    void changeDirection(Vector3D direction);

    Vector3D getDirection();

    void sendMsgToView(Message message, long exceptId);

    void sendMsg(Message message);

    void onDeath(IEntity caster);

    void onRevive();

    boolean isDead();

    boolean isInWorld();

    void setHp(float curr);

    void serializeRoleInfo();

    IndirectObject<IEntity> getIndirectObject();

    int distance(Vector3D position);

    boolean isVisibleToOthers(IEntity other);

    Vector3D getNormalizeDir();

    void killMySelf();
}
