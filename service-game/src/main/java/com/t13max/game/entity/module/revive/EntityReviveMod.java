package com.t13max.game.entity.module.revive;

import com.t13max.common.event.GameEventBus;
import com.t13max.game.consts.UnitBits;
import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.PlayerEntity;
import com.t13max.game.entity.module.EntityModule;
import com.t13max.game.event.OnPlayerEntityRelive;
import com.t13max.game.util.TickTimer;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 实体复活模块
 *
 * @author t13max
 * @since 11:00 2024/12/12
 */
@Getter
public class EntityReviveMod extends EntityModule {

    //上次复活时间
    private long reviveStamp;

    //复活计时器
    private final TickTimer objReviveTimer = new TickTimer();

    public EntityReviveMod(IEntity owner) {
        super(owner);
    }

    @Override
    public int pulse(long now) {
        if (owner.getWorld().canRevive() && objReviveTimer.isOnce(now)) {
            int result = checkAndRevive();
            objReviveTimer.stop();
            if (result != 0) {

            }
        }
        return 0;
    }

    /**
     * 登录重连初始化数据
     *
     * @Author t13max
     * @Date 11:02 2024/12/12
     */
    public void initial() {

    }

    /**
     * 检查复活条件，复活单位
     *
     * @Author t13max
     * @Date 11:02 2024/12/12
     */
    public int checkAndRevive() {
        if (!owner.getWorld().canRevive()) {
            return -1;
        }
        if (owner.getBit(UnitBits.REVIVING)) {
            return -1;
        }

        owner.setBit(UnitBits.REVIVING, true);
        //发送消息
        return 0;
    }

    /**
     * 复活单位
     *
     * @Author t13max
     * @Date 11:07 2024/12/12
     */
    public void doRevive() {
        this.reviveStamp = owner.getTime();
        // 停掉计时器
        if (objReviveTimer.isStarted()) {
            objReviveTimer.stop();
        }

        //调整朝向位置

        // 满血
        owner.getEntityModules().getAttrMod().fullHP(false);

        // 清除死亡标记
        owner.setBit(UnitBits.DEAD, false);
        // 清除复活中标记
        owner.setBit(UnitBits.REVIVING, false);

        owner.onRevive();

        PlayerEntity playerEntity = owner.queryObject(PlayerEntity.class);
        if (playerEntity != null) {
            // 序列化一下数据
            playerEntity.serializeRoleInfo();
        }

        // 广播消息


        if (playerEntity == null) {
            return;
        }

        // 复活事件
        GameEventBus.inst().postEvent(new OnPlayerEntityRelive(playerEntity));
    }

    /**
     * 重置死亡倒计时 重新发送死亡消息
     *
     * @Author t13max
     * @Date 11:10 2024/12/12
     */
    public void deathReset() {
        objReviveTimer.stop();

        onUnitDeath(null, false);
    }

    /**
     * 单位死亡逻辑
     *
     * @Author t13max
     * @Date 11:11 2024/12/12
     */
    public void onUnitDeath(IEntity killer, boolean isEnemy) {

        if (killer == null) {
            doRevive();
            return;
        }
        // 已经进行倒计时
        if (objReviveTimer.isStarted()) {
            return;
        }

        objReviveTimer.start(TimeUnit.SECONDS.toMillis(60));

        PlayerEntity playerEntity = owner.queryObject(PlayerEntity.class);
        if (playerEntity != null) {
            playerEntity.setBit(UnitBits.DEAD, true);
        }
        //发消息
    }

}
