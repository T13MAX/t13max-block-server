package com.t13max.game.entity.module.station;

import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.IndirectObject;
import com.t13max.game.entity.module.EntityModule;
import com.t13max.game.pos.Vector3D;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 实体站位
 *
 * @author t13max
 * @since 14:36 2024/12/10
 */
public class EntityStationMod extends EntityModule {

    private static final double R_ANGLE = Math.PI / 2;

    @Getter
    private final Map<Integer, IEntity> usedIndex = new HashMap<>();

    private Vector3D aroundDirection;

    private int lastAroundIndex = 0;

    private int aroundIndex = -1;

    private IndirectObject<IEntity> target;

    @Setter
    private Vector3D spreadPosition;

    private int followCount;

    @Getter
    @Setter
    private double distTemp;

    private final LinkedList<Integer> vacantIndex = new LinkedList<>();

    private int factor = 1;

    public EntityStationMod(IEntity owner) {
        super(owner);
    }

    @Override
    public int pulse(long now) {
        return super.pulse(now);
    }

    @Override
    public void leaveWorld() {
        super.leaveWorld();
        spreadPosition = null;
        usedIndex.clear();
    }

    /**
     * 获取角色周围站位坐标
     *
     * @Author t13max
     * @Date 11:16 2024/12/12
     */
    public Vector3D getPositionAround(IEntity object, float radius, boolean updateDirect) {
        Vector3D aroundPosition = owner.getPosition();
        if (object == null) {
            return aroundPosition;
        }
        if (owner.getPosition().distance(object.getPosition()) < 0.1f)
            return aroundPosition;
        int aroundIndex = object.getEntityModules().getStationMod().getAroundIndex(owner, true);
        if (aroundIndex < 0) {
            int index = 0;
            for (; index < usedIndex.size(); ++index) {
                if (!usedIndex.containsKey(index)) {
                    break;
                }
            }
            aroundIndex = index;
            usedIndex.put(index, object);

            object.getEntityModules().getStationMod().setAroundIndex(owner, aroundIndex);
        }

        if (updateDirect) {
            if (lastAroundIndex == 0 || aroundIndex <= lastAroundIndex) {
                Vector3D newDir = aroundPosition.sub(object.getPosition()).normalize();
                if (aroundDirection != null && aroundDirection.dot2D(newDir) < 0) {
                    this.factor = -this.factor;
                }
                aroundDirection = newDir;
            }
            lastAroundIndex = aroundIndex;
        }

        if (radius < 0.5f) {
            radius += 0.5f;
        }

        double unit = object.getModelRadius() < radius ? Math.asin(object.getModelRadius() / radius) : R_ANGLE;

        Vector3D dir = new Vector3D(aroundDirection);

        int a = (aroundIndex + 1) / 2;
        int b = aroundIndex % 2;
        double offsetDir = unit * a;
        if (b == 0) {
            dir = dir.rotateRad(offsetDir, false);
        } else {
            dir = dir.rotateRad(offsetDir, true);
        }
        dir = dir.normalize();

        aroundPosition = aroundPosition.sum(dir.mul(radius));

        return aroundPosition;
    }

    public Vector3D getPositionAngle(IEntity object, float radius, int angle) {
        Vector3D aroundPosition = owner.getPosition();
        if (object == null) {
            return aroundPosition;
        }
        Vector3D dir = owner.getNormalizeDir().rotateCW(angle);

        return aroundPosition.sum(dir.mul(radius));
    }

    public void leaveAround() {
        if (target != null) {
            IEntity targetEntity = target.get();
            if (targetEntity != null && targetEntity.getWorld() == owner.getWorld()) {
                targetEntity.getEntityModules().getStationMod().remMeFromAround(owner);
            }
        }
        this.aroundIndex = -1;
    }

    public void remMeFromAround(IEntity me) {
        this.usedIndex.remove(me.getEntityModules().getStationMod().getAroundIndex(owner, false));
    }

    /**
     * 获取跟随站位
     *
     * @Author t13max
     * @Date 11:19 2024/12/12
     */
    public Vector3D getFollowPosition(IEntity object, float distance) {
        Vector3D followPosition = new Vector3D(owner.getPosition());
        if (object == null) {
            return followPosition;
        }
        int aroundIndex = object.getEntityModules().getStationMod().getAroundIndex(owner, true);
        if (aroundIndex < 0 || aroundIndex > followCount) {
            if (vacantIndex.isEmpty()) {
                aroundIndex = ++this.followCount;
                object.getEntityModules().getStationMod().setAroundIndex(owner, aroundIndex);
            } else {
                object.getEntityModules().getStationMod().setAroundIndex(owner, vacantIndex.pop());
            }

        }

        Vector3D dir = new Vector3D(owner.getDirection());

        float rotateDir = (float) (Math.PI / 2);
//        dir = dir.rotateRad(rotateDir, false);

        if (aroundIndex > 1 && followCount > 1) {
            rotateDir = (float) (Math.PI / (followCount - 1) * (aroundIndex - 1));
            dir = dir.rotateRad(-rotateDir, false);
        }

        dir = dir.normalize();

        followPosition = followPosition.sum(dir.mul(distance));

        return followPosition;
    }

    public boolean isSpread() {
        return this.spreadPosition != null && this.spreadPosition.equals(owner.getPosition());
    }

    private int getAroundIndex(IEntity targetEntity, boolean check) {
        if (this.target == null || this.target.get() == null) {
            return -1;
        }
        if (check && this.target.get().getId() != targetEntity.getId()) {
            targetEntity.getEntityModules().getStationMod().remMeFromAround(owner);
            return -1;
        }


        return aroundIndex;
    }

    private void setAroundIndex(IEntity target, int aroundIndex) {
        this.aroundIndex = aroundIndex;
        this.target = target.getIndirectObject();
    }
}
