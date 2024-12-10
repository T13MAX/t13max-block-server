package com.t13max.game.entity.module.motion.attach;


import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.motion.MotionInfo;
import game.enums.MotionEnum;

/**
 * 飞行
 *
 * @Author t13max
 * @Date 10:28 2024/12/10
 */
public class MoveFly extends MoveAttachment {

    public MoveFly(IEntity owner) {
        super(owner);
    }

    @Override
    public MotionInfo getMotionInfo() {
        return null;
    }

    @Override
    public MotionEnum getMotionEnum() {
        return null;
    }
}
