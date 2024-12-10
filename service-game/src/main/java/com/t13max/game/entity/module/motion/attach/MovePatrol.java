package com.t13max.game.entity.module.motion.attach;


import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.motion.MotionConst;
import com.t13max.game.entity.module.motion.MotionInfo;
import com.t13max.game.pos.Vector3D;
import com.t13max.game.util.TickTimer;
import game.enums.MotionEnum;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 按路点巡逻
 *
 * @Author t13max
 * @Date 15:58 2024/12/10
 */
public class MovePatrol extends MoveAttachment {

    private int currentWayPoint;

    private int delay;

    private int flags;

    private TickTimer delayTimer = new TickTimer();

    private final MovePosition movePosition;

    public MovePatrol(IEntity owner) {
        super(owner);
        movePosition = new MovePosition(owner);
    }

    @Override
    public MotionInfo getMotionInfo() {
        return movePosition.getMotionInfo();
    }

    @Override
    public int tick(long now) {
        super.tick(now);
        movePosition.tick(now);
        return updateMovePoint();
    }

    @Override
    public int startup() {
        super.startup();
        setMovePoint();

        return 0;
    }

    @Override
    public boolean isStarted() {
        return super.isStarted();
    }

    @Override
    public MotionEnum getMotionEnum() {
        return MotionEnum.PATROL;
    }

    public boolean doPatrol(int wayPointId, int delay, int flags) {
        this.currentWayPoint = wayPointId;
        this.delay = delay;
        this.flags = flags;
        return this.wayPointCheck(wayPointId);
    }


    private int updateMovePoint() {
        if (movePosition.isFinished()) {
            owner.getEntityModules().getMotionMod().onMotionEnded(!this.isIncludeFlags(MotionConst.MOTION_IGNORE_MSG));
            return setMovePoint();
        }
        return 0;
    }

    private int setMovePoint() {
        /*ConfWayPoint confWayPoint = ConfWayPoint.get(currentWayPoint);
        if (confWayPoint != null) {
            Vector3D vector3D = new Vector3D(confWayPoint.position[0], confWayPoint.position[1], confWayPoint.position[2]);
            movePosition.cleanup();
            if (vector3D.distance(owner.getPosition()) > 0.01) {
                movePosition.initial(new ArrayList<>(Collections.singletonList(vector3D)), flags);
                if (!movePosition.isStarted()) {
                    movePosition.startup();
                }
                owner.getUnitModule().getModMotion().onMotionBegan(!this.isIncludeFlags(MotionDef.MOTION_IGNORE_MSG));
                return 0;
            }

            if (confWayPoint.nextSn > 0) {
                if (delayTimer.isStarted()) {
                    if (delayTimer.isOnce(owner.getTime())) {
                        return setNextWayPoint(confWayPoint.nextSn);
                    }
                } else {
                    delayTimer.start(delay);
                }
            } else {
                finished = true;
                return -1;
            }

        }*/
        return 0;
    }

    private int setNextWayPoint(int nextWayPoint) {
        if (!wayPointCheck(nextWayPoint)) {
            return -1;
        }

        // TODO MovePatrol setNextWayPoint
        /*currentWayPoint = nextWayPoint;
        CreatureObject creatureObject = owner.queryObject(CreatureObject.class);
        if (creatureObject != null) {
            creatureObject.setWayPoint(currentWayPoint);
        }*/

        return 0;
    }

    /**
     * TODO MovePatrol wayPointCheck
     *
     * @Author t13max
     * @Date 16:03 2024/12/10
     */
    private boolean wayPointCheck(int wayPoint) {
        /*Scene scene = owner.getScene();
        ConfWayPoint confNextWayPoint = ConfWayPoint.get(wayPoint);
        if (confNextWayPoint == null) {
            LogServer.scene.error("ConfWayPoint not exist sn = {}", wayPoint);
            return false;
        }

        if (confNextWayPoint.sceneSn != scene.getSn()) {
            LogServer.scene.error("ConfWayPoint 路点场景不匹配, wayPointSn = {}, sceneSn = {}", confNextWayPoint.sn, scene.getSn());
            return false;
        }

        if (confNextWayPoint.position.length < 3) {
            LogServer.scene.error("illegal position in ConfWayPoint, wayPointSn = {}", confNextWayPoint.sn);
            return false;
        }*/

        return true;
    }
}
