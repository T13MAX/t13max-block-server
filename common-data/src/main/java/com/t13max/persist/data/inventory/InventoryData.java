package com.t13max.persist.data.inventory;

import java.util.HashMap;
import java.util.Map;

/**
 * 玩家库存
 *
 * @Author: t13max
 * @Since: 22:07 2024/7/14
 */
public class InventoryData {

    //背包列表 快捷工具栏10 背包30 从0开始
    private Map<Integer, ItemStackData> itemStackMap = new HashMap();
}
