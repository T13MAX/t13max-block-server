package com.t13max.persist.data.chunk;

import com.t13max.game.util.PosUtil;
import com.t13max.persist.collection.XMap;
import com.t13max.persist.data.IData;
import com.t13max.persist.data.entity.EntityData;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.Data;

import java.util.Map;

/**
 * 区块data
 *
 * @author: t13max
 * @since: 13:37 2024/7/15
 */
@Entity
@Data
public class ChunkData implements IData {

    @Id
    private int chunkId;//区块id 由两个short拼成 高16位是z 低16位是x

    //key是方块在当前区块的位置 8位存高度 4位存x 4位存y
    private Map<Short, Short> blockDataMap = new XMap<>();

    //实体对象不会太多 全存 理论上 被命名的实体才会序列化 其他实体在序列化之前就删掉了
    private Map<Short, EntityData> entityDataMap = new XMap<>();

    public ChunkData() {
    }

    public ChunkData(int chunkId) {
        this.chunkId = chunkId;
    }

    public int getX() {
        return PosUtil.getChunkX(chunkId);
    }

    public int getZ() {
        return PosUtil.getChunkZ(chunkId);
    }
}
