package com.t13max.game.entity.module.shift;


import com.google.protobuf.Message;
import com.t13max.game.consts.UnitBits;
import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.EntityModule;
import com.t13max.game.entity.module.skill.MagicConst;
import com.t13max.game.pos.Vector3D;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * 非motion类位移 冲锋、跳斩 、击退 、击飞、闪现
 *
 * @Author t13max
 * @Date 11:47 2024/12/12
 */
public class EntityShiftMod extends EntityModule {

    //位移类型
    private ShiftEnum type;

    //移动总时间
    private long totalTime;
    //移动总位移
    private double totalDistance;
    //终点
    private Vector3D targetPosition;
    //移动方向
    private Vector3D targetDirection;
    //上次心跳时间
    private long lastPulseTime;
    //流逝时间
    private long elapsedTime;
    //Shift结束同步消息
    private Message finishMsg;
    //是否提前中止Shift
    private boolean midInterrupt = false;

    private Consumer<IEntity> endEffect;

    public EntityShiftMod(IEntity owner) {
        super(owner);
    }

    @Override
    public int pulse(long now) {
        if (this.targetPosition == null) {
            return 0;
        }

        long diff = now - lastPulseTime;
        elapsedTime += diff;
        this.lastPulseTime = now;

        // 位移结束
        if (elapsedTime >= totalTime) {
            onShifted(true);
        } else {
            if (type == ShiftEnum.JOG) {
                if (midInterrupt) {
                    targetPosition = owner.getPosition();
                    onShifted(false);
                } else {
                    double p = (double) diff / (double) this.totalTime * totalDistance;
                    Vector3D pos = owner.getPosition().sum(targetDirection.mul(p));
                    owner.changePosition(pos);
                }
            }
        }
        return 0;
    }

    @Override
    public void leaveWorld() {
        super.leaveWorld();
        if (targetPosition != null) {
            cleanup();
        }
    }

    /**
     * 闪现
     *
     * @Author t13max
     * @Date 11:50 2024/12/12
     */
    public boolean blink(Vector3D targetPos, long delay) {
        return startup(ShiftEnum.BLINK, targetPos, delay, null);
    }

    /**
     * 慢移
     *
     * @Author t13max
     * @Date 11:51 2024/12/12
     */
    public void jog(Vector3D targetPos, long time) {
        jog(targetPos, time, null);
    }

    public void jog(Vector3D targetPos, long time, Consumer<IEntity> endEffect) {
        startup(ShiftEnum.JOG, targetPos, time, endEffect);
    }

    private boolean startup(ShiftEnum type, Vector3D targetPos, long time, Consumer<IEntity> endEffect) {
        if (this.targetPosition != null || owner.getPosition().equals(targetPos)) {
            return false;
        }
        this.type = type;
        this.totalDistance = targetPos.distance(owner.getPosition());
        this.targetDirection = targetPos.sub(owner.getPosition()).normalize();
        this.targetPosition = targetPos;
        this.totalTime = time;
        this.lastPulseTime = owner.getTime();
        this.midInterrupt = false;
        this.endEffect = endEffect;

        owner.setBit(UnitBits.SHIFTING, true);

        return true;
    }

    public void cleanup() {
        this.targetPosition = null;
        this.elapsedTime = 0L;
        this.lastPulseTime = 0L;
        this.finishMsg = null;
        this.midInterrupt = false;
        this.type = null;
        this.endEffect = null;

        if (owner.getBit(UnitBits.SHIFTING)) {
            owner.setBit(UnitBits.SHIFTING, false);
        }
    }

    private void onShifted(boolean finished) {
        owner.changePosition(targetPosition);

        if (finishMsg != null) {
            owner.sendMsgToView(finishMsg, owner.getId());
        }
        if (finished && endEffect != null) {
            endEffect.accept(owner);
        }

        this.cleanup();
    }

    /**
     * 提前中断Shift位移
     *
     * @Author t13max
     * @Date 13:44 2024/12/12
     */
    public boolean interruptMid() {
        // 是否在进行状态
        if (!owner.getBit(UnitBits.SHIFTING)) {
            return false;
        }

        midInterrupt = true;
        return true;
    }

    /**
     * 是否挑点中
     *
     * @Author t13max
     * @Date 13:49 2024/12/12
     */
    public boolean isJumping() {

        return type == ShiftEnum.JUMP;
    }
}
