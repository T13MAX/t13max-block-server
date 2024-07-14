package com.t13max.persist.data;


import com.t13max.persist.data.inventory.InventoryData;
import dev.morphia.annotations.*;
import lombok.Data;

/**
 * @Author: t13max
 * @Since: 22:04 2024/7/14
 */
@Entity("PlayerData")
@Indexes({
        @Index(fields = {@Field("userId")})
})
@Data
public class PlayerData implements IPersistData{

    @Id
    private long id;

    //玩家唯一用户id
    private long userId;

    //玩家的库存
    private InventoryData inventoryData = new InventoryData();
}
