package com.t13max.persist.data.chunk;

import com.t13max.game.pos.Position;
import com.t13max.persist.data.block.BlockData;
import dev.morphia.annotations.Id;

import java.util.HashMap;
import java.util.Map;

/**
 * 区块data
 * id是两个int值合并而来
 *
 * @author: t13max
 * @since: 13:37 2024/7/15
 */
public class ChunkData {

    @Id
    private long chunkId;

    private Map<Position, BlockData> blockDataMap = new HashMap<>();

}
