package com.t13max.persist.data.entity;


import com.t13max.persist.data.IPersistData;
import com.t13max.persist.data.inventory.InventoryData;
import com.t13max.persist.data.inventory.ItemStackData;
import dev.morphia.annotations.*;
import game.enums.EquipPartsEnum;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 玩家数据
 *
 * @Author: t13max
 * @Since: 22:04 2024/7/14
 */
@Entity("PlayerData")
@Indexes({
        @Index(fields = {@Field("userId")})
})
@Data
public class PlayerData implements IPersistData {

    @Id
    private long id;
    //玩家唯一用户id
    private long userId;
    //玩家的库存
    private InventoryData inventoryData = new InventoryData();
    //左手
    private ItemStackData rightHandItem = ItemStackData.EMPTY;
    //右手
    private ItemStackData leftHandItem = ItemStackData.EMPTY;
    //装备
    private Map<EquipPartsEnum, ItemStackData> equipItemMap = new HashMap<>();
}
