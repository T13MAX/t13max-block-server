package com.t13max.persist.data;

import dev.morphia.annotations.Transient;
import lombok.Data;

/**
 * 需要触发卸载的实体
 *
 * @author: t13max
 * @since: 17:56 2024/8/2
 */
@Data
public abstract class UnloadData implements IPersistData {

    //触发卸载时间
    @Transient
    private long triggerMills;

    //保存完成 可以卸载了
    @Transient
    private boolean ok;
}
