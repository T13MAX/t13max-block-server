package com.t13max.game.entity.module.skill;

/**
 * @author t13max
 * @since 11:52 2024/12/12
 */
public interface MagicConst {
    //---------------------------------------------------------------------
    // 法术激发标记位

    //不转方向 */
    int TRIGGERED_IGNORE_DIRECTION = 0x0001;
    //无施法状态 */
    int TRIGGERED_IGNORE_CASTING = 0x0002;
    //不结算一切触发事件 */
    int TRIGGERED_IGNORE_REACTION = 0x0004;
    //不检查条件 */
    int TRIGGERED_IGNORE_CONDITION = 0x0008;
    //无消耗 */
    int TRIGGERED_IGNORE_CONSUME = 0x0010;
    //忽略施法开始消息 */
    int TRIGGERED_IGNORE_CAST_BEGAN_MSG = 0x0020;
    //忽略施法结束消息 */
    int TRIGGERED_IGNORE_CAST_ENDED_MSG = 0x0040;
    //无视死亡状态 */
    int TRIGGERED_IGNORE_DEATH = 0x0080;
    //瞬发 */
    int TRIGGERED_IMMEDIATELY = 0x0100;

    //默认标记位 */
    int TRIGGERED_DEFAULT_MASK = TRIGGERED_IGNORE_REACTION | TRIGGERED_IGNORE_CASTING |
            TRIGGERED_IGNORE_CONSUME | TRIGGERED_IGNORE_CONDITION | TRIGGERED_IGNORE_DIRECTION;
    //允许REACTION默认标记位 */
    int TRIGGERED_DEFAULT_MASK_WITH_REACTION = TRIGGERED_DEFAULT_MASK & ~TRIGGERED_IGNORE_REACTION;

    //---------------------------------------------------------------------
    // 法术打断标记位

    //强制打断 */
    int INTERRUPT_FORCE = 0x0001;
    //跳过状态阶段 */
    int INTERRUPT_JUMP_STEPS = 0x0002;
    //不发送打断消息 */
    int INTERRUPT_DONT_SENDMSG = 0x0004;
    //释放其他技能时打断 */
    int INTERRUPT_CAST_OTHER_MAGIC = 0x0008;
    //移动时打断 */
    int INTERRUPT_MOVE = 0x0010;
    //休闲动作打断 */
    int INTERRUPT_ACTION = 0x0020;

    //---------------------------------------------------------------------
    // 施法过程标记位

    //施法中禁止移动 */
    int MAGIC_CASTING_FLAG_BAN_MOVE = 0x0001;
    //施法中禁止转向 */
    int MAGIC_CASTING_FLAG_BAN_TURN = 0x0002;

    //---------------------------------------------------------------------
    // 法术效果类型

    //结算类 */
    int EFFECT_TYPE_COMMON = 1;
    //位移类 */
    int EFFECT_TYPE_MOTION = 2;
    //子弹类 */
    int EFFECT_TYPE_BULLET = 3;
    //专有类 */
    int EFFECT_TYPE_FLEXIBLE = 4;

    //---------------------------------------------------------------------
    // 效果区域类型

    //目标 */
    int REGION_TYPE_SINGLE = 1;
    //原点+偏移+圆 */
    int REGION_TYPE_ORIGIN_OFFSET_ROUND = 2;
    //目标点+偏移+圆 */
    int REGION_TYPE_OFFSET_ROUND = 3;
    //扇形 */
    int REGION_TYPE_SECTOR = 4;
    //原点前方矩形+偏移 */
    int REGION_TYPE_RECTANGLE_ORIGIN = 5;
    //目标前方矩形 */
    int REGION_TYPE_RECTANGLE_TARGET = 6;
    //原点至目标点矩形 */
    int REGION_TYPE_RECTANGLE = 7;
    /***圆环*/
    int REGION_TYPE_RING = 8;

    //---------------------------------------------------------------------
    // 法术效果区域搜索类型

    //1次结算 */
    int SEARCH_TYPE_DEFAULT = 0;
    //区域按距离n等分 */
    int SEARCH_TYPE_REGION = 1;
    //区域按时间n等分 */
    int SEARCH_TYPE_TIME = 2;
    //按人头结算伤害 */
    int SEARCH_COUNTER = 3;

    //---------------------------------------------------------------------
    // 技能释放目标类型

    //位置施放 */
    int CAST_TYPE_POS = 0;

    //目标施放 */
    int CAST_TYPE_OBJ = 1;

    //---------------------------------------------------------------------
    // 移动类效果时间控制类型

    //根据移动距离控制时间 */
    int MOTION_TIME_CONTROL_BY_DIST = 0;
    //时间不变 */
    int MOTION_TIME_CONTROL_BY_TIME = 1;

    //---------------------------------------------------------------------
    // 受击类型

    //击倒 */
    int KNOCK_TYPE_DOWN = 1;
    //击退 */
    int KNOCK_TYPE_BACK = 2;
    //击飞 */
    int KNOCK_TYPE_FLY = 3;

    //---------------------------------------------------------------------
    // 受击效果类型

    //默认 */
    int KNOCK_EFFECT_TYPE_NORMAL = 0;
    //挤开 */
    int KNOCK_EFFECT_TYPE_CROWDED = 1;
    //收缩 */
    int KNOCK_EFFECT_TYPE_SHRINK = 2;

    //---------------------------------------------------------------------
    // 子效果类型

    //受击 */
    int SUBEFFECT_KNOCK = 1;

    //---------------------------------------------------------------------
    // 普通伤害命中结算类型

    //结算伤害 */
    int EFFECT_COMMON_BIT_DAMAGE = 0x0001;
    //结算光环 */
    int EFFECT_COMMON_BIT_AURA = 0x0002;
    //结算治疗 */
    int EFFECT_COMMON_BIT_CURE = 0x0004;
}
