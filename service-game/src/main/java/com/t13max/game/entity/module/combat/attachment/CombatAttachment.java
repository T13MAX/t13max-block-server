package com.t13max.game.entity.module.combat.attachment;

import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.combat.CombatContextResult;
import com.t13max.template.temp.TemplateMagic;
import com.t13max.template.temp.TemplateMagicEffect;

/**
 * 战斗附件
 *
 * @author t13max
 * @since 14:52 2024/12/11
 */
public abstract class CombatAttachment {

    protected IEntity owner;

    public CombatAttachment(IEntity owner) {
        this.owner = owner;
    }

    //tick
    public abstract void tick(long now);

    //进入世界
    public abstract void onEnterWorld();

    //离开世界
    public abstract void onLeaveWorld();

    //进入战斗
    public abstract void onEnterCombat();

    //脱离战斗
    public abstract void onLeaveCombat();

    //被攻击
    public void beHit(IEntity caster, TemplateMagic templateMagic, CombatContextResult result, TemplateMagicEffect templateMagicEffect) {
    }

    //死亡
    public void onDead() {
    }

    //重生
    public void onRespawn() {
    }
}
