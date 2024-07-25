package com.t13max.game.world.module;

import com.t13max.game.world.World;
import com.t13max.persist.data.chunk.ChunkData;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 区块管理模块
 *
 * @author: t13max
 * @since: 16:20 2024/7/25
 */
@Getter
public class WorldChunkMod extends WorldModule{

    private final Map<Long, ChunkData> blockDataMap = new HashMap<>();

    public WorldChunkMod(World world) {
        super(world);
    }


}
