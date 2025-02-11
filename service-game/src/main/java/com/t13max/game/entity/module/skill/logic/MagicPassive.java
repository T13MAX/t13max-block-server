package com.t13max.game.entity.module.skill.logic;


import com.t13max.game.entity.module.reaction.AbstractReaction;
import com.t13max.game.entity.module.skill.Magic;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 被动技能
 *
 * @Author t13max
 * @Date 16:50 2024/12/12
 */

public class MagicPassive extends Magic {
    /***属性*/
    public static final int EFFECT_ATTR = 0;
    /***触发器*/
    public static final int EFFECT_TRIGGER = 1;
    /***效果修改*/
    public static final int EFFECT_MODIFY = 2;
    /***战斗光环*/
    public static final int EFFECT_BATTLE_AURA = 3;
    /***属性转化公式x属性转化为y属性.仅简单公式: y = ax + b*/
    public static final int EFFECT_ATTR_FORMULA = 4;

    @Getter
    private boolean active;

    private ConfMagic confMagic;

    private List<AbstractReaction> globalReaction = new ArrayList<>();

    @Getter
    private Multimap<Integer, AbstractReaction> magicReaction = ArrayListMultimap.create();

    public MagicPassive(ConfMagic confMagic, int level) {
        super(confMagic.sn, level);
        this.confMagic = confMagic;
    }

    public void startup(UnitObject unitObject) {
        if (active) {
            return;
        }

        for (int effect : confMagic.passiveEffects) {
            ConfMagicPassiveEffect conf = ConfMagicPassiveEffect.get(effect);
            if (conf != null) {
                switch (conf.type) {
                    case EFFECT_ATTR: {
                        Map<Integer, Float> attrs = MagicUtils.getPassiveMagicAttrs(this, conf);
                        if (!attrs.isEmpty()) {
                            String key = PermanentBuffConstant.PASSIVE_ATTR + conf.sn;
                            unitObject.getUnitModule().getModPermanentBuff().addPermanentBuff(key, attrs);
                        }
                    }
                    break;
                    case EFFECT_TRIGGER: {
                        ConfCombatTrigger confCombatTrigger = ConfCombatTrigger.get(conf.triggerId);
                        if (confCombatTrigger == null) {
                            LogServer.skill.error("Passive magic init failed ,combatTrigger not found, sn {}", conf.triggerId);
                            break;
                        }
                        MagicReaction reaction = FlexibleReactionActions.doAction(unitObject, sn, level, confCombatTrigger);
                        if (conf.magicSn.length > 0) {
                            for (int magicSn : conf.magicSn) {
                                this.magicReaction.put(magicSn, reaction);
                            }
                        } else {
                            unitObject.getUnitModule().getModReaction().addReaction(reaction);
                            this.globalReaction.add(reaction);
                        }
                    }
                    break;
                    case EFFECT_MODIFY: {
                        ConfEffectModifier confModifier = ConfEffectModifier.get(conf.modifierSn);
                        if (confModifier == null) {
                            LogServer.skill.error("Passive magic init failed, ConfEffectModifier sn {} not found", conf.modifierSn);
                            break;
                        }

                        unitObject.getUnitModule().getModSkill().getModifier().modify(confModifier);
                    }
                    break;
                    case EFFECT_BATTLE_AURA: {
                        CombatAuraAttachment attachment = new CombatAuraAttachment(unitObject, conf.auraSn);
                        unitObject.getUnitModule().getModCombat().addAttachment(attachment);
                    }
                    break;
                    case EFFECT_ATTR_FORMULA: {
                        Map<Integer, Float> attrs = MagicUtils.getPassiveMagicAttrsTrans(unitObject, conf);
                        if (!attrs.isEmpty()) {
                            String key = PermanentBuffConstant.PASSIVE_ATTR + conf.sn;
                            unitObject.getUnitModule().getModPermanentBuff().addPermanentBuff(key, attrs);
                        }
                    }
                    break;
                    default:
                        LogServer.skill.error("UnSupport passive magic effect type {}", conf.type);
                }
            }
        }

        if (LogServer.skill.isDebugEnabled()) {
            LogServer.skill.debug("Name {} passive magic sn {} startup", unitObject.getName(), sn);
        }
        this.active = true;
    }

    public void cleanup(UnitObject unitObject) {
        if (!active) {
            return;
        }

        if (!unitObject.isInWorld()) {
            return;
        }

        // remove magic reaction
        for (AbstractReaction reaction : globalReaction) {
            unitObject.getUnitModule().getModReaction().remReaction(reaction);
        }
        magicReaction.clear();

        for (int effect : confMagic.passiveEffects) {
            ConfMagicPassiveEffect conf = ConfMagicPassiveEffect.get(effect);
            if (conf != null) {
                switch (conf.type) {
                    case EFFECT_ATTR: {
                        Map<Integer, Float> attrs = MagicUtils.getPassiveMagicAttrs(this, conf);
                        if (!attrs.isEmpty()) {
                            String key = PermanentBuffConstant.PASSIVE_ATTR + conf.sn;
                            unitObject.getUnitModule().getModPermanentBuff().removePermanentBuff(key, true);
                        }
                    }
                    break;
                    case EFFECT_TRIGGER:
                        break;
                    case EFFECT_MODIFY: {
                        ConfEffectModifier confModifier = ConfEffectModifier.get(conf.modifierSn);
                        if (confModifier == null) {
                            LogServer.skill.error("Passive magic init failed, ConfEffectModifier sn {} not found", conf.modifierSn);
                            break;
                        }

                        unitObject.getUnitModule().getModSkill().getModifier().reset(confModifier);
                    }
                    break;
                    case EFFECT_BATTLE_AURA: {
                        CombatAuraAttachment attachment = unitObject.getUnitModule().getModCombat().removeAttachment(CombatAuraAttachment.class);
                        if (attachment != null) {
                            attachment.onLeaveCombat();
                        }
                    }
                    break;
                    case EFFECT_ATTR_FORMULA: {
                        Map<Integer, Float> attrs = MagicUtils.getPassiveMagicAttrsTrans(unitObject, conf);
                        if (!attrs.isEmpty()) {
                            String key = PermanentBuffConstant.PASSIVE_ATTR + conf.sn;
                            unitObject.getUnitModule().getModPermanentBuff().removePermanentBuff(key, true);
                        }
                    }
                    break;
                    default:
                        LogServer.skill.error("UnSupport passive magic effect type {}", conf.type);
                }

            }
        }

        if (LogServer.skill.isDebugEnabled()) {
            LogServer.skill.debug("Name {} passive magic sn {} cleanup", unitObject.getName(), sn);
        }
        this.active = false;
        this.globalReaction.clear();
    }
}
