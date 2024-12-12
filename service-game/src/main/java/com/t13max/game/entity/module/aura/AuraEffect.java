package com.t13max.game.entity.module.aura;


import com.t13max.game.consts.Const;
import com.t13max.game.entity.module.aura.effect.AuraEffectListener;
import com.t13max.game.entity.module.aura.effect.AuraEffectTrigger;
import com.t13max.game.exception.CommonException;
import com.t13max.game.exception.GameException;
import com.t13max.template.helper.AuraEffectHelper;
import com.t13max.template.manager.TemplateManager;
import com.t13max.template.temp.TemplateMagicEffect;
import com.t13max.util.PackageUtil;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 光环效果
 *
 * @Author t13max
 * @Date 17:49 2024/12/11
 */
@Getter
public class AuraEffect {

    private final static Map<Integer, AuraEffectTrigger> TRIGGER_MAP = new HashMap<>();

    static {

        try {
            Set<Class<?>> classSet = PackageUtil.scan("com.t13max.game.entity.module.aura.effect");
            //创建实例
            for (Class<?> clazz : classSet) {
                // 只需要加载TemplateHelper注解数据
                if (!AuraEffectTrigger.class.isAssignableFrom(clazz) || Modifier.isAbstract(clazz.getModifiers())) {
                    continue;
                }

                // 创建实例
                Object inst = clazz.getDeclaredConstructor().newInstance();
                AuraEffectTrigger trigger = (AuraEffectTrigger) inst;
                AuraEffectListener annotation = trigger.getClass().getAnnotation(AuraEffectListener.class);
                if (annotation == null) {
                    continue;
                }
                int type = annotation.value();
                TRIGGER_MAP.put(type, trigger);
            }

        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new CommonException(e);
        }

    }

    public static void trigger(int type, AuraEffectContext context) {
        AuraEffectTrigger trigger = TRIGGER_MAP.get(type);
        trigger.trigger(context);
    }

    //光环配置
    private TemplateMagicEffect templateMagicEffect;
    private final int index;
    //光环类型
    private final int type;
    //剩余周期触发时间
    private long periodTime;

    public AuraEffect(int index, int effectId, int magicLv) {
        this.index = index;
        this.templateMagicEffect = TemplateManager.inst().helper(AuraEffectHelper.class).getTemplate(effectId);
        if (templateMagicEffect == null) {
            throw new GameException("ConfAuraEffect notfound, effectId:" + effectId);
        }
        this.type = templateMagicEffect.type;
        this.periodTime = Math.max(templateMagicEffect.upgrade,  Const.TICK_INTERVAL);
    }

    public int update(Aura aura, long diff) {
        int tickTIme = Math.max(templateMagicEffect.upgrade,  Const.TICK_INTERVAL);

        long remain = diff - this.periodTime;
        this.periodTime = Math.max(0L, this.periodTime - diff);

        if (this.periodTime == 0L) {
            this.periodTime = tickTIme - remain;
            aura.doEffect(this, AuraConst.AURA_EFFECT_TICK);
        }

        return 0;
    }

    /**
     * 执行单个效果
     *
     * @Author t13max
     * @Date 17:49 2024/12/11
     */
    public void handleEffect(AuraBuff buff, int mode) {
        Aura aura = buff.getAura();
        AuraEffectContext context = new AuraEffectContext(aura.getCasterOrNull(), buff.getTargetAnyway(), aura, this, buff, mode);
        trigger(templateMagicEffect.type, context);
    }

}
