package com.t13max.game.entity.module.aura;

import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.skill.MagicLogic;
import lombok.Data;

/**
 * @Author t13max
 * @Date 17:51 2024/12/11
 */
@Data
public class AuraEffectContext {

    //释放者
    public final IEntity operator;
    //释放者
    public final IEntity casterOrNull;
    //释放目标
    public final IEntity target;
    //光环对象
    public final Aura aura;
    //光环效果对象
    public final AuraEffect auraEffect;
    //光环目标对象
    public final AuraBuff auraBuff;
    //光环阶段
    public final int mode;

    public AuraEffectContext(IEntity casterOrNull, IEntity target, Aura aura, AuraEffect auraEffect, AuraBuff auraBuff, int mode) {
        this.casterOrNull = casterOrNull;
        this.target = target;
        this.aura = aura;
        this.auraEffect = auraEffect;
        this.auraBuff = auraBuff;
        this.mode = mode;
        MagicLogic magicLogic = aura.getMagicOrNull();
        if (magicLogic != null) {
            this.operator = magicLogic.getProxy();
        } else {
            operator = null;
        }
    }
}
