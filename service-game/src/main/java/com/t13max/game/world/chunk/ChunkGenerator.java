package com.t13max.game.world.chunk;

import com.t13max.persist.data.chunk.ChunkData;

/**
 * @author: t13max
 * @since: 17:21 2024/7/25
 */
public abstract class ChunkGenerator {

    public abstract ChunkData generateChunkData(long chunkId);


}
