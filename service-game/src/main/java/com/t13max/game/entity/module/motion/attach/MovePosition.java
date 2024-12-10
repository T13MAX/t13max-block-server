package com.t13max.game.entity.module.motion.attach;


import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.motion.MotionConst;
import com.t13max.game.entity.module.motion.MotionInfo;
import com.t13max.game.exception.GameException;
import com.t13max.game.pos.Position;
import com.t13max.game.pos.Vector3D;
import game.enums.MotionEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 指定点移动
 *
 * @Author t13max
 * @Date 17:16 2024/12/9
 */
public class MovePosition extends MoveAttachment {

    //移动方向
    private Vector3D direction;
    //移动起点
    private Vector3D position;
    //移动路点
    @Getter
    private final List<Vector3D> paths = new ArrayList<>();
    //上一帧时间
    private long lastPulseTime;
    //速度，未设置取属性
    @Getter
    @Setter
    private float speed = 0.f;

    public MovePosition(IEntity owner) {
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

        if (this.finished) {
            return 0;
        }

        if (this.paths.isEmpty()) {
            throw new GameException("paths is empty, id:{}" + owner.getId());
        }

        long deltaTime = now - lastPulseTime;
        lastPulseTime = now;

        Vector3D from = this.position;
        Vector3D to = this.paths.get(0);
        double speed = this.speed > 0 ? this.speed : owner.getSpeed();
        double distanceMove = speed * deltaTime / TimeUnit.SECONDS.toMillis(1);
        double distanceTarget = distance(from, to);
        double heightDiff = to.getY() - from.getY();
        while (true) {
            if (distanceMove > distanceTarget) {
                this.paths.remove(0);

                // 没有下一路点，移动结束
                if (this.paths.isEmpty()) {
                    this.finished = true;

                    this.position = to;
                    owner.changePosition(this.position);

                    break;
                    // 有下一路点，拐点补偿
                } else {
                    distanceMove -= distanceTarget;
                    from = to;
                    to = paths.get(0);
                    distanceTarget = distance(from, to);

                    this.position = from;
                    owner.changePosition(this.position);
                    Vector3D direction = to.sub(from);

                    this.direction = normalize(direction);
                    if (!this.isIncludeFlags(MotionConst.MOTION_IGNORE_DIRECTION)) {
                        owner.changeDirection(this.direction);
                    }

                }
            } else {
                this.position = this.position.sum(normalize(direction).mul(distanceMove));
                if (!this.isIncludeFlags(MotionConst.MOTION_COMPUTE_HEIGHT)) {
                    // 对高度做平滑处理 防止找不到行走面
                    double diff = heightDiff * (distanceMove / distanceTarget);
                    this.position = new Vector3D(position.getX(), position.getY() + diff, position.getZ());
                }
                owner.changePosition(this.position);
                break;
            }
        }

        return 0;
    }

    @Override
    public int startup() {
        if (paths.size() <= 0) {
            return -1;
        }

        if (super.startup() < 0) {
            throw new GameException("already started, id="+owner.getId());
        }

        Vector3D posBegin = paths.get(0);
        Vector3D direction = posBegin.sub(this.position);

        this.direction = normalize(direction);
        if (!this.isIncludeFlags(MotionConst.MOTION_IGNORE_DIRECTION)) {
            owner.changeDirection(direction);
        }

        lastPulseTime = owner.getTime();
        owner.getEntityModules().getMotionMod().onMotionBegan(!this.isIncludeFlags(MotionConst.MOTION_IGNORE_MSG));

        return 0;
    }

    @Override
    public int cleanup() {
        super.cleanup();

        this.finished = true;

        return 0;
    }

    @Override
    public MotionInfo getMotionInfo() {
        MotionInfo info = new MotionInfo();
        info.direction = this.direction;
        info.position = this.position;
        info.paths.addAll(this.paths);

        return info;
    }

    @Override
    public MotionEnum getMotionEnum() {
        return MotionEnum.POSITION;
    }

    public int initial(List<Vector3D> path, int flags) {
        position = owner.getPosition();
        this.includeFlags(flags);

        // 修正所有点，如果连续两个点相同，则移出后一个
        Vector3D pos = this.position;
        Iterator<Vector3D> it = path.iterator();
        while (it.hasNext()) {
            Vector3D posNext = it.next();
            if (distance(pos, posNext) < 0.01) {
                it.remove();
                continue;
            }

            pos = posNext;
        }

        // 设置路点
        this.paths.clear();
        this.paths.addAll(path);

        return 0;
    }

    private boolean isZero(Vector3D position) {
        if (!this.isIncludeFlags(MotionConst.MOTION_COMPUTE_HEIGHT)) {
            return position.isZero2D();
        } else {
            return position.isZero();
        }
    }

    private Vector3D normalize(Vector3D v3) {
        if (!this.isIncludeFlags(MotionConst.MOTION_COMPUTE_HEIGHT)) {
            return v3.normalize();
        } else {
            return v3.normalize(true);
        }
    }

}
