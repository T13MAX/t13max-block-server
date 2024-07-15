package com.t13max.persist.data.entity;

import com.t13max.persist.data.IPersistData;
import com.t13max.persist.data.inventory.ItemStackData;
import dev.morphia.annotations.Id;
import game.enums.EquipPartsEnum;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 僵尸实体
 *
 * @author: t13max
 * @since: 14:03 2024/7/15
 */
@Data
public class ZombieEntityData implements IPersistData {

    @Id
    private long id;
    //左手
    private ItemStackData rightHandItem = ItemStackData.EMPTY;
    //右手
    private ItemStackData leftHandItem = ItemStackData.EMPTY;
    //装备
    private Map<EquipPartsEnum, ItemStackData> equipItemMap = new HashMap<>();
    //元数据 value灵活运用
    private Map<String, String> metaMap = new HashMap<>();
}
