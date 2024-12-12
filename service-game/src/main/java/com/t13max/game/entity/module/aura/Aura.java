package com.t13max.game.entity.module.aura;


import com.t13max.game.consts.Const;
import com.t13max.game.consts.UnitBits;
import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.PlayerEntity;
import com.t13max.game.entity.module.reaction.AbstractReaction;
import com.t13max.game.entity.module.reaction.ReactionParam;
import com.t13max.game.entity.module.reaction.ReactionEnum;
import com.t13max.game.entity.module.skill.MagicLogic;
import com.t13max.game.exception.GameException;
import com.t13max.game.world.World;
import com.t13max.template.helper.AuraHelper;
import com.t13max.template.manager.TemplateManager;
import com.t13max.template.temp.TemplateAura;
import lombok.Getter;
import lombok.Setter;
import world.entity.PBAura;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * 光环
 *
 * @Author t13max
 * @Date 17:46 2024/12/11
 */
public final class Aura {

    private long id;
    //光环配置
    @Getter
    private final TemplateAura templateAura;
    //光环拥有者
    @Getter
    private IEntity owner;
    //释放者ID
    @Getter
    private long casterId;
    //等级
    @Getter
    private int level;
    @Getter
    private int magicLevel;
    //层数
    private int overlap;
    //刷新周期
    private long refreshTick;
    //叠层时间栈
    private final Deque<Long> durationStack = new LinkedList<>();
    //持续时间
    @Getter
    private long duration;
    //是否结束
    @Getter
    private boolean finished;
    //参数
    @Getter
    @Setter
    private long param;
    //技能管理者
    @Getter
    private MagicLogic magicOrNull;
    @Getter
    private AuraCleaner cleaner;
    //effect
    private final Map<Long, AuraBuff> auraBuffs = new HashMap<>();
    private final Map<Long, AuraBuff> addBuffs = new HashMap<>();
    private final Set<Long> remBuffs = new HashSet<>();
    private int running;
    //效果列表
    private final AuraEffect[] effects = new AuraEffect[AuraConst.MAX_AURAS_EFFECTS];

    public static Aura createOrNull(IEntity owner,
                                    IEntity caster,
                                    TemplateAura templateAura, int level, int magicLv, MagicLogic magicLogic, long param) {

        return new Aura(owner, caster, templateAura, level, magicLv, magicLogic, param);
    }

    public static Aura createAndStart(IEntity owner,
                                      IEntity caster,
                                      TemplateAura templateAura, int level, int magicLv, MagicLogic magicLogic, long param) {
        Aura aura = createOrNull(owner, caster, templateAura, level, magicLv, magicLogic, param);
        if (aura != null) {
            return aura.startup(owner, 0);
        }
        return null;
    }

    Aura(PlayerEntity owner, PBAura pbAura) {
        this.id = owner.getWorld().getWorldModules().getUuidMod().applyRefId();
        this.templateAura = TemplateManager.inst().helper(AuraHelper.class).getTemplate(pbAura.getTemplateId());
        //this.casterId = dAura.getCasterId();
        //this.level = dAura.getLevel();
        //this.overlap = dAura.getOverlap();
        //this.duration = dAura.getDuration();
        this.owner = owner;
        //this.durationStack.addAll(dAura.getDurationStackList());

        this.init(owner.getWorld().getWorldModules().getEntityMod().getEntity(casterId), false);
    }

    private Aura(IEntity owner, IEntity caster, TemplateAura templateAura, int level, int magicLevel, MagicLogic magicLogic, long param) {
        this.templateAura = templateAura;
        this.id = owner.getWorld().getWorldModules().getUuidMod().applyRefId();
        this.casterId = caster.getId();
        this.level = level;
        this.magicLevel = magicLevel;
        //this.duration = AuraManager.getDuration(caster, templateAura, magicLevel);
        this.magicOrNull = magicLogic;
        this.param = param;
        this.owner = owner;
        //this.overlap = modifier.overlapNum;

        this.init(caster, true);
    }

