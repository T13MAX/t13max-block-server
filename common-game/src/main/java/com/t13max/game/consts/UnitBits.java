package com.t13max.game.consts;

/**
 * 标记位常量
 *
 * @author t13max
 * @since 17:01 2024/12/9
 */
public interface UnitBits {
    
    //移动中
    int MOVING = 0;
    //无敌的,不受攻击,不可选为攻击对象
    int INVINCIBLE = 1;
    //免疫的,可受攻击,但伤害为0
    int IMMUNIZING = 2;
    //眩晕
    int STUNNED = 3;
    //禁足
    int GROUNDED = 4;
    //免疫knock
    int UN_KNOCK = 5;
    //沉默，只能释放部分技能
    int SILENT = 6;
    //安全区
    int SAFE_AREA = 7;
    //免伤的，正常结算，目标不掉血
    int INJURE_FREE = 8;
    //混乱*/
    int CONFUSION = 9;
    //不死
    int UNDEAD = 10;
    //禁疗
    int ANTI_HEAL = 11;
    //施法中
    int CASTING = 20;
    //位移中
    int SHIFTING = 21;
    //忽略光环
    int IGNORE_AURAS = 22;
    //硬直
    int STIFF = 23;
    //跳跃中
    int JUMP = 24;


    //已死亡
    int DEAD =32;
    //战斗移动
    int FIGHT_MOVE = 33;
    //动作表现
    int ACTION = 34;
    //观战切图
    int WATCH_SWITCH = 35;
    //复活中
    int REVIVING = 36;
    //假隐身
    int INVISIBLE = 38;
    //锁血
    int LOCK_HP = 40;
    //单向隐藏 别人不会受到进入消息
    int SINGLE_HIDDEN = 41;
    //跨服限时首领标志
    int CROSS_TIME_LIMITED_BOSS = 42;
}
