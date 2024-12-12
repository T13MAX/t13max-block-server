package com.t13max.game.entity.module.aura;


import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.IndirectObject;
import com.t13max.game.entity.module.reaction.AbstractReaction;
import com.t13max.template.temp.TemplateAura;
import lombok.Getter;
import world.entity.PBAura;

import java.util.ArrayList;
import java.util.List;

/**
 * 光环buff
 *
 * @Author t13max
 * @Date 17:23 2024/12/11
 */
public class AuraBuff {
    @Getter
    private long id;
    //光环对象
    @Getter
    private Aura aura;
    //标记位
    private int flags;
    //叠加层数
    @Getter
    private int overlap;
    //是否已结束
    private boolean finished;
    //等级
    @Getter
    private int level;
    //目标
    private IEntity target;
    @Getter
    private long targetId;
    //间接对象
    @Getter
    private IndirectObject<AuraBuff> indirectObject;
    //环境参数
    private Object[] objContext = new Object[4];
    //光环反应器
    @Getter
    private List<AbstractReaction> reactions = new ArrayList<>();

    public AuraBuff(Aura aura, IEntity target, int flags, int overlap) {
        this.id = target.getWorld().getWorldModules().getUuidMod().applyRefId();
        this.aura = aura;
        this.flags = flags;
        this.finished = false;

        this.target = target;
        this.targetId = target.getId();
        this.level = aura.getLevel();

        this.overlap = overlap;

        indirectObject = new IndirectObject<>(this);
    }

    public int startup() {
        target.getEntityModules().getAuraMod().addAuraBuff(this);
        target.getEntityModules().getAuraMod().onAuraBuffBegan(this);

        return this.handleEffect(AuraConst.AURA_EFFECT_START);
    }

    void cleanup(int mode) {
        if (!this.finished) {
            this.finished = true;

            if (target != null) {
                this.handleEffect(AuraConst.AURA_EFFECT_CLEANUP);
                target.getEntityModules().getAuraMod().onAuraBuffEnded(this);
                target.getEntityModules().getAuraMod().removeAuraBuff(this);
                for (AbstractReaction reaction : reactions) {
                    target.getEntityModules().getReactionMod().remReaction(reaction);
                }
                reactions.clear();
                target = null;
            }

            indirectObject.invalid();
        }
    }

    public void handleEffect(int index, int mode) {
        AuraEffect effect = this.aura.getEffect(index);
        if (effect != null) {
            effect.handleEffect(this, mode);
        }
    }

    public int handleEffect(int mode) {
        for (int i = 0; i < AuraConst.MAX_AURAS_EFFECTS; ++i) {
            this.handleEffect(i, mode);
        }

        return 0;
    }

    public void manageReaction(AbstractReaction reaction) {

        this.reactions.add(reaction);
    }

    public TemplateAura getTemplateAura() {
        return this.aura.getTemplateAura();
    }


    public boolean isSingleBuff() {

        return getAura().isSingleBuff();
    }

    /**
     * 光环层数增加
     *
     * @Author t13max
     * @Date 18:05 2024/12/11
     */
    void onOverlapIncrement(int overlap, int level) {
        handleEffect(AuraConst.AURA_EFFECT_CLEANUP);
        this.overlap = overlap;
        this.level = level;
        handleEffect(AuraConst.AURA_EFFECT_START);

        if (!finished)
            aura.updateAuraBuff(this);
    }

    /**
     * 光环层数减少
     *
     * @Author t13max
     * @Date 18:05 2024/12/11
     */
    void onOverlapDecrement(int overlap) {
        this.handleEffect(AuraConst.AURA_EFFECT_CLEANUP);
        this.overlap = overlap;
        this.handleEffect(AuraConst.AURA_EFFECT_START);

        if (!finished)
            aura.updateAuraBuff(this);
    }

    //---------------------------------------------------------------------
    // flags

    public boolean isIncludeFlags(int flags) {
        return (this.flags & flags) > 0;
    }

    public void includeFlags(int flags) {
        this.flags |= flags;
    }

    public void excludeFlags(int flags) {
        this.flags &= ~flags;
    }

    //---------------------------------------------------------------------
    // getters and setters

    public int getTemplateId() {
        return getTemplateAura().id;
    }

    public boolean isFinished() {
        return finished || getTargetValid() == null;
    }

    public IEntity getTargetValid() {
        return target == null ? null : target.getIndirectObject().get();
    }

    public IEntity getTargetAnyway() {
        return target;
    }

    public Object getContext(int index) {
        if (index < 0 || index >= objContext.length) {
            return null;
        }

        return objContext[index];
    }

    public void setContext(Object context, int index) {
        if (index < 0 || index >= objContext.length) {
            return;
        }

        if (context == null || objContext[index] != null) {
            return;
        }

        objContext[index] = context;
    }

    public PBAura.Builder toDAura() {
        PBAura.Builder builder = PBAura.newBuilder();
        builder.setId(getId());
        builder.setTemplateId(aura.getTemplateAura().id);
        return builder;
    }
}
