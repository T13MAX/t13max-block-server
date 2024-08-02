package com.t13max.persist.data.world;

import com.t13max.persist.data.IPersistData;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.Data;

/**
 * 世界数据类 保存世界的一些信息
 * 详细区块信息保存在 {@link com.t13max.persist.data.chunk.ChunkData)
 * <p>
 * @author: t13max
 * @since: 15:33 2024/8/2
 */
@Entity
@Data
public class WorldData implements IPersistData {

    //使用名字作为唯一主键 理论上世界再多 也多不到哪去
    @Id
    private String name;
}
