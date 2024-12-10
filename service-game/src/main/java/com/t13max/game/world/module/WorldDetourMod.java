package com.t13max.game.world.module;

import com.t13max.game.entity.IEntity;
import com.t13max.game.pos.Position;
import com.t13max.game.pos.Vector3D;
import com.t13max.game.world.World;

import java.util.List;

/**
 * @author t13max
 * @since 14:32 2024/12/10
 */
public class WorldDetourMod extends WorldModule {

    public WorldDetourMod(World world) {
        super(world);
    }

    public List<Vector3D> findPaths(IEntity owner, Vector3D beginPos, Vector3D targetPos) {
        return null;
    }

    public boolean isWalkable(Vector3D pos, int motionFlags) {
        return false;
    }

    public Vector3D findRandomPosInCircle(IEntity owner, Vector3D pos, float radius) {
        return findRandomPosInCircle(owner, pos, 0, radius);
    }

    public Vector3D findRandomPosInCircle(IEntity senderOrNull, Vector3D pos, double minRadius, double maxRadius) {
        return null;
    }

    public int getMotionFlags() {
        return 0;
    }
}
