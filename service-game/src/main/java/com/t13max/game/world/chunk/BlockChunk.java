package com.t13max.game.world.chunk;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 区块
 * 这是一个区块集合 一个World持有一个 里面包含这个世界所有已加载的区块
 *
 * @Author: t13max
 * @Since: 21:35 2024/7/14
 */
@Getter
@Setter
public class BlockChunk {

    private final Map<Long, Chunk> chunkMap = new HashMap<>();

    public BlockChunk() {
    }


}