    public void init(IEntity caster, boolean create) {
        // 切场景或重新登录
        this.finished = false;
        this.refreshTick = Math.max(templateAura.refreshTime, Const.TICK_INTERVAL);
        this.cleaner = new AuraCleaner();
        if (create) {
            // 创建时修正持续时间
            this.duration = templateAura.duration;
            // 创建光环触发
            owner.getEntityModules().getReactionMod()
                    .trigger(ReactionEnum.REACTION_AURA_CREATING, new ReactionParam(this));
        }

        this.initEffects();
    }

    public void initEffects() {
        for (int i = 1; i <= AuraConst.MAX_AURAS_EFFECTS; ++i) {
            int sn = templateAura.effect[i];
            if (sn > 0) {
                AuraEffect effect = new AuraEffect(i, sn, magicLevel);
                effects[i] = effect;
            }
        }
    }

    public Aura startup(IEntity owner, int flags) {
        this.owner = owner;
        this.finished = false;

        // 处理光环级别叠加关系
        AuraConflict conflict = owner.getEntityModules().getAuraMod().getAuraConflict(this);
        switch (conflict.conflict) {
            case AuraConst.AURA_SIDE_EFFECT_OVERLAP:
                // 光环叠层
                conflict.aura.incrementOverlap(this.level, this.overlap, this.duration);
                return conflict.aura;
            case AuraConst.AURA_SIDE_EFFECT_OVERLAP_REFRESH:
                // 光环叠层 刷新时间
                conflict.aura.refreshDuration(false, this.duration);
                if (!conflict.aura.incrementOverlap(this.level, this.overlap, this.duration)) {
                    conflict.aura.notifyAuraBuffUpdate();
                }
                return conflict.aura;
            case AuraConst.AURA_SIDE_EFFECT_REFRESH_EXISTS:
                // 重置现有aura的持续时间
                conflict.aura.refreshDuration(true, this.duration);
                return conflict.aura;
            case AuraConst.AURA_SIDE_EFFECT_APPEND_DURATION:
                // 增加现有aura的持续时间
                conflict.aura.appendDuration(true, duration);
                return conflict.aura;
            case AuraConst.AURA_SIDE_EFFECT_REPLACE:
                // 移除旧光环.添加新光环
                conflict.aura.cleanup(AuraConst.AURA_REMOVE_BY_DEFAULT, flags);
            default: {  // 新光环
                if ((flags & AuraConst.AURA_FLAGS_IGNORE_OPER_MODULE) == 0) {
                    // 光环超上限了 很有可能是配置错误
                    if (conflict.existAuras.size() > AuraConst.MAX_SAME_AURA_SIZE) {
                        for (Aura exist : conflict.existAuras) {
                            exist.getCleaner().cleanupAsync(AuraConst.AURA_REMOVE_BY_CANCEL, 0);
                        }
                    }

                    owner.getEntityModules().getAuraMod().add(this);
                }

                /*if (this.magicOrNull != null && this.templateAura.manageByMagic) {
                    this.magicOrNull.addEvent(new AuraEvent(this));
                } else {
                    owner.getModEvent().add(new AuraEvent(this));
                }*/

                for (int i = 1; i < overlap; i++)
                    this.durationStack.push(duration);

                this.updateTargets(false);

                return this;
            }
        }
    }

    public int cleanup(int mode, int flags) {
        if (!this.finished) {
            if (isRunning()) {
                cleaner.cleanupAsync(mode, flags);
            } else {
                this.finished = true;

                if ((flags & AuraConst.AURA_FLAGS_IGNORE_OPER_MODULE) == 0) {
                    owner.getEntityModules().getAuraMod().remove(this);
                }

                foreach((key, buff) -> buff.cleanup(mode));

                auraBuffs.clear();
            }
        }

        return 0;
    }

    public void onLeaveWorld() {
        if (isSingleBuff()) {
            return;
        }
        if (isRunning()) {
            auraBuffs.forEach((targetId, buff) -> {
                buff.cleanup(AuraConst.AURA_REMOVE_BY_INVALID);
                remBuffs.add(targetId);
            });
        } else {
            auraBuffs.forEach((targetId, buff) -> buff.cleanup(AuraConst.AURA_REMOVE_BY_INVALID));
            addBuffs.clear();
        }
    }

