package com.t13max.game.utils;

import com.t13max.game.entity.IEntity;
import com.t13max.game.pos.Vector3D;
import lombok.experimental.UtilityClass;

/**
 * @author t13max
 * @since 16:07 2024/12/10
 */
@UtilityClass
public class VectorUtils {

    public static Vector3D me2TargetDir(IEntity me, IEntity target) {

        return cur2TargetDir(me.getPosition(), target.getPosition(), me.getDirection());
    }

    /**
     * 当前位置到目标位置的朝向
     *
     * @Author t13max
     * @Date 16:08 2024/12/10
     */
    public static Vector3D cur2TargetDir(Vector3D curPosition, Vector3D tarPosition, Vector3D defaultDir) {
        if (tarPosition.equals(curPosition))
            return defaultDir;

        return tarPosition.sub(curPosition).normalize();
    }
}
