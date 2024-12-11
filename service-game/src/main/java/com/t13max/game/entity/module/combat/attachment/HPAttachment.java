package com.t13max.game.entity.module.combat.attachment;

import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.PlayerEntity;
import com.t13max.game.entity.module.attr.AttrKey;
import com.t13max.game.entity.module.skill.effect.CureProcess;
import com.t13max.game.util.TickTimer;
import com.t13max.template.temp.TemplateWorld;

import java.util.concurrent.TimeUnit;

/**
 * 血量附件
 *
 * @author t13max
 * @since 16:02 2024/12/11
 */
public class HPAttachment extends CombatAttachment {

    private final static int HP_RECOVER_PARAM = 1;
    private final static int HP_RECOVER_TIMER = 1;

    //HP恢复计时器
    private final TickTimer hpRecoverTimer = new TickTimer();

    //场景百分比回血计时器
    private final TickTimer hpRecoverSceneTimer = new TickTimer();

    //百分比回血
    private float recoverHpPercent = 0;

    public HPAttachment(IEntity owner) {
        super(owner);
    }

    @Override
    public void tick(long now) {
        if (owner.isDead() || owner.getEntityModules().getAttrMod().isFullHp()) {
            return;
        }

        // 属性HP恢复
        if (hpRecoverTimer.isPeriod(now)) {
            float recover = owner.getEntityModules().getAttrMod().getAttr(AttrKey.hpRecovery);
            if (owner.getEntityModules().getCombatMod().isInCombat()) {
                CureProcess.recoverHp(owner, owner, 0, recover);
            } else {
                CureProcess.recoverHp(owner, owner, 0, recover * HP_RECOVER_PARAM);//读表!
            }
        }

        // 脱战百分比回血
        if (hpRecoverSceneTimer.isPeriod(now)) {
            float recover = owner.getEntityModules().getAttrMod().getHpMax() * recoverHpPercent;
            if (recover > 0) {
                CureProcess.recoverHp(owner, owner, 0, recover);
            }
        }
    }

    @Override
    public void onEnterWorld() {
        this.hpRecoverTimer.start(TimeUnit.SECONDS.toMillis(HP_RECOVER_TIMER));

        if (owner instanceof PlayerEntity) {
            TemplateWorld templateWorld = owner.getWorld().getTemplateWorld();
            if (templateWorld != null && templateWorld.perRecovery.length >= 2) {
                hpRecoverSceneTimer.start(TimeUnit.SECONDS.toMillis(templateWorld.perRecovery[0]));
                recoverHpPercent = templateWorld.perRecovery[1] / 100.f;
            } else {
                hpRecoverSceneTimer.stop();
                recoverHpPercent = 0.f;
            }
        }
    }

    @Override
    public void onLeaveWorld() {
        hpRecoverTimer.stop();
        hpRecoverSceneTimer.stop();
    }

    @Override
    public void onEnterCombat() {
        hpRecoverSceneTimer.stop();
    }

    @Override
    public void onLeaveCombat() {
        if (owner instanceof PlayerEntity) {
            hpRecoverSceneTimer.reStart();
        }
    }
}
