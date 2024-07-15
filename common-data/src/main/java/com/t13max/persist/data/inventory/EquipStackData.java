package com.t13max.persist.data.inventory;

import game.enums.EnchantEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * 装备
 *
 * @author: t13max
 * @since: 14:42 2024/7/15
 */
public class EquipStackData extends AbstractStackData {

    //附魔效果
    private Map<EnchantEnum, Integer> enchantMap = new HashMap<>();
}
