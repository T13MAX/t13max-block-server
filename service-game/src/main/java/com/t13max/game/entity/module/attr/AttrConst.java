package com.t13max.game.entity.module.attr;

/**
 * Attr常量
 *
 * @Author t13max
 * @Date 17:11 2024/12/11
 */
public class AttrConst {

    //---------------------------------------------------------------------
    // 属性Id类型

    //属性Key跨度
    public static final int ATTR_SPACE_CONST = 10;

    //Id以0结尾的属性类型
    public static final int ATTR_FINAL_ID_TYPE = 0x0001;

    //Id以1结尾的数值属性类型
    public static final int ATTR_VALUE_ID_TYPE = 0x0002;

    //Id以2结尾的百分比属性类型
    public static final int ATTR_PERCENT_ID_TYPE = 0x0004;

    //全部属性类型
    public static final int ATTR_ID_TYPE_ALL = ATTR_FINAL_ID_TYPE | ATTR_VALUE_ID_TYPE | ATTR_PERCENT_ID_TYPE;


}
