package com.t13max.game.entity.module.motion;

/**
 * @author t13max
 * @since 17:18 2024/12/9
 */
public interface MotionConst {
    //移动标记位忽略消息
    int MOTION_IGNORE_MSG = 0x0001;
    //移动标记位忽略方向
    int MOTION_IGNORE_DIRECTION = 0x0002;
    //到达目的地后,停下不动
    int MOTION_IGNORE_FINISHED = 0x0004;
    //移动标记位计算高度
    int MOTION_COMPUTE_HEIGHT = 0x0008;

    float POSITION_DISTANCE_ERROR_RANGE = 0.05f;
}
