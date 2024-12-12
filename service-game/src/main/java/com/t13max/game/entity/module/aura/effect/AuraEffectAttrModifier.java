package com.t13max.game.entity.module.aura.effect;


import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.aura.AuraEffectContext;

/**
 * 属性修改
 *
 * @Author t13max
 * @Date 16:36 2024/12/11
 */
@AuraEffectListener(AuraBuffEffectType.Attr)
public class AuraEffectAttrModifier extends AuraEffectTrigger {

    @Override
    public void onStart(IEntity target, AuraEffectContext context) {
        //添加修改
    }


    @Override
    public void onEnd(IEntity target, AuraEffectContext context) {

        //移除修改
    }
}
