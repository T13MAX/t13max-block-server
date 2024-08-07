package com.t13max.game.world.module;

import com.t13max.game.consts.Const;
import com.t13max.game.world.World;
import com.t13max.persist.manager.DataManager;
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
public class WorldChunkMod extends WorldModule {

    private final Map<Long, ChunkData> chunkMap = new HashMap<>();

    public WorldChunkMod(World world) {
        super(world);
    }

    public ChunkData loadChunk(long chunkId) {
        ChunkData chunkData = DataManager.inst().findById(owner.getName() + Const.CHUNK_NAME, chunkId);
        this.chunkMap.put(chunkId, chunkData);
        return chunkData;
    }

    public void unloadChunk(ChunkData chunkData) {
        ChunkData remove = this.chunkMap.remove(chunkData.getChunkId());

    }

    public ChunkData getChunk(long chunkId) {
        return chunkMap.get(chunkId);
    }
}
