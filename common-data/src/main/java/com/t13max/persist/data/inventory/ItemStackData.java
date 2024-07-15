package com.t13max.persist.data.inventory;

import lombok.Data;

/**
 * 道具
 *
 * @Author: t13max
 * @Since: 22:09 2024/7/14
 */
@Data
public class ItemStackData extends AbstractStackData{

    public static final ItemStackData EMPTY = new ItemStackData();

    //数量
    private int num;

}
