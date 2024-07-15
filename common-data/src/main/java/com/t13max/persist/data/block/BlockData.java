package com.t13max.persist.data.block;

import com.t13max.game.pos.Position;
import game.enums.BlockEnum;
import lombok.Data;

/**
 * 方块实体
 *
 * @author: t13max
 * @since: 13:38 2024/7/15
 */
@Data
public class BlockData {

    //方块所在位置
    private Position pos;

    private BlockEnum blockEnum = BlockEnum.AIR_BLOCK;

    public BlockData() {
    }
}
