package com.t13max.game.entity.module.motion.attach;


import com.t13max.game.consts.UnitBits;
import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.motion.MotionInfo;
import com.t13max.game.pos.Vector3D;
import game.enums.MotionEnum;

import java.util.List;

/**
 * 跳跃
 *
 * @Author t13max
 * @Date 15:44 2024/12/10
 */
public class MoveJump extends MoveAttachment {

    private final MovePosition movePosition;

    public MoveJump(IEntity owner) {
        super(owner);
        movePosition = new MovePosition(owner);
    }

    @Override
    public MotionInfo getMotionInfo() {
        return movePosition.getMotionInfo();
    }

    @Override
    public MotionEnum getMotionEnum() {
        return MotionEnum.JUMP;
    }

    public int initial(List<Vector3D> path, int flags, long time) {
        float distance = 0.f;
        for (int i = 0; i < path.size() - 1; ++i) {
            distance += path.get(i).distance3D(path.get(i + 1));
        }

        float speed = distance / (float) time * 1000;
        this.movePosition.initial(path, flags);
        this.movePosition.setSpeed(speed);

        return 0;
    }

    @Override
    public boolean isStarted() {
        return super.isStarted();
    }

    @Override
    public int startup() {
        super.startup();

        owner.setBit(UnitBits.JUMP, true);

        this.movePosition.startup();

        return 0;
    }

    @Override
    public int cleanup() {
        super.cleanup();

        this.movePosition.cleanup();

        if (owner.getBit(UnitBits.JUMP)) {
            owner.setBit(UnitBits.JUMP, false);
        }

        return 0;
    }

    @Override
    public int tick(long now) {
        super.tick(now);

        if (!this.started) {
            return -1;
        }

        if (this.finished) {
            return -1;
        }

        movePosition.tick(now);

        this.finished = this.movePosition.finished;
        if (finished) {
            owner.setBit(UnitBits.JUMP, false);
        }

        return 0;
    }
}
