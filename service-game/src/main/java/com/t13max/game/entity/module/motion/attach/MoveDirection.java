package com.t13max.game.entity.module.motion.attach;

import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.motion.MotionConst;
import com.t13max.game.entity.module.motion.MotionInfo;
import com.t13max.game.exception.GameException;
import com.t13max.game.pos.Vector3D;
import game.enums.MotionEnum;

/**
 * 朝着指定方向移动
 *
 * @author t13max
 * @since 16:42 2024/12/9
 */
public class MoveDirection extends MoveAttachment {
    //移动方向
    private Vector3D direction;
    //移动起点
    private Vector3D startPosition;
    //当前方向可达的最远点
    private Vector3D farthestPosition;
    //移动信息
    private MotionInfo motionInfo;

    public MoveDirection(IEntity owner) {
        super(owner);
    }

    @Override
    public boolean isFinished() {
        if (this.isIncludeFlags(MotionConst.MOTION_IGNORE_FINISHED)) {
            return false;
        }

        return this.finished;
    }

    @Override
    public int tick(long now) {
        super.tick(now);

        if (!this.isStarted()) {
            return -1;
        }

        return 0;
    }

    @Override
    public int startup() {
        if (super.startup() < 0) {
            throw new GameException("already started, id="+ owner.getId());
        }

        return 0;
    }

    @Override
    public int cleanup() {
        super.cleanup();

        this.direction = null;
        this.motionInfo = null;
        this.startPosition = null;
        this.farthestPosition = null;

        this.finished = true;

        return 0;
    }

    @Override
    public MotionInfo getMotionInfo() {
        return motionInfo;
    }
    
    public int initial(Vector3D startPos, Vector3D moveDir, Vector3D faceDir, Vector3D farthestPos, MotionEnum motionEnum, int flags) {
        startPosition = startPos;
        direction = normalize(moveDir);
        farthestPosition = farthestPos;
        this.includeFlags(flags);

        motionInfo = new MotionInfo();
        motionInfo.direction = this.direction;
        motionInfo.faceDir = normalize(faceDir);
        motionInfo.position = this.startPosition;
        motionInfo.paths.add(farthestPosition);
        motionInfo.farthestPosition = this.farthestPosition;
        motionInfo.motionEnum = motionEnum;

        return 0;
    }

    private Vector3D normalize(Vector3D v3) {
        if (!this.isIncludeFlags(MotionConst.MOTION_COMPUTE_HEIGHT)) {
            return v3.normalize();
        } else {
            return v3.normalize(true);
        }
    }

    @Override
    public MotionEnum getMotionEnum() {
        return MotionEnum.DIRECTION;
    }
}
