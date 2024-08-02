package com.t13max.game.world.chunk;

import com.t13max.game.consts.Const;
import com.t13max.game.util.PosUtil;
import com.t13max.persist.data.chunk.ChunkData;
import game.enums.BlockEnum;

import java.util.List;
import java.util.Map;

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
    public ChunkData generateChunkData(long chunkId) {
        ChunkData chunkData = new ChunkData(chunkId);
        Map<Short, Short> blockDataMap = chunkData.getBlockDataMap();
        int y = 0;
        for (FlatPair pair : flatList) {
            short blockId = (short) pair.blockEnum.getNumber();
            for (int i = 0; i < pair.layer; i++) {
                for (int j = 0; j < Const.CHUNK_LENGTH; j++) {
                    for (int k = 0; k < Const.CHUNK_LENGTH; k++) {
                        blockDataMap.put(PosUtil.getPos(j, k, y), blockId);
                    }
                }
                y++;
            }
        }
        return null;
    }

    record FlatPair(BlockEnum blockEnum, Integer layer) {

    }
}
