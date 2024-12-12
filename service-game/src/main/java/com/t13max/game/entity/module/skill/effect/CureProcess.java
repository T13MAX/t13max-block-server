package com.t13max.game.entity.module.skill.effect;


import com.t13max.game.consts.UnitBits;
import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.reaction.ReactionParam;
import com.t13max.game.entity.module.reaction.ReactionEnum;
import com.t13max.game.entity.module.skill.DamageConst;

/**
 * 治疗处理器
 *
 * @Author t13max
 * @Date 16:08 2024/12/11
 */
public class CureProcess {

    /**
     * 标准治疗回血流程
     *
     * @Author t13max
     * @Date 16:09 2024/12/11
     */
    public static void recoverHp(IEntity caster, IEntity target, int magicSn, float value) {
        recoverHp(caster, target, magicSn, 0, value);
    }

    public static void recoverHp(IEntity caster, IEntity target, int magicSn, int effectSn, float value) {
        // 死亡不能回血
        if (target.isDead()) {
            return;
        }
        // 禁疗
        if (target.getBit(UnitBits.ANTI_HEAL)) {
            return;
        }

        float diff = target.getEntityModules().getAttrMod().modHp(value);
        ReactionParam param = new ReactionParam(value, diff, target, magicSn);
        target.getEntityModules().getReactionMod().trigger(ReactionEnum.REACTION_BE_CURED, param);
        if (caster != null) {
            caster.getEntityModules().getReactionMod().trigger(ReactionEnum.REACTION_CURE_OBJ, param);
        }

        if (diff < DamageConst.EPSILON) {
            return;
        }

        //发送消息
        //target.sendMsgToView(msg.build(), 0L);
    }
}
