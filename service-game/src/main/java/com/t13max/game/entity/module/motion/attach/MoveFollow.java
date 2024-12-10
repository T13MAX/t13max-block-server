package com.t13max.game.entity.module.motion.attach;

import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.motion.MotionConst;
import com.t13max.game.entity.module.motion.MotionInfo;
import com.t13max.game.pos.Vector3D;
import com.t13max.game.utils.Utils;
import game.enums.MotionEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 跟随
 *
 * @author t13max
 * @since 16:42 2024/12/9
 */
public class MoveFollow extends MoveAttachment {
    
    private static final int UPDATE_INTERVAL = 1000;

    private long lastPulseTime;

    private long nextUpdateTime;

    private long targetId;

    private boolean syncSpeed;

    private Vector3D targetPos;

    private boolean estimated;

    private Vector3D targetDirection;

    private float followDistance;

    private Integer immutableAngle;

    private final MovePosition movePosition;

    public MoveFollow(IEntity owner) {
        super(owner);
        movePosition = new MovePosition(owner);
        
    }
 
    @Override
    public MotionInfo getMotionInfo() {
        return movePosition.getMotionInfo();
    }

    @Override
    public int startup() {
        super.startup();
        IEntity target =  owner.getWorld().getWorldModules().getEntityMod().getEntity(targetId);
        if (target == null) {
            return -1;
        }
        targetPos = getFollowPosition(target);
        if (updateFollow(0)) {
            return -1;
        }
        return 0;
    }

    @Override
    public int tick(long now) {
        super.tick(now);

        movePosition.tick(now);

        long deltaTime = now - lastPulseTime;
        if (updateFollow(deltaTime)) {
            finished = true;
            return -1;
        }

        return 0;
    }

    @Override
    public int cleanup() {
        super.cleanup();
        owner.getEntityModules().getStationMod().leaveAround();
        return 0;
    }

    public boolean follow(long targetId, float distance, boolean syncSpeed, Integer angle, int flag) {
        this.targetId = targetId;
        this.followDistance = distance;
        this.syncSpeed = syncSpeed;
        this.immutableAngle = angle;
        this.includeFlags(flag);

        return true;
    }

    private boolean updateFollow(long deltaTime) {
        lastPulseTime = owner.getTime();
        IEntity target =  owner.getWorld().getWorldModules().getEntityMod().getEntity(targetId);
        if (target == null) {
            return true;
        }
        if (!owner.getWorld().getWorldModules().getDetourMod().isWalkable(target.getPosition(), owner.getEntityModules().getMotionMod().getMotionFlags())) {
            return false;
        }
        if (!owner.isMovable()) {
            return false;
        }

        double distance = distance(owner.getPosition(), target.getPosition());
        distance += distance * MotionConst.POSITION_DISTANCE_ERROR_RANGE;
        if (distance < followDistance) {
            owner.getEntityModules().getMotionMod().onMotionEnded(!this.isIncludeFlags(MotionConst.MOTION_IGNORE_MSG));
            finished = true;
            return false;
        }

        if (syncSpeed) {
            float selfSpeed = owner.getSpeed();
            float targetSpeed = target.getSpeed();
            if (!Utils.isFloatEqual(selfSpeed, targetSpeed)) {
                owner.changeSpeed(selfSpeed, targetSpeed);
            }
        }

        double unitDistance = owner.getSpeed() * deltaTime / TimeUnit.SECONDS.toMillis(1);
        double tarDistance = distance(targetPos, owner.getPosition());

        // need update, frequency when target is moving
        boolean updateTime = false;
        if (owner.getTime() > nextUpdateTime) {
            if (target.getEntityModules().getMotionMod().isMoving()) //same direction ,update only arrive
                updateTime = !target.getDirection().equals(targetDirection);
            else // update if last follow target is estimated position
                updateTime = estimated;
        }

        // need update, the critical to targetPos when target is moving
        boolean criticalDis = target.getEntityModules().getMotionMod().isMoving() && tarDistance < unitDistance;
        if (deltaTime == 0 || updateTime || criticalDis) {
            estimated = false;
            Vector3D beginPos = owner.getPosition();
            targetPos = getFollowPosition(target);
            List<Vector3D> path = new ArrayList<>();
            if (this.isIncludeFlags(MotionConst.MOTION_COMPUTE_HEIGHT)) {
                path.add(targetPos);
            } else {
                path.addAll(owner.getWorld().getWorldModules().getDetourMod().findPaths(owner, beginPos, targetPos));
            }

            if (path.isEmpty()) {
                return true;
            }

            movePosition.cleanup();
            int flag = isIncludeFlags(MotionConst.MOTION_COMPUTE_HEIGHT) ? MotionConst.MOTION_COMPUTE_HEIGHT : 0;

            movePosition.initial(path, flag);
            if (!movePosition.isStarted()) {
                movePosition.startup();
            }
            targetDirection = new Vector3D(target.getDirection());
            nextUpdateTime = owner.getTime() + UPDATE_INTERVAL;
        } else {
            if (movePosition.isFinished())
                return deltaTime > 0 && !target.getEntityModules().getMotionMod().isMoving();
        }

        return false;
    }

    private Vector3D getFollowPosition(IEntity target) {
        if (target.getEntityModules().getMotionMod().isMoving()) {
            if (targetPos != null && target.getDirection().equals(targetDirection)) {
                Vector3D pos = estimatedPosition(owner.getPosition(), targetPos, targetDirection);
                if (pos.distance(owner.getPosition()) > owner.getSpeed()) {
                    return pos;
                }
            }
        }

        Vector3D pos;
        if (Objects.isNull(immutableAngle)) {
            // self-adaptation position
            pos = target.getEntityModules().getStationMod()
                    .getPositionAround(owner, followDistance * 0.9f, true);
        } else {
            // immutable position
            pos = target.getEntityModules().getStationMod()
                    .getPositionAngle(owner, followDistance * 0.9f, immutableAngle);
        }


        if (!owner.getWorld().getWorldModules().getDetourMod().isWalkable(pos, owner.getEntityModules().getMotionMod().getMotionFlags())) {
            pos = target.getPosition();
        }

        //高度投影
//        pos = owner.getWorld().getWorldModules().getDetourMod().getHeight(new Vector3D(pos.x,owner.getPosition().y,pos.z));
        return pos;
    }

    private Vector3D estimatedPosition(Vector3D startPos, Vector3D targetPos, Vector3D direction) {
        estimated = true;

        Vector3D pos = targetPos.sum(direction.mul(5 * owner.getSpeed()));
        if (owner.getWorld().getWorldModules().getDetourMod().isWalkable(pos, owner.getEntityModules().getMotionMod().getMotionFlags())) {
            return pos;
        }
        pos = targetPos.sum(direction.mul(2 * owner.getSpeed()));
        if (owner.getWorld().getWorldModules().getDetourMod().isWalkable(pos, owner.getEntityModules().getMotionMod().getMotionFlags())) {
            return pos;
        }

        return startPos;
    }

    @Override
    public MotionEnum getMotionEnum() {
        return MotionEnum.FOLLOW;
    }
}
