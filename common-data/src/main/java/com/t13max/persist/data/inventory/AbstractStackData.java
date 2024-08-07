package com.t13max.persist.data.inventory;

import com.t13max.persist.collection.XMap;
import game.enums.ItemEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * 抽象道具
 *
 * @author: t13max
 * @since: 14:43 2024/7/15
 */
public abstract class AbstractStackData implements IItemStack {

    //道具类型
    private ItemEnum itemEnum = ItemEnum.AIR_ITEM;
    //元数据
    private Map<String, String> metaMap = new XMap<>();
}
