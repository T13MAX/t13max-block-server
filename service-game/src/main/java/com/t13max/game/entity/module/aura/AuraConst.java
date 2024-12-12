package com.t13max.game.entity.module.aura;

/**
 * @author t13max
 * @since 16:28 2024/12/11
 */
public interface AuraConst {

    //同一施法者同一光环共存上限
    int MAX_SAME_AURA_SIZE = 10;
    //Aura效果最大数量 
    int MAX_AURAS_EFFECTS = 6;
    //同一友好度App上限 
    int MAX_AURA_APPS_PER_FRIENDLY = 50;

    //---------------------------------------------------------------------
    // 光环配置标记位

    //光环只自己可见 
    int AURA_FLAGS_PRIVATE = 0x01;

    //----------------------------------光环生效阶段---------------------------------

    //开始触发 
    int AURA_EFFECT_START = 1;
    //周期触发 
    int AURA_EFFECT_TICK = 2;
    //结束触发 
    int AURA_EFFECT_CLEANUP = 4;

    //---------------------------------------------------------------------
    // 光环移除类型

    //默认 
    int AURA_REMOVE_BY_DEFAULT = 1;
    //取消 
    int AURA_REMOVE_BY_CANCEL = 2;
    //失效 
    int AURA_REMOVE_BY_INVALID = 3;
    //过期 
    int AURA_REMOVE_BY_EXPIRE = 4;
    //死亡 
    int AURA_REMOVE_BY_DEATH = 5;
    //替换 
    int AURA_REMOVE_BY_REPLACE = 6;

    //---------------------------------------------------------------------
    // 光环标记位

    //被免疫 
    int AURA_FLAGS_ASTRICT_IMMUNIZING = 0x01;
    //忽略模块操作(切图时不添加，离开世界不删除) 
    int AURA_FLAGS_IGNORE_OPER_MODULE = 0x02;

    //---------------------------------------------------------------------
    // 光环/buff的有益性

    //增益 
    int TYPE_BENEFIT = 0;
    //减益 
    int TYPE_HARM = 1;
    //中性 
    int TYPE_NORMAL = 2;

    //---------------------------------------------------------------------
    // 光环冲抵关系 顺序不能换 有大小比对关系

    //共存 
    int AURA_SIDE_EFFECT_ATTACH = 0;
    //刷新已存在的同buff 
    int AURA_SIDE_EFFECT_REFRESH_EXISTS = 1;
    //叠加 
    int AURA_SIDE_EFFECT_OVERLAP = 2;
    //叠加并刷新时间 
    int AURA_SIDE_EFFECT_OVERLAP_REFRESH = 3;
    //加时间 
    int AURA_SIDE_EFFECT_APPEND_DURATION = 4;
    //冲抵/覆盖/替换 
    int AURA_SIDE_EFFECT_REPLACE = 5;
    //免疫 
    int AURA_SIDE_EFFECT_IMMUNE = 6;


    //-----------------------------配置表冲抵关系----------------------------------------
    //叠加 刷新时间
    int AURA_CONFLICT_OVERLAP_REFRESH = 0;
    //冲抵 
    int AURA_CONFLICT_REPLACE = 1;
    //免疫 
    int AURA_CONFLICT_IMMUNE = 2;
    //共存 
    int AURA_CONFLICT_COEXISTENCE = 3;
    //刷新已有的同buff 
    int AURA_CONFLICT_REFRESH_EXISTS = 4;
    //增加已存在buff时间 
    int AURA_CONFLICT_APPEND_DURATION = 5;
    //叠加 
    int AURA_CONFLICT_OVERLAP = 6;

    //---------------------------------------------------------------------
    // 光环目标标记位

    //异步清除 
    int AURA_APP_FLAGS_ASYNC_CLEAN = 0x0001;
    //清除消息已发送 
    int AURA_APP_FLAGS_CLEAN_MSG_SENT = 0x0002;


    int MAGIC_SINGLE_TARGET = 1;

}
