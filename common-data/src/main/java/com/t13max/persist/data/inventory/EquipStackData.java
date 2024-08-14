package com.t13max.persist.data.inventory;

import com.t13max.persist.collection.XMap;
import dev.morphia.annotations.Entity;
import game.enums.EnchantEnum;
import lombok.Data;

import java.util.Map;

/**
 * 装备
 *
 * @author: t13max
 * @since: 14:42 2024/7/15
 */
@Entity
@Data
public class EquipStackData extends AbstractStackData {

    //附魔效果
    private Map<EnchantEnum, Integer> enchantMap = new XMap<>();
}