    public void cleanupBuff(int mode, long targetId) {
        if (auraBuffs.containsKey(targetId)) {
            AuraBuff buff = auraBuffs.get(targetId);
            buff.cleanup(mode);
            if (isRunning())
                remBuffs.add(targetId);
            else
                auraBuffs.remove(targetId);
        }
    }

    public int pulse(long diff) {
        if (this.finished) {
            return -1;
        }

        if (this.cleaner.update(this)) {
            return 0;
        }

        markRunning(true);
        // 刷新目标
        this.refreshTick = Math.max(0L, this.refreshTick - diff);
        if (refreshTick == 0L) {
            // 刷新周期不能突破场景tick
            this.refreshTick = Math.max(templateAura.refreshTime, Const.TICK_INTERVAL);
            this.updateTargets(true);
        }

        // 刷新效果
        for (int i = 0; i < AuraConst.MAX_AURAS_EFFECTS; ++i) {
            if (this.effects[i] != null) {
                this.effects[i].update(this, diff);
            }
        }

        // 刷新周期
        if (duration != -1) {
            this.duration = Math.max(0L, this.duration - diff);
            // 少于20ms剩余时间直接结束了
            if (this.duration <= 20) {
                onExpired();
            }
        }

        // 这里是保证心跳结束时肯定不为running了 正常只会调用一次
        while (isRunning()) {
            markRunning(false);
        }

        return 0;
    }

    //刷新目标，不做其他操作*/
    public void updateTargets(boolean update) {
        // 只对自己生效且目标已设置，直接返回
        if (isSingleBuff() && auraBuffs.size() == 1) {
            return;
        }

        // 到期返回
        if (this.duration == 0 && update) {
            return;
        }

        // ------------------------------------------------------------------------------------------
        // 清除已经失效的Aura buff
        // ------------------------------------------------------------------------------------------

        List<Long> invalids = new ArrayList<>();
        for (Map.Entry<Long, AuraBuff> entry : auraBuffs.entrySet()) {
            long targetId = entry.getKey();
            AuraBuff auraBuff = entry.getValue();

            IEntity target = auraBuff.getTargetValid();
            // 目标已下线 | 超出光环距离
            if (target == null || target.distance(owner.getPosition()) > 0) {
                invalids.add(targetId);

                auraBuff.cleanup(AuraConst.AURA_REMOVE_BY_INVALID);
            }
        }

        for (Long invalid : invalids) {
            if (isRunning())
                remBuffs.add(invalid);
            else
                auraBuffs.remove(invalid);
        }

        // ------------------------------------------------------------------------------------------
        // 处理新进入的对象
        // ------------------------------------------------------------------------------------------

        if (isSingleBuff()) {
            // 只对自己生效
            tryEffect(owner);
        } else {
            // 生效上限
            if (templateAura.targetNumber > 0 && auraBuffs.size() >= templateAura.targetNumber) {
                return;
            }
            final int auraTarget = templateAura.auraTarget;
            if (auraTarget == AuraConst.MAGIC_SINGLE_TARGET) {
                // 光环作用给单体技能释放目标. 这个光环只能配置给有目标的单体技能
                final World world = this.owner.getWorld();
                if (this.magicOrNull == null || this.magicOrNull.getMagicTarget() == null) {
                    return;
                }
                IEntity unitObj = world.getWorldModules().getEntityMod().getEntity(magicOrNull.getMagicTarget().getTargetId());
                this.tryEffect(unitObj);
            } else {
                // 周围角色对象
                final Stream<IEntity> stream = null;//todo AOI AOICollectors.streamInCircle(owner, owner.getScene(), owner.getPosition(), getModifier().radius, IEntity.class);
                stream.forEach(this::tryEffect);
            }

        }
    }

    /**
     * 返回受光环影响的目标集合
     *
     * @return collection of target
     */
    public Collection<Long> getTargets() {

        return this.auraBuffs.keySet();
    }

