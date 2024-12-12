package com.t13max.game.entity.module.attr;

import com.t13max.game.consts.UnitBits;
import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.EntityModule;
import com.t13max.game.entity.module.aura.AuraBuff;
import com.t13max.game.exception.GameException;
import com.t13max.game.utils.Utils;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 实体属性模块
 *
 * @author: t13max
 * @since: 17:03 2024/7/25
 */
public class EntityAttrMod extends EntityModule {

    //最终属性 以0结尾 通过其他的计算而来
    private final Map<Integer, Float> finalAttr = new HashMap<>(64);
    //效果值，1或者2结尾 用于计算finalAttrs
    private final Map<Integer, Float> effects = new HashMap<>(128);
    //不可变
    private boolean immutable;
    //受到伤害上限
    @Setter
    private float sufferLimit;

    public EntityAttrMod(IEntity owner) {
        super(owner);
    }

    public int initial(EntityAttrMod other) {
        this.finalAttr.clear();
        this.finalAttr.putAll(other.finalAttr);
        return 0;
    }

    public Set<Integer> plus(Map<Integer, Float> attrs) {
        return modify(this.finalAttr, attrs);
    }

    public Set<Integer> minus(Map<Integer, Float> attrs) {
        Map<Integer, Float> attrsTmp = new HashMap<>(attrs);
        attrsTmp.replaceAll((k, v) -> -v);
        return modify(this.finalAttr, attrsTmp);
    }

    private static Set<Integer> modify(Map<Integer, Float> attrs, Map<Integer, Float> modifyAttrs) {
        Set<Integer> modAttrs = new HashSet<>();
        for (Map.Entry<Integer, Float> entry : modifyAttrs.entrySet()) {
            int attrKey = entry.getKey();
            float value = entry.getValue();
            if (Utils.floatEqual(value, 0.0f)) {
                continue;
            }

            attrs.merge(attrKey, value, Float::sum);
            modAttrs.add(attrKey);
        }

        return modAttrs;
    }

    public void setUnCalcAttr(int key, float value) {
        switch (key) {
            case AttrKey.hp:
                setHp(value);
                //SceneMsgHelper.sendUpdateHPToView(unitObject, getHp(), AttrConst.MODHP_TYPE_NONE, AttrConst.MOD_FLAG_SET, unitObject.getId());
                break;
            case AttrKey.xp:
                setXP(value);
                //SceneMsgHelper.sendUpdateXPToOwner(unitObject, value);
                break;
            default:
                throw new GameException("UnSupport attr for setUnCalcAttr， key ->" + key);
        }
    }

    //------------------------------------血量------------------------------------
    public float getHp() {
        return getFloatVal(finalAttr.get(AttrKey.hp));
    }

    public float getHpPercent() {
        float hpLimit = this.getAttr(AttrKey.hpLimit);
        if (Utils.floatEqual(hpLimit, 0.f)) return 0.f;

        return getHp() * 100.0f / this.getAttr(AttrKey.hpLimit);
    }

    public void setHp(float hp) {
        if (owner.getBit(UnitBits.LOCK_HP)) {
            return;
        }

        finalAttr.put(AttrKey.hp, Math.max(0.0f, hp));
    }

    public float modHp(float value) {
        double hp = getHp();
        double curr = Math.min(Math.max(0.d, hp + value), this.getHpMax());
        float result = (float) (curr - hp);

        owner.setHp((float) curr);

        return result;
    }

    public float modHp(float value, boolean sendMsg) {
        float result = modHp(value);
        if (sendMsg) {
            //SceneMsgHelper.sendUpdateHPToView(unitObject, getHp(), AttrConst.MODHP_TYPE_NONE, AttrConst.MOD_FLAG_SET, unitObject.getId());
        }
        return result;
    }

    public void fullHP(boolean sendMsg) {
        this.modHp(this.getHpMax());

        if (sendMsg) {
            //SceneMsgHelper.sendUpdateHPToView(unitObject, getHp(), AttrConst.MODHP_TYPE_NONE, AttrConst.MOD_FLAG_SET, unitObject.getId());
        }
    }

    public boolean isFullHp() {
        return getHp() == getHpMax();
    }

    public float getHpMax() {
        return this.getAttr(AttrKey.hpLimit);
    }

