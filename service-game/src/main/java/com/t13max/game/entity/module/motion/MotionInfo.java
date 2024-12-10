package com.t13max.game.entity.module.motion;


import com.t13max.game.pos.Position;
import com.t13max.game.pos.Vector3D;
import game.enums.MotionEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * 移动信息
 *
 * @Author t13max
 * @Date 16:38 2024/12/9
 */
public class MotionInfo {

    //移动方向
    public Vector3D direction;
    //移动起点
    public Vector3D position;
    //移动路径
    public List<Vector3D> paths = new ArrayList<>();
    //当前方向可达的最远点
    public Vector3D farthestPosition;
    //当前朝向
    public Vector3D faceDir;
    //移动类型
    public MotionEnum motionEnum;
}
