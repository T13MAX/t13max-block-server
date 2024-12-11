package com.t13max.game.entity.module.combat.attachment;

import com.t13max.game.entity.IEntity;

/**
 * 光环附件
 *
 * @author t13max
 * @since 15:59 2024/12/11
 */
public class AuraAttachment extends CombatAttachment {

    private final int[] auraArray;

    public AuraAttachment(IEntity owner, int[] auraArray) {
        super(owner);
        this.auraArray = auraArray;
    }

    @Override
    public void tick(long now) {

    }

    @Override
    public void onEnterWorld() {

    }

    @Override
    public void onLeaveWorld() {

    }

    @Override
    public void onEnterCombat() {
        for (int auraSn : auraArray) {
            owner.getEntityModules().getAuraMod().createAura(auraSn);
        }
    }

    @Override
    public void onLeaveCombat() {
        for (int auraSn : auraArray) {
            owner.getEntityModules().getAuraMod().deleteAura(auraSn);
        }
    }
}