    /**
     * try enable aura buff to target
     *
     * @param target target
     */
    private void tryEffect(IEntity target) {
        // 目标已存在
        if (auraBuffs.containsKey(target.getId())) {
            return;
        }
        // 生效上限
        if (templateAura.targetNumber > 0 && auraBuffs.size() >= templateAura.targetNumber) {
            return;
        }

        // 忽略光环效果
        if (target.getBit(UnitBits.IGNORE_AURAS) && casterId != target.getId()) {
            return;
        }

        // 可见性
        if (!target.isVisibleToOthers(owner)) {
            return;
        }

        // 死亡不生效
        if (target.isDead()) {
            return;
        }

        // 已有光环buff与本光环buff冲突规则
        AuraAttachContext context = new AuraAttachContext();
        int result = target.getEntityModules().getAuraMod().getAttachSideEffects(this, context);

        // 只要不是免疫 那肯定是冲抵或共存
        if (result == AuraConst.AURA_SIDE_EFFECT_IMMUNE) {
            notifyImmune(target);
        } else {
            if (result != AuraConst.AURA_SIDE_EFFECT_ATTACH) {
                throw new GameException("Aura buff invalid conflict type " + result);
            }

            for (int i = 0; i < context.replaceBuffs.size(); ++i) {
                AuraBuff app = context.replaceBuffs.get(i);
                app.getAura().cleanupBuff(AuraConst.AURA_REMOVE_BY_REPLACE, app.getTargetId());
            }

            // 再查检查下aura 在之前的逻辑有没有被clean掉
            if (isFinished()) {
                return;
            }

            target.getEntityModules().getAuraMod().spareSpace(templateAura);

            // create aura buff
            AuraBuff auraBuff = new AuraBuff(this, target, 0, this.overlap);
            if (isRunning())
                this.addBuffs.put(target.getId(), auraBuff);
            else
                this.auraBuffs.put(target.getId(), auraBuff);

            // aura buff startup
            auraBuff.startup();
        }
    }

    public void doEffect(AuraEffect effect, int mode) {
        for (AuraBuff buff : auraBuffs.values()) {
            if (buff.isIncludeFlags(AuraConst.AURA_APP_FLAGS_ASYNC_CLEAN) || buff.getTargetValid() == null) {
                continue;
            }

            effect.handleEffect(buff, mode);
        }
    }

    /*----------------------------------overlap end-----------------------------------*/

    public PBAura.Builder toDAura() {
        PBAura.Builder builder = PBAura.newBuilder();
        builder.setId(this.id);
        //builder.setSn(this.getSn());
        //builder.setCasterId(this.casterId);
        //builder.setOwnerId(owner.getId());
        //builder.setLevel(this.level);
        //builder.setOverlap(this.overlap);
        //builder.setDuration(this.duration);
        //builder.addAllDurationStack(this.durationStack);
        //if (this.param > 0)
        //    builder.setParam(this.param);

        IEntity caster = getCasterOrNull();
        if (caster != null) {
            //EffectModifier modifier = caster.getEntityModules().getModSkill().getModifier();
            //Collection<Integer> modifierList = modifier.getAuraModifiersNeedNotify(getSn());
            //modifierList.forEach(builder::addModifiers);
        }

        return builder;
    }

    public AuraEffect getEffect(int index) {
        if (index <= 0 || index > AuraConst.MAX_AURAS_EFFECTS) {
            return null;
        }

        return this.effects[index];
    }

    public boolean isEmpty() {

        return this.auraBuffs.isEmpty();
    }

    public IEntity getCasterOrNull() {

        IEntity obj = owner.getWorld().getWorldModules().getEntityMod().getEntity(this.casterId);
        if (obj != null) {
            return obj.queryObject(IEntity.class);
        }

        return null;
    }

    public boolean isPrivate() {

        return (templateAura.flags & AuraConst.AURA_FLAGS_PRIVATE) > 0;
    }

    /**
     * 是否是单体buff
     *
     * @Author t13max
     * @Date 10:34 2024/12/12
     */
    public boolean isSingleBuff() {

        return this.templateAura.singleBuff == 1;
    }

    /**
     * 是否是持久化光环
     *
     * @Author t13max
     * @Date 10:34 2024/12/12
     */
    public boolean isPersist() {

        return !templateAura.finishWhenOffline;
    }

    public void setDuration(long duration) {

        this.duration = duration;
    }

    public int getTemplateId() {
        return this.templateAura.id;
    }

