package com.t13max.game.world.chunk;

import com.t13max.persist.data.chunk.ChunkData;
import game.enums.BlockEnum;

import java.util.List;

/**
 * 超平坦
 *
 * @author: t13max
 * @since: 17:21 2024/7/25
 */
public class FlatChunkGenerator extends ChunkGenerator {

    private final List<FlatPair> flatList;

    public FlatChunkGenerator(List<FlatPair> flatList) {
        this.flatList = flatList;
    }

    @Override
    public ChunkData generateChunkData(long pos) {
        ChunkData chunkData = new ChunkData(pos);
        for (FlatPair pair : flatList) {

        }
        return null;
    }

    record FlatPair(BlockEnum blockEnum, Integer layer) {

    }
}
