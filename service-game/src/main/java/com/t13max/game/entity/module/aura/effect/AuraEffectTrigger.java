package com.t13max.game.entity.module.aura.effect;

import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.aura.AuraConst;
import com.t13max.game.entity.module.aura.AuraEffectContext;

/**
 * @author t13max
 * @since 16:27 2024/12/11
 */
public abstract class AuraEffectTrigger {

    public void trigger(AuraEffectContext context) {
        switch (context.getMode()) {
            case AuraConst.AURA_EFFECT_START:
                onStart(context.getTarget(), context);
                break;
            case AuraConst.AURA_EFFECT_TICK:
                onTick(context.getTarget(), context);
                break;
            case AuraConst.AURA_EFFECT_CLEANUP:
                onEnd(context.getTarget(), context);
                break;
        }
    }

    protected void onStart(IEntity target, AuraEffectContext context) {

    }

    protected void onTick(IEntity target, AuraEffectContext context) {

    }

    protected void onEnd(IEntity target, AuraEffectContext context) {

    }
}
