package com.t13max.game.world.chunk;

import com.t13max.game.entity.IEntity;
import com.t13max.persist.data.chunk.ChunkData;

import java.util.HashMap;
import java.util.Map;

/**
 * 区块内存对象
 *
 * @author: t13max
 * @since: 14:55 2024/8/14
 */
public class Chunk {
    //区块id
    private final int chunkId;
    //实体集合
    private final Map<Long, IEntity> entityMap = new HashMap<>();
    //区块数据实体
    private final ChunkData chunkData;
    //区块加载等级?
    private int level;

    public Chunk(ChunkData chunkData) {
        this.chunkId = chunkData.getChunkId();
        this.chunkData = chunkData;
    }

    /**
     * 实体进入世界
     * 上线 通过传送门进入某个世界
     *
     * @Author t13max
     * @Date 14:03 2024/8/14
     */
    public void enterWorld(IEntity entity) {

    }

    /**
     * 实体离开世界
     * 下线 通过传送门离开某个世界
     *
     * @Author t13max
     * @Date 14:03 2024/8/14
     */
    public void leaveWorld(IEntity entity) {

    }
}
