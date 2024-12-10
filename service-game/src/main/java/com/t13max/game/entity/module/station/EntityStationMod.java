package com.t13max.game.entity.module.station;

import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.EntityModule;
import com.t13max.game.pos.Position;
import com.t13max.game.pos.Vector3D;

/**
 * 实体站位
 *
 * @author t13max
 * @since 14:36 2024/12/10
 */
public class EntityStationMod extends EntityModule {

    public EntityStationMod(IEntity owner) {
        super(owner);
    }

    /**
     * 获取角色周围站位坐标
     *
     * @Author t13max
     * @Date 14:38 2024/12/10
     */
    public Vector3D getPositionAround(IEntity owner, float aroundRadius, boolean updateDirect) {
        return null;
    }

    public void leaveAround() {

    }

    public Vector3D getPositionAngle(IEntity owner, float v, Integer immutableAngle) {
        return null;
    }
}