    /**
     * 减少指定层数
     *
     * @Author t13max
     * @Date 10:34 2024/12/12
     */
    public int decrementOverlap(int reduce) {
        if (reduce <= 0) {
            return overlap;
        }
        if (overlap <= reduce) {
            // 异步删除
            overlap = 0;
            cleaner.cleanupAsync(AuraConst.AURA_REMOVE_BY_CANCEL, 0);
            return 0;
        }

        overlap -= reduce;
        foreach((id, buff) -> buff.onOverlapDecrement(overlap));
        for (int i = 0; i < reduce; i++) resetDurationByOverlap();

        return overlap;
    }

    public void manageReaction(AbstractReaction reaction) {
        if (!isSingleBuff()) {
            return;
        }
        foreach((id, buff) -> buff.manageReaction(reaction));
    }

    void updateAuraBuff(AuraBuff auraBuff) {
        owner.getEntityModules().getAuraMod().onAuraBuffUpdate(this.casterId, 0, auraBuff);
    }

    /**
     * 增加光环效果层数
     *
     * @Author t13max
     * @Date 10:34 2024/12/12
     */
    private boolean incrementOverlap(int level, int increment, long duration) {
        if (templateAura.overlapMax <= 0) {
            return false;
        }
        if (this.overlap >= templateAura.overlapMax) {
            return false;
        }
        int old = this.overlap;
        this.overlap = Math.min(this.overlap + increment, templateAura.overlapMax);
        increment = overlap - old;
        this.level = level;
        for (int i = 0; i < increment; i++)
            this.durationStack.push(duration);

        foreach((id, buff) -> buff.onOverlapIncrement(overlap, level));

        return true;
    }

    //减少光环效果层数*/
    private void decrementOverlap() {
        if (overlap <= 1) {
            throw new GameException("Can't decrement overlap when overlap " + overlap);
        }

        --overlap;
        resetDurationByOverlap();
        if (!isFinished()) {
            foreach((id, buff) -> buff.onOverlapDecrement(overlap));
        }

    }

    /**
     * 光环到期
     *
     * @Author t13max
     * @Date 10:35 2024/12/12
     */
    private void onExpired() {
        if (overlap > 1 && templateAura.reduceOverlap) {
            // 层数衰减
            decrementOverlap();
        } else {
            // 直接结束
            this.cleanup(AuraConst.AURA_REMOVE_BY_EXPIRE, 0);
        }
    }

    /**
     * 刷新持续时间
     *
     * @Author t13max
     * @Date 10:35 2024/12/12
     */
    private void refreshDuration(boolean sendMsg, long duration) {
        this.duration = duration;

        if (sendMsg) {
            notifyAuraBuffUpdate();
        }
    }

    /**
     * 增加持续时间
     *
     * @Author t13max
     * @Date 10:35 2024/12/12
     */
    private void appendDuration(boolean sendMsg, long duration) {
        this.duration += duration;

        if (sendMsg) {
            notifyAuraBuffUpdate();
        }
    }

    private void resetDurationByOverlap() {
        if (durationStack.isEmpty()) {
            this.duration = templateAura.duration;
        } else {
            this.duration = durationStack.pop();
        }
    }

    private void notifyImmune(IEntity target) {

    }

    private void notifyAuraBuffUpdate() {
        for (AuraBuff buff : auraBuffs.values()) {
            owner.getEntityModules().getAuraMod().onAuraBuffUpdate(this.casterId, 0, buff);
        }
    }

    private void markRunning(boolean running) {
        if (running)
            this.running++;
        else
            this.running = Math.max(0, this.running - 1);
        if (!isRunning()) {
            if (!addBuffs.isEmpty()) {
                auraBuffs.putAll(addBuffs);
                addBuffs.clear();
            }
            if (!remBuffs.isEmpty()) {
                remBuffs.removeIf(targetId -> {
                    auraBuffs.remove(targetId);
                    return true;
                });
            }
        }
    }

    private void foreach(BiConsumer<Long, AuraBuff> action) {
        markRunning(true);
        auraBuffs.forEach(action);
        markRunning(false);
    }

    private boolean isRunning() {
        return running > 0;
    }
}
