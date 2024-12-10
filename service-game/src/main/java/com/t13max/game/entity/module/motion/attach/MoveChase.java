package com.t13max.game.entity.module.motion.attach;

import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.motion.MotionConst;
import com.t13max.game.entity.module.motion.MotionInfo;
import com.t13max.game.pos.Vector3D;
import com.t13max.game.world.module.WorldEntityMod;
import game.enums.MotionEnum;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * 追逐
 *
 * @author t13max
 * @since 16:42 2024/12/9
 */
public class MoveChase extends MoveAttachment {

    //追逐update间隔
    private static final int CHASE_UPDATE_INTERVAL = 500;

    //目标
    private long targetId;
    //追逐半径
    private float chaseRadius;
    //最大距离
    private float maxDistance;
    //上次tick时间
    private long lastTickTime;
    //下次更新时间
    private long nextUpdateTime;
    //目标位置
    private Vector3D targetPos;
    //移动到指定位置附件
    private final MovePosition movePosition;

    public MoveChase(IEntity owner) {
        super(owner);
        this.movePosition = new MovePosition(owner);
    }

    @Override
    public MotionEnum getMotionEnum() {
        return MotionEnum.CHASE;
    }

    public boolean startChase(long targetId, float minRadius, float maxDistance, int flag) {
        this.targetId = targetId;
        this.chaseRadius = minRadius;
        this.maxDistance = maxDistance;
        this.includeFlags(flag);
        return true;
    }

    @Override
    public int tick(long now) {
        super.tick(now);

        movePosition.tick(now);

        long deltaTime = now - lastTickTime;
        if (!onChaseUpdate(deltaTime)) {
            finished = true;
            return -1;
        }
        lastTickTime = now;

        return 0;
    }

    @Override
    public int startup() {
        super.startup();

        IEntity target = owner.getWorld().getWorldModules().getWorldModule(WorldEntityMod.class).getEntity(targetId);
        if (target == null) {
            return -1;
        }

        if (!chaseTarget(target)) {
            return -1;
        }
        return 0;
    }

    @Override
    public MotionInfo getMotionInfo() {
        return movePosition.getMotionInfo();
    }

    /**
     * 更新追逐
     *
     * @Author t13max
     * @Date 14:44 2024/12/10
     */
    private boolean onChaseUpdate(long deltaTime) {
        IEntity target = owner.getWorld().getWorldModules().getEntityMod().getEntity(targetId);
        if (target == null) {
            return false;
        }
        if (!owner.isMovable()) {
            return true;
        }

        if (!owner.getEntityModules().getMotionMod().isMoving()) {
            return true;
        }
        double distance = distance(owner.getPosition(), target.getPosition());
        double chaseDistance = chaseRadius;
        if (target.getEntityModules().getMotionMod().isMoving()) {
            chaseDistance *= 0.95f;
        }
        if (distance <= chaseDistance) {
            owner.getEntityModules().getMotionMod().onMotionEnded(!this.isIncludeFlags(MotionConst.MOTION_IGNORE_MSG));
            finished = true;
            return false;
        }
        double unitDistance = owner.getSpeed() * deltaTime / TimeUnit.SECONDS.toMillis(1);
        double tarDistance = distance(targetPos, owner.getPosition());

        // need update, targetPos invalid
        boolean targetPosInvalid = distance(targetPos, target.getPosition()) > maxDistance;
        // need update, frequency when target is moving
        boolean updateTime = target.getEntityModules().getMotionMod().isMoving() && owner.getTime() > nextUpdateTime;
        // need update, the critical to targetPos when target is moving
        boolean criticalDis = target.getEntityModules().getMotionMod().isMoving() && tarDistance < 2 * unitDistance;

        if (movePosition.finished || targetPosInvalid || criticalDis || updateTime) {
            if (!chaseTarget(target)) {
                return false;
            }
            nextUpdateTime = owner.getTime() + CHASE_UPDATE_INTERVAL;
        }
        return true;
    }

    /**
     * 追逐目标!
     * 前往目标点
     *
     * @Author t13max
     * @Date 14:35 2024/12/10
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean chaseTarget(IEntity target) {

        Vector3D beginPos = owner.getPosition();
        targetPos = getPositionAround(target);
        LinkedList<Vector3D> path = new LinkedList<>();
        if (this.isIncludeFlags(MotionConst.MOTION_COMPUTE_HEIGHT)) {
            path.add(targetPos);
        } else if (!beginPos.equals(targetPos)) {
            path.addAll(owner.getWorld().getWorldModules().getDetourMod().findPaths(owner, beginPos, targetPos));
        }
        if (path.isEmpty()) {
            return false;
        }

        movePosition.cleanup();
        int flag = isIncludeFlags(MotionConst.MOTION_COMPUTE_HEIGHT) ? MotionConst.MOTION_COMPUTE_HEIGHT : 0;
        movePosition.initial(path, flag);
        if (!movePosition.isStarted()) {
            movePosition.startup();
        }

        return true;
    }

    /**
     * 获取目标实体周围的做标
     *
     * @Author t13max
     * @Date 14:42 2024/12/10
     */
    private Vector3D getPositionAround(IEntity target) {
        float aroundRadius = chaseRadius * 0.9f;
        Vector3D pos = target.getEntityModules().getStationMod().getPositionAround(owner, aroundRadius, true);
        if (!owner.getWorld().getWorldModules().getDetourMod().isWalkable(pos, owner.getEntityModules().getMotionMod().getMotionFlags())) {
            pos = target.getPosition();
            if (!owner.getWorld().getWorldModules().getDetourMod().isWalkable(pos, owner.getEntityModules().getMotionMod().getMotionFlags())) {
                return owner.getPosition();
            }
        }
        return pos;
    }

}
