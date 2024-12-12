package com.t13max.game.world.module;

import com.t13max.game.entity.IEntity;
import com.t13max.game.pos.Vector3D;
import com.t13max.game.world.World;

import java.util.List;

/**
 * @author t13max
 * @since 14:32 2024/12/10
 */
public class WorldUuidMod extends WorldModule {

    public WorldUuidMod(World world) {
        super(world);
    }

    public long applyRefId() {
        return 0;
    }
}
