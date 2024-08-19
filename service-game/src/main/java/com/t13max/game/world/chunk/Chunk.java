package com.t13max.game.world.chunk;

import com.t13max.game.entity.EntityFactory;
import com.t13max.game.entity.IEntity;
import com.t13max.game.util.PosUtil;
import com.t13max.persist.data.chunk.ChunkData;
import com.t13max.persist.data.entity.EntityData;
import com.t13max.util.TimeUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 区块内存对象
 *
 * @author: t13max
 * @since: 14:55 2024/8/14
 */
@Getter
@Setter
public class Chunk {
    //区块id
    private final int chunkId;
    //实体集合
    private final Map<Long, IEntity> entityMap = new HashMap<>();
    //区块数据实体
    private final ChunkData chunkData;
    //上一次 这个区块被要求加载的seconds 超过一定时间则异步入库 入库完成后 视情况卸载
    private int lastTickSeconds;
    //当前区块被加载的等级 玩家90格内强加载 90-112弱加载 实体会被删除 48格内的实体会被tick 48-90会被跳过
    private int level;

    public Chunk(ChunkData chunkData) {
        this.chunkId = chunkData.getChunkId();
        this.chunkData = chunkData;
        Map<Short, EntityData> entityDataMap = chunkData.getEntityDataMap();
        for (EntityData entityData : entityDataMap.values()) {
            IEntity entity = EntityFactory.createEntity(entityData.getEntityEnum(), entityData);
            this.entityMap.put(entity.getId(), entity);
        }
    }

    /**
     * 实体进入世界
     * 上线 通过传送门进入某个世界
     *
     * @Author t13max
     * @Date 14:03 2024/8/14
     */
    public void enterWorld(IEntity entity) {
        this.entityMap.put(entity.getId(), entity);
        this.chunkData.getEntityDataMap().put(PosUtil.getPos(entity.getPosition()), entity.getEntityData());
    }

    /**
     * 实体离开世界
     * 下线 通过传送门离开某个世界
     *
     * @Author t13max
     * @Date 14:03 2024/8/14
     */
    public void leaveWorld(IEntity entity) {
        this.entityMap.remove(entity.getId());
        this.chunkData.getEntityDataMap().remove(PosUtil.getPos(entity.getPosition()));
    }

    /**
     * 是否需要卸载
     * 没有人加载他超过一定时间 并且不是变动数据(被修改过还没存)
     *
     * @Author t13max
     * @Date 16:14 2024/8/19
     */
    public boolean checkUnload() {

        return false;
    }

}