    public float getXP() {
        return getFloatVal(finalAttr.get(AttrKey.xp));
    }

    public void setXP(float xp) {
        finalAttr.put(AttrKey.xp, Math.max(0.0f, xp));
    }

    public float modXP(float value) {
        float xp = getXP();
        float curr = Math.min(Math.max(0.f, xp + value), this.getXPMax());
        setXP(curr);
        return curr;
    }

    public float getXPMax() {
        return this.getAttr(AttrKey.xpLimit);
    }

    /**
     * 改变基础移速
     *
     * @Author t13max
     * @Date 17:08 2024/12/11
     */
    public void changeBaseSpeed(float oldSpeed, float newSpeed) {
        detachEffect(AttrKey.moveSpeed_v, oldSpeed);
        attachEffect(AttrKey.moveSpeed_v, newSpeed);

        updateAllFinalAttrs(true);
    }

    public float getFloatVal(Float val) {
        return (val == null || val.isNaN()) ? 0f : val;
    }

    /**
     * 增加一个属性效果
     *
     * @Author t13max
     * @Date 17:09 2024/12/11
     */
    public void attachEffect(int effectType, float value) {
        int check = effectType % AttrConst.ATTR_SPACE_CONST;
        if (check != 1 && check != 2) {
            return;
        }
        Float oldValue = effects.get(effectType);
        if (oldValue == null) {
            effects.put(effectType, value);
        } else {
            float newValue = oldValue + value;
            effects.put(effectType, newValue);
        }

    }

    protected float makePositiveVal(float val) {
        return Math.max(0, val);
    }

    public void detachEffect(int effectType, float value) {
        Float oldValue = effects.get(effectType);
        if (oldValue == null) {
            oldValue = Utils.FLOAT_ZERO;
        }

        float newValue = oldValue;
        //值的效果以累加的结果保存
        if (effectType % AttrConst.ATTR_SPACE_CONST == 1) {
            // 记住，这里不再取整，因为有的效果就是小于1的，例如伤害修正类
            newValue = oldValue - value;
        } else if (effectType % AttrConst.ATTR_SPACE_CONST == 2) {
            //百分比的效果以累乘的结果保存
            newValue = (1 + oldValue) / (1 + value) - 1;
        }
        effects.put(effectType, newValue);
    }

    public void attachEffects(Map<Integer, Float> effects) {
        if (effects == null || effects.isEmpty()) {
            return;
        }
        for (Map.Entry<Integer, Float> entry : effects.entrySet()) {
            int effectType = entry.getKey();
            if (effectType % AttrConst.ATTR_SPACE_CONST == 2 && entry.getValue() <= -1.0f) {
                //在attach是规避一下，防止在detach时除数为0的问题
                entry.setValue(-0.95f);
            }
            attachEffect(effectType, entry.getValue());
        }
    }

    public void detachEffects(Map<Integer, Float> effects) {
        if (effects == null || effects.isEmpty()) {
            return;
        }

        for (Map.Entry<Integer, Float> entry : effects.entrySet()) {
            int effectType = entry.getKey();
            if (effectType % AttrConst.ATTR_SPACE_CONST == 2 && entry.getValue() == -1.0f) {
                //防止在detach时除数为0的问题
                entry.setValue(-0.95f);
            }
            detachEffect(effectType, entry.getValue());
        }
    }

    protected float calcEffectBonus(int attrType, float attrValue) {
        Float abl = effects.get(attrType + 1);
        if (abl == null) {
            abl = Utils.FLOAT_ZERO;
        }

        Float pct = effects.get(attrType + 2);
        if (pct == null) {
            pct = Utils.FLOAT_ZERO;
        }
        float value = (attrValue + abl) * (1 + pct);
        return makePositiveVal(value);
    }

