package com.t13max.persist.data.entity;

import com.t13max.persist.data.IPersistData;
import dev.morphia.annotations.Entity;
import lombok.Data;

/**
 * 方块实体
 * 有些方块是有状态的
 *
 * @author: t13max
 * @since: 17:50 2024/8/2
 */
@Entity
@Data
public class BlockEntity implements IPersistData {
}
