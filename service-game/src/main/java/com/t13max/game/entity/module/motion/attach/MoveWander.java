package com.t13max.game.entity.module.motion.attach;


import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.motion.MotionConst;
import com.t13max.game.entity.module.motion.MotionInfo;
import com.t13max.game.pos.Vector3D;
import com.t13max.game.util.TickTimer;
import com.t13max.game.world.module.WorldDetourMod;
import game.enums.MotionEnum;

import java.util.List;

/**
 * 逛街
 *
 * @Author t13max
 * @Date 15:49 2024/12/10
 */
public class MoveWander extends MoveAttachment {

    //停留间隔
    private int interval;
    //闲逛半径
    private float radius;

    private Vector3D center;

    private MovePosition movePosition;

    private final TickTimer wanderTimer = new TickTimer();

    public MoveWander(IEntity owner) {
        super(owner);
        movePosition = new MovePosition(owner);
    }

    @Override
    public MotionInfo getMotionInfo() {
        return movePosition.getMotionInfo();
    }

    @Override
    public MotionEnum getMotionEnum() {
        return MotionEnum.WANDER;
    }

    @Override
    public int startup() {
        if (super.startup() != 0) {
            return -1;
        }

        moveWander();

        return 0;
    }

    @Override
    public int tick(long now) {
        if (super.tick(now) != 0) {
            return 0;
        }

        // unmovable
        if (!owner.isMovable()) {
            return 0;
        }

        movePosition.tick(now);

        if (movePosition != null && movePosition.finished) {
            if (wanderTimer.isStarted()) {
                if (wanderTimer.isOnce(now)) {
                    moveWander();
                }
            } else {
                wanderTimer.start(interval);
            }
        }

        return 0;
    }

    public void doWander(Vector3D center, float radius, int interval) {
        this.center = new Vector3D(center);
        this.radius = radius;
        this.interval = interval;
    }

    private void moveWander() {
        movePosition.cleanup();
        int flag = isIncludeFlags(MotionConst.MOTION_COMPUTE_HEIGHT) ? MotionConst.MOTION_COMPUTE_HEIGHT : 0;
        WorldDetourMod modDetour = owner.getWorld().getWorldModules().getDetourMod();
        List<Vector3D> path = modDetour.findPaths(owner, owner.getPosition(), modDetour.findRandomPosInCircle(owner, center, radius));
        movePosition.initial(path, flag);
        movePosition.startup();
    }
}