    public void resetEffects(Map<Integer, Float> effects, float ratio, int flags) {
        if (effects == null || effects.isEmpty()) {
            return;
        }

        this.effects.clear();
        for (Map.Entry<Integer, Float> entry : effects.entrySet()) {
            int effectType = entry.getKey();
            int idType = effectType % AttrConst.ATTR_SPACE_CONST;
            float value = entry.getValue() * ratio;
            switch (idType) {
                case 0: {
                    if ((flags & AttrConst.ATTR_FINAL_ID_TYPE) <= 0) {
                        continue;
                    }
                    break;
                }
                case 1: {
                    if ((flags & AttrConst.ATTR_VALUE_ID_TYPE) <= 0) {
                        continue;
                    }
                    break;
                }
                case 2: {
                    if ((flags & AttrConst.ATTR_PERCENT_ID_TYPE) <= 0) {
                        continue;
                    }

                    if (value <= -1.0f) {
                        // 再attach是规避一下，防止在detach时除数为0的问题
                        value = -0.95f;
                    }
                    break;
                }
                default: {
                    continue;
                }
            }

            attachEffect(effectType, value);
        }
    }

    public void resetEffects(Map<Integer, Float> effects) {
        resetEffects(effects, 1.0f, AttrConst.ATTR_ID_TYPE_ALL);
    }

    /**
     * 根据effect重新计算属性值
     *
     * @return
     */
    public Map<Integer, Float> updateAllFinalAttrs() {
        return updateAllFinalAttrs(false);
    }

    /**
     * 根据effect重新计算属性值
     *
     * @return
     */
    public Map<Integer, Float> updateAllFinalAttrs(boolean notify) {
        if (immutable) {
            // 不可修改
            return Collections.emptyMap();
        }
        HashMap<Integer, Float> changedFinalAttrs = new HashMap<>(16);

        boolean notifySelf = false;
        boolean notifyAround = false;

        float oldHpPercent = getHpPercent() / 100;

        for (int attrId : AttrKey.MAIN_ATTRS) {
            //体力，血，能量等 是不能参与计算的，它们只是一个标量，没有反应玩家战斗能力
            if (!isCalculationAttr(attrId)) {
                continue;
            }

            Float newValue = calcFinalAttr(attrId);

            Float oldValue = finalAttr.get(attrId);
            if (oldValue == null) {
                oldValue = Utils.FLOAT_ZERO;
            }

            //新值跟旧值不一样
            if (!oldValue.equals(newValue)) {
                finalAttr.put(attrId, newValue);
                changedFinalAttrs.put(attrId, newValue);
            }
        }

        // 最大血量发生变化 血量做百分比折算
        if (changedFinalAttrs.containsKey(AttrKey.hpLimit) && oldHpPercent > 0) {
            float hp = getHpMax() * oldHpPercent;
            setHp(hp);
            changedFinalAttrs.put(AttrKey.hp, hp);
        }
        if (changedFinalAttrs.containsKey(AttrKey.xpLimit)) {
            changedFinalAttrs.put(AttrKey.xp, Math.min(this.getXP(), this.getXPMax()));
        }

        //发送消息

        return changedFinalAttrs;
    }

    public boolean isCalculationAttr(int attrId) {
        switch (attrId) {
            case AttrKey.hp:
            case AttrKey.xp:
                return false;
        }
        return true;
    }

    public float calcFinalAttr(int attrId) {
        return calcEffectBonus(attrId, 0f);
    }

    public final float getAttr(int attrKey) {
        return makePositiveVal(getFloatVal(finalAttr.get(attrKey)));
    }

    public final void setAttr(int attrKey, float value) {
        finalAttr.put(attrKey, value);
    }

    public Map<Integer, Float> shallowCopy() {
        return finalAttr;
    }

    public Map<Integer, Float> finalAttrDeepCopy() {
        return new HashMap<>(this.finalAttr);
    }

    public Map<Integer, Float> effectsDeepCopy() {
        return new HashMap<>(this.effects);
    }

    public void clearEffect() {
        this.effects.clear();
    }

    public Map<Integer, Float> getFinalAttr() {
        return finalAttr;
    }

    public float getHpRecoverSpeed() {
        return getFloatVal(finalAttr.get(AttrKey.hpRecovery));
    }

    public float getXpRecoverSpeed() {
        return getFloatVal(finalAttr.get(AttrKey.xpRecovery));
    }

    public float getMoveSpeed() {
        return getFloatVal(finalAttr.get(AttrKey.moveSpeed));
    }

    public float getSufferDamageLimit() {
        if (sufferLimit < 0.0001f) {
            return 0.f;
        }

        return getHpMax() * sufferLimit;
    }

}

