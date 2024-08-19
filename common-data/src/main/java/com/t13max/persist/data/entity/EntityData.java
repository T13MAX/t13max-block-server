package com.t13max.persist.data.entity;

import com.t13max.persist.data.IData;
import game.enums.EntityEnum;
import lombok.Data;

/**
 * 实体基类
 *
 * @author: t13max
 * @since: 15:29 2024/7/25
 */
@Data
public class EntityData implements IData {

    //所属类型
    private EntityEnum entityEnum;


}
