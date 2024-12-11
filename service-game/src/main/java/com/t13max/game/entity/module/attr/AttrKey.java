package com.t13max.game.entity.module.attr;

/**
 * @author t13max
 * @since 16:05 2024/12/11
 */
public interface AttrKey {

    //生命值
    int hpLimit = 10;
    int hpLimit_v = 11;
    int hpLimit_p = 12;

    //能量值
    int xpLimit = 20;
    int xpLimit_v = 21;
    int xpLimit_p = 22;

    //生命恢复速度
    int hpRecovery = 30;
    int hpRecovery_v = 31;
    int hpRecovery_p = 32;

    //能量恢复速度
    int xpRecovery = 40;
    int xpRecovery_v = 41;
    int xpRecovery_p = 42;

    //当前生命
    int hp = 50;
    int hp_v = 51;
    int hp_p = 52;

    //当前能量
    int xp = 60;
    int xp_v = 61;
    int xp_p = 62;

    //攻击
    int attack = 70;
    int attack_v = 71;
    int attack_p = 72;

    //魔法攻击
    int magicAttack = 80;
    int magicAttack_v = 81;
    int magicAttack_p = 82;

    //防御
    int def = 90;
    int def_v = 91;
    int def_p = 92;

    //魔法防御
    int magicDef = 100;
    int magicDef_v = 101;
    int magicDef_p = 102;

    //命中
    int hit = 110;
    int hit_v = 111;
    int hit_p = 112;

    //闪避
    int dodge = 120;
    int dodge_v = 121;
    int dodge_p = 122;
}
