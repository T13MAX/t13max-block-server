package com.t13max.game.world.chunk;


import com.t13max.game.block.IBlock;
import com.t13max.game.pos.Position;

import java.util.HashMap;
import java.util.Map;

/**
 * 区块内存实体
 *
 * @author: t13max
 * @since: 11:10 2024/7/15
 */
public class Chunk {

    //所有的方块的信息
    private Map<Position, IBlock> blockDataMap = new HashMap<>();
}
