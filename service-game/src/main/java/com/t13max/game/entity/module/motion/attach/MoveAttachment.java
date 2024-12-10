package com.t13max.game.entity.module.motion.attach;

import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.motion.MotionConst;
import com.t13max.game.entity.module.motion.MotionInfo;
import com.t13max.game.pos.Position;
import com.t13max.game.pos.Vector3D;
import game.enums.MotionEnum;
import lombok.Getter;

/**
 * 移动用附件
 *
 * @author t13max
 * @since 16:34 2024/12/9
 */
public abstract class MoveAttachment {

    //所有者
    protected final IEntity owner;
    //位移已开始
    @Getter
    protected boolean started;
    //位移已结束
    @Getter
    protected boolean finished;
    //移动标记为
    private int flags;

    protected MoveAttachment(IEntity owner) {
        this.owner = owner;
    }

    public int startup() {
        if (this.isStarted()) {
            return -1;
        }

        this.started = true;
        this.finished = false;

        return 0;
    }

    public int cleanup() {
        this.flags = 0;
        if (!started) {
            return -1;
        }

        started = false;
        this.finished = true;

        return 0;
    }

    public abstract MotionInfo getMotionInfo();

    public abstract MotionEnum getMotionEnum();

    public int tick(long now) {

        return 0;
    }

    public boolean isIncludeFlags(int flag) {
        int result = this.flags & flag;

        return result > 0;
    }

    public void includeFlags(int value) {
        this.flags |= value;
    }

    public void excludeFlags(int value) {
        this.flags &= ~value;
    }

    /**
     * 两点距离
     *
     * @Author t13max
     * @Date 14:43 2024/12/10
     */
    protected double distance(Vector3D from, Vector3D to) {
        if (this.isIncludeFlags(MotionConst.MOTION_COMPUTE_HEIGHT)) {
            return from.distance3D(to);
        } else {
            return from.distance(to);
        }
    }
}
