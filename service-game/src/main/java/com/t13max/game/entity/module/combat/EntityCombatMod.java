package com.t13max.game.entity.module.combat;

import com.t13max.common.event.GameEventBus;
import com.t13max.game.consts.UnitBits;
import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.PlayerEntity;
import com.t13max.game.entity.module.EntityModule;
import com.t13max.game.entity.module.combat.attachment.CombatAttachment;
import com.t13max.game.entity.module.reaction.ReactionTriggerEnum;
import com.t13max.game.entity.module.skill.DamageConst;
import com.t13max.game.event.OnEntityBeHit;
import com.t13max.game.event.OnEntityCombatStatusChange;
import com.t13max.game.event.OnPlayerHit;
import com.t13max.game.event.OnPlayerKill;
import com.t13max.game.util.TickTimer;
import com.t13max.template.temp.TemplateMagic;
import com.t13max.template.temp.TemplateMagicEffect;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 战斗模块
 *
 * @author t13max
 * @since 15:01 2024/12/11
 */
public class EntityCombatMod extends EntityModule {

    //战斗附件
    private final Map<Class<?>, CombatAttachment> attachments = new HashMap<>();
    //刷新计时器(发送消息)
    private final TickTimer flushTimer = new TickTimer(500);
    //在战斗状态
    @Getter
    private boolean inCombat;
    private final DamageCache damageCache;
    private Set<Long> discoverers;
    @Setter
    @Getter
    private IEntity currentAttacker;

    public EntityCombatMod(IEntity owner) {
        super(owner);
        damageCache = new DamageCache(owner);
    }

    @Override
    public int pulse(long now) {
        super.pulse(now);
        if (!damageCache.isEmpty() && flushTimer.isPeriod(now)) {
            damageCache.flush();
        }
        if (currentAttacker != null) {
            currentAttacker = null;
        }
        return 0;
    }

    @Override
    public int pulsePerSec(long now) {
        super.pulsePerSec(now);

        if (!attachments.isEmpty()) {
            attachments.forEach((key, value) -> value.tick(now));
        }
        return 0;
    }

    @Override
    public void enterWorld() {
        if (!attachments.isEmpty()) {
            attachments.forEach((key, value) -> value.onEnterWorld());
        }
    }

    @Override
    public void leaveWorld() {
        if (!attachments.isEmpty()) {
            attachments.forEach((key, value) -> value.onLeaveWorld());
        }
    }

    public EntityCombatMod addAttachment(CombatAttachment attachment) {
        attachments.put(attachment.getClass(), attachment);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends CombatAttachment> T removeAttachment(Class<T> clazz) {
        return (T) attachments.remove(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T extends CombatAttachment> T getAttachment(Class<T> clazz) {

        return (T) attachments.get(clazz);
    }

    /**
     * 重生
     *
     * @Author t13max
     * @Date 15:29 2024/12/11
     */
    public void onRespawn() {
        if (!attachments.isEmpty()) {
            attachments.forEach((key, value) -> value.onRespawn());
        }
    }

    /**
     * 被技能击中
     *
     * @Author t13max
     * @Date 15:29 2024/12/11
     */
    public void onBeHit(IEntity caster, TemplateMagic templateMagic, CombatContextResult result, TemplateMagicEffect templateMagicEffect) {
        GameEventBus.inst().postEvent(new OnEntityBeHit(owner, caster, result));
        if (!attachments.isEmpty()) {
            attachments.forEach((key, value) -> value.beHit(caster, templateMagic, result, templateMagicEffect));
        }
    }

    /**
     * 技能击中目标
     *
     * @Author t13max
     * @Date 15:30 2024/12/11
     */
    public void onHit(IEntity target) {

        PlayerEntity worldPlayer = owner.queryObject(PlayerEntity.class);
        if (worldPlayer != null) {
            GameEventBus.inst().postEvent(new OnPlayerHit(target));
        }
    }

    /**
     * 被击杀事件
     *
     * @Author t13max
     * @Date 15:43 2024/12/11
     */
    public void onKillBy(IEntity caster) {
        // 死亡事件
        owner.onDeath(caster);
        if (!attachments.isEmpty()) {
            attachments.forEach((key, value) -> value.onDead());
        }
    }


    /**
     * 击杀目标事件
     *
     * @Author t13max
     * @Date 15:45 2024/12/11
     */
    public void onKillTarget(IEntity realKiller, IEntity target) {
        PlayerEntity playerEntity = owner.queryObject(PlayerEntity.class);
        if (playerEntity != null) {
            GameEventBus.inst().postEvent(new OnPlayerKill(playerEntity, realKiller, target));
        }
    }

    /**
     * 护盾消耗
     *
     * @Author t13max
     * @Date 15:47 2024/12/11
     */
    public void costShield(float damage) {
        if (damage < DamageConst.NIL) {
            return;
        }
        if (owner.getEntityModules().getAttrMod().getTotalShieldValue() > 0) {
            owner.getEntityModules().getAttrMod().reduceShieldValue(damage);
        }
    }

    /**
     * 进入战斗
     *
     * @Author t13max
     * @Date 15:52 2024/12/11
     */
    public void enterCombat() {
        if (this.inCombat) {
            return;
        }

        this.inCombat = true;
        //发送消息
        //SceneMsgHelper.sendCombatStatusToView(owner, this.inCombat);

        if (!this.attachments.isEmpty()) {
            attachments.forEach((key, value) -> value.onEnterCombat());
        }

        owner.getEntityModules().getReactionMod().trigger(ReactionTriggerEnum.REACTION_ENTER_COMBAT, null);

        GameEventBus.inst().postEvent(new OnEntityCombatStatusChange(owner, inCombat));
    }

    /**
     * 脱离战斗
     *
     * @Author t13max
     * @Date 15:55 2024/12/11
     */
    public void leaveCombat() {
        if (!this.inCombat) {
            return;
        }

        this.inCombat = false;

        //发送消息
        //SceneMsgHelper.sendCombatStatusToView(owner, this.inCombat);

        if (!this.attachments.isEmpty()) {
            attachments.forEach((key, value) -> value.onLeaveCombat());
        }

        owner.getEntityModules().getReactionMod().trigger(ReactionTriggerEnum.REACTION_LEAVE_COMBAT, null);

        owner.getEntityModules().getStationMod().leaveAround();

        GameEventBus.inst().postEvent(new OnEntityCombatStatusChange(owner, inCombat));

    }

    public void invisibilityDiscovered(IEntity discoverer) {
        if (discoverers == null) {
            discoverers = new HashSet<>();
        }
        discoverers.add(discoverer.getId());
    }

    public void invisibilityLost(IEntity unitObj) {
        if (discoverers != null) {
            discoverers.remove(unitObj.getId());
        }
    }

    public boolean isInvisibility(IEntity observer) {
        if (owner.getBit(UnitBits.INVISIBLE)) {
            return discoverers == null || !discoverers.contains(observer.getId());
        }

        return false;
    }

}
