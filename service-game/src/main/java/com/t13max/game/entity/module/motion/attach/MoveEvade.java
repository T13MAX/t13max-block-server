package com.t13max.game.entity.module.motion.attach;


import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.motion.MotionConst;
import com.t13max.game.entity.module.motion.MotionInfo;
import com.t13max.game.pos.Vector3D;
import com.t13max.game.utils.VectorUtils;
import game.enums.MotionEnum;

import java.util.List;
import java.util.function.Function;

/**
 * 逃离
 *
 * @Author t13max
 * @Date 17:14 2024/12/9
 */
public class MoveEvade extends MoveAttachment {

    private static final int PERIOD = 1000;

    private float evadeRange;

    private Function<IEntity, IEntity> alertFunc;

    private MovePosition movePosition;

    private byte retryCnt;

    private int auraId;

    private long nextCheckTime;

    public MoveEvade(IEntity owner) {
        super(owner);
        movePosition = new MovePosition(owner);
    }

    public void moveEvade(IEntity baseTarget,
                          float evadeRange, int auraId,
                          Function<IEntity, IEntity> alertFunc,
                          int flag) {
        this.evadeRange = evadeRange;
        this.alertFunc = alertFunc;
        this.includeFlags(flag);
        startup();
        updateEvade(baseTarget, owner.getTime());
        if (auraId > 0) {
            this.auraId = auraId;
            owner.getEntityModules().getAuraMod().createAura(auraId);
        }
    }

    @Override
    public int tick(long now) {
        super.tick(now);

        movePosition.tick(now);
        if (updateEvade(null, now)) {
            owner.getEntityModules().getMotionMod().onMotionEnded(!this.isIncludeFlags(MotionConst.MOTION_IGNORE_MSG));
            finished = true;
            return -1;
        }

        return 0;
    }

    @Override
    public int cleanup() {
        super.cleanup();
        if (auraId > 0) {
            owner.getEntityModules().getAuraMod().deleteAura(auraId);
        }
        return 0;
    }

    private boolean updateEvade(IEntity target, long now) {
        if (!owner.isMovable()) {
            return false;
        }

        if (now < nextCheckTime) {
            return false;
        }
        nextCheckTime = now + PERIOD;

        if (target == null) {
            target = alertFunc.apply(owner);
        }
        if (target == null) {
            return movePosition.isFinished();
        }
        Vector3D direction = VectorUtils.me2TargetDir(target, owner);
        Vector3D targetPos = owner.getPosition().sum(direction.mul(evadeRange));
        List<Vector3D> path = owner.getWorld().getWorldModules().getDetourMod().findPaths(owner, owner.getPosition(), targetPos);
        if (path.isEmpty() || path.get(path.size() - 1).equals(owner.getPosition())) {
            targetPos = owner.getWorld().getWorldModules().getDetourMod()
                    .findRandomPosInCircle(owner, owner.getPosition(), evadeRange * 0.5, evadeRange);
            path = owner.getWorld().getWorldModules().getDetourMod().findPaths(owner, owner.getPosition(), targetPos);
            if (path.isEmpty()) {
                return false;
            }
        }
        movePosition.cleanup();
        int flag = isIncludeFlags(MotionConst.MOTION_COMPUTE_HEIGHT) ? MotionConst.MOTION_COMPUTE_HEIGHT : 0;
        movePosition.initial(path, flag);
        if (!movePosition.isStarted()) {
            movePosition.startup();
        }
        return false;
    }

    @Override
    public MotionInfo getMotionInfo() {
        return movePosition.getMotionInfo();
    }

    @Override
    public MotionEnum getMotionEnum() {
        return MotionEnum.EVADE;
    }
}
