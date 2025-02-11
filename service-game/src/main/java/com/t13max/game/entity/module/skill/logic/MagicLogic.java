package com.t13max.game.entity.module.skill.logic;


import com.t13max.game.entity.module.reaction.AbstractReaction;
import com.t13max.game.entity.module.reaction.ReactionParam;
import com.t13max.game.entity.module.skill.Magic;
import com.t13max.game.entity.module.skill.MagicConst;
import com.t13max.game.entity.module.skill.MagicTarget;
import com.t13max.game.pos.Vector3D;
import lombok.Getter;

import java.util.*;

/**
 * 技能逻辑
 *
 * @Author t13max
 * @Date 16:02 2025/2/11
 */
public class MagicLogic {
    /** 操作者 */
    @Getter
    private UnitObject proxy;

    /** 法术释放者 */
    @Getter
    private UnitObject caster;

    /** 法术配置 */
    @Getter
    private ConfMagic conf;

    /** 连续技初始技能 */
    @Getter
    private ConfMagic confContinuesStart;

    /** 技能fsm */
    @Getter
    private StateMachineDefault<com.t13max.game.entity.module.skill.MagicLogic, MagicState, MagicStateContext> fsm;

    /** 法术目标 */
    @Getter
    private MagicTarget magicTarget;

    /** 等级 */
    @Getter
    private int level;

    /** 来源者ID */
    private long sourceId;

    /** 触发标记位 */
    private int triggerFlags;

    /** 释放者位置 */
    @Getter
    private Vector3D casterPosition;

    /** 释放位置 */
    @Getter
    private Vector3D castTargetPosition;

    /** 释放方向 */
    @Getter
    private Vector3D castTargetDirection;

    /** 法术环境 */
    @Getter
    private MagicEnv magicEnv;

    /** 来源类型 */
    @Getter
    private EMagicSource castSource = EMagicSource.SOURCE_NORMAL;

    /** 是否已打断 */
    @Getter
    private boolean isInterrupted;

    /** 是否蓄力 */
    @Getter
    private boolean isPrepare;

    /** 事件处理器 */
    @Getter
    private BasicEventProcessor eventProcessor;

    /** 效果集合:里面存的是父类的实现：Common、Motion、Bullet、Flexible */
    private List<MagicEffect> effects = new ArrayList<>();

    /** 反应器集合 */
    private List<AbstractReaction> reactions = new ArrayList<>();

    /** 组合操作者 */
    private List<Long> combinations = new ArrayList<>();

    public MagicLogic(UnitObject caster, ConfMagic conf, int level) {
        this(caster, conf, level, 0, null);
    }

    public MagicLogic(UnitObject caster, ConfMagic conf, int level, int triggerFlags, ConfMagic confStart) {
        this.caster = caster;
        this.sourceId = caster.getOwnerOrSelf().getId();
        this.casterPosition = caster.getPosition();
        this.conf = conf;

        if (confStart != null) {
            this.confContinuesStart = ConfMagic.get(confStart.sn);
        }
        this.fsm = new StateMachineDefault<>(this, MagicState.STATE_INITIAL);
        this.triggerFlags = triggerFlags;
        this.level = level > 0 ? level : 1;
        this.eventProcessor = new BasicEventProcessor();
        this.magicEnv = new MagicEnv();
    }

    public void initial(boolean isPrepare, UnitObject proxy, EMagicSource source) {
        this.isPrepare = isPrepare;
        this.proxy = proxy;
        this.castSource = source;
    }

    public void initial(EMagicSource source) {
        this.initial(false, null, source);
    }

    public int startup(MagicTarget target) {
        int result = ErrorCode.SUCCESS;

        // 初始化法术目标
        resetTarget(target);

        // 初始化技能效果，[效果类型1 效果id2 效果延时1、...]，这样子最多14个效果
        this.initialEffects();

        // 如果需要检查条件
        if (!this.isIncludeTriggerFlags(MagicConst.TRIGGERED_IGNORE_CONDITION)) {
            // 如果被缚地，则无法释放技能
            result = this.checkCondition();
            if (result != ErrorCode.SUCCESS) {
                return result;
            }
        }

        // 默认这个里面diff等各种参数都是0，会立马进入施法状态，如果有前摇，那么会一直tick下去,直到前摇结束进入下一个状态. 一直update下去
        this.fsm.startup(new MagicStateContext());

        // 为施法者加上一个事件，之后走场景tick，通过processor调用下面的pulse
        caster.getModEvent().add(new EventMagic(this));

        return result;
    }

    /**
     * 变更法术目标
     */
    public void resetTarget(MagicTarget target) {
        this.magicTarget = target;

        // 释放位置和方向
        castTargetPosition = magicTarget.getCastPosition(caster.getScene());
        if (!conf.LockDir && !castTargetPosition.isZero() && !castTargetPosition.equals(casterPosition)) {
            castTargetDirection = castTargetPosition.sub(casterPosition).normalize();

            // 施法者转向
            if (!this.isIncludeTriggerFlags(MagicConst.TRIGGERED_IGNORE_DIRECTION)) {
                caster.changeDirection(castTargetDirection);
            }
        } else {
            if (caster.getDirection().isZero2D()) {
                LogServer.game.error("{} 朝向异常, 已修正为标准朝向", caster);
                caster.changeDirection(SceneFightHelper.standardDirection());
            }
            castTargetDirection = caster.getDirection().normalize();
            triggerFlags |= MagicConst.TRIGGERED_IGNORE_DIRECTION;
        }
    }

    public void cleanup() {
        reactions.removeIf(reaction -> {
            caster.getUnitModule().getModReaction().remReaction(reaction);
            return true;
        });
    }

    /**
     * 传入0是为了做状态补偿
     * 之后通过event，不断的更新
     *
     * @param diff
     * @return
     */
    public int pulse(long diff) {
        // 做状态补偿用,前摇和后摇时间特别短，那起码tick一次
        while (true) {
            MagicState oldState = this.fsm.getCurrentState();

            this.fsm.getStateParam().setDiff((int) diff);

            try {
                this.fsm.update();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 比如：有前摇。这种update一下，那肯定还是等于当前状态.
            // 没有前摇那种，就会直接跳到下一个状态
            if (oldState == this.fsm.getCurrentState()) {
                // 说明有前摇
                break;
            }
        }

        return 0;
    }

    /**
     * 条件检查
     *
     * @return ErrorCode
     */
    private int checkCondition() {
        if (conf.isMotionMagic && caster.getBitSetEx(UnitBits.BITEX_GROUNDED)) {
            return ErrorCode.MAGIC_CAST_GROUNDED_LIMIT;
        }

        // todo::施法距离检查

        return ErrorCode.SUCCESS;
    }

    /**
     * 蓄力技能开火
     *
     * @return ErrorCode
     */
    public int fire(MagicTarget target, Vector3D casterPos) {
        if (!conf.isPrepareMagic) {
            return -1;
        }

        // 变更法术目标
        resetTarget(target);

        MagicState currState = fsm.getCurrentState();
        switch (currState) {
            case STATE_CASTING: {
                this.casterPosition = casterPos;
                this.magicTarget = target;

                this.isPrepare = false;
                break;
            }
            case STATE_PREPARING: {
                this.casterPosition = casterPos;
                this.magicTarget = target;

                MagicStateContext context = this.fsm.getStateParam();
                context.setTimeTick(0);
                context.setDiff(0);
                this.fsm.update();
                break;
            }
            default: {
                return -1;
            }
        }

        return ErrorCode.SUCCESS;
    }

    /**
     * 是否不可自发打断
     */
    public boolean isSelfUnInterrupt() {

        return !getFsm().getCurrentState().canSelfInterrupt(getFsm().getStateParam());
    }

    public int interrupt(int flags) {
        if (this.isInterrupted) {
            return -1;
        }

        this.isInterrupted = true;

        // 跳过步骤
        if ((flags & MagicConst.INTERRUPT_JUMP_STEPS) > 0) {
            this.fsm.changeState(MagicState.STATE_FINISHING);
        } else {
            this.fsm.getCurrentState().onInterrupt(this, fsm.getStateParam());
        }

        // 打断消息
        if ((flags & MagicConst.INTERRUPT_DONT_SENDMSG) == 0) {
            MsgMagic.SCCastInterruped.Builder msg = MsgMagic.SCCastInterruped.newBuilder();
            msg.setCaster(caster.getId());
            msg.setMagicSn(conf.sn);
            if ((flags & MagicConst.INTERRUPT_ACTION) > 0) {
                msg.setType(1);
            }

            long exceptId = 0L;
            // 被技能打断不通知自己
            if ((flags & MagicConst.INTERRUPT_CAST_OTHER_MAGIC) != 0) {
                exceptId = caster.getId();
            }

            caster.sendMsgToView(msg.build(), exceptId);
        }

        // 打断组合操作者
        for (long combinationId : combinations) {
            UnitObject combinator = caster.getScene().getWorldObjectAs(UnitObject.class, combinationId);
            if (combinator != null) {
                combinator.getUnitModule().getModSkill().interruptMagic(MagicConst.INTERRUPT_FORCE);
            }
        }

        return 0;
    }

    public void doCastBegan() {
        ModSkill modSkill = caster.getUnitModule().getModSkill();
        modSkill.onMagicCastBegan(this);

        if (!isIncludeTriggerFlags(MagicConst.TRIGGERED_IGNORE_CAST_BEGAN_MSG)) {
            MsgMagic.SCCastBegan.Builder msg = MsgMagic.SCCastBegan.newBuilder();
            msg.setCaster(caster.getId());
            msg.setCasterPosition(caster.getPosition().toMsg());
            msg.setCasterDirection(caster.getDirection().toMsg());
            msg.setMagicSn(conf.sn);
            msg.setMagicTarget(magicTarget.buildDMagicTarget());

            // 修改信息同步
            if (caster.isKindOf(SceneRole.class)) {
                Collection<Integer> modified = modSkill.getModifier().getMagicModifiersNeedNotify(conf.sn);
                if (!modified.isEmpty()) {
                    msg.addAllModifiers(modified);
                }
            }

            if (conf.isPrepareMagic && isPrepare) msg.setIsPrepare(true);
            if (isIncludeTriggerFlags(MagicConst.TRIGGERED_IMMEDIATELY)) msg.setImmediately(true);

            this.caster.sendMsgToView(msg.build(), 0L);
        }
    }

    public void doCastEnded() {
        //todo::激发硬直
        ConfMagic tmpConf = conf;

        // 坐骑替换技能
        Magic obj = caster.getUnitModule().getModSkill().getRealMagic(tmpConf.sn);
        if (obj != null) {
            tmpConf = ConfMagic.get(obj.getSn());
        }

        ModifierConfMagic modifier = caster.getModifier().getConfMagicModifier(tmpConf);

        ReactionMagicCasted context = new ReactionMagicCasted();
        context.caster = caster;
        context.magic = this;
        context.confMagic = tmpConf;
        context.cooldownMs = modifier.cdTime(getLevel());
        context.consumeMP = tmpConf.consumeXP;

        if (!isIncludeTriggerFlags(MagicConst.TRIGGERED_IGNORE_REACTION)) {
            this.caster.getUnitModule().getModReaction().trigger(EReactionTrigger.REACTION_MAGIC_CASTED, new ReactionParam(context));
        }

        // 计算CD
        caster.getUnitModule().getModSkill().startCD(tmpConf, context.cooldownMs);
        if (confContinuesStart != null) {
            // 连续技重置起始技能cd
            caster.getUnitModule().getModSkill().startCD(confContinuesStart, context.cooldownMs);
        }

        // 技能前摇结束
        caster.getUnitModule().getModSkill().onMagicCastEnded(tmpConf, confContinuesStart);

        // xp消耗
        costXP:
        {
            if (caster.getUnitModule().getModSkill().isWtf()) {
                break costXP;
            }
            if (tmpConf.consumeXP > 0.f) {
                float value = caster.getUnitModule().getModAttr().modXP(-tmpConf.consumeXP);
                SceneMsgHelper.sendUpdateXPToOwner(this.caster, value);
            }
        }

        // sp消耗
        costSP:
        {
            if (caster.getUnitModule().getModSkill().isWtf()) {
                break costSP;
            }
            if (tmpConf.expendSP > 0) {
                caster.getUnitModule().getModAttr().modSP(-tmpConf.expendSP);
            }
        }
    }

    public void doPrepareBegan() {
        MsgMagic.SCCastPrepareBegan.Builder msg = MsgMagic.SCCastPrepareBegan.newBuilder();
        msg.setCaster(caster.getId());
        msg.setMagicSn(conf.sn);
        msg.setMaxTime(conf.prepareTime);

        this.caster.sendMsgToView(msg.build(), caster.getId());
    }

    public void doPrepareEnded() {
        MsgMagic.SCCastPrepareEnded.Builder msg = MsgMagic.SCCastPrepareEnded.newBuilder();
        msg.setCaster(caster.getId());
        msg.setMagicSn(conf.sn);
        this.caster.sendMsgToView(msg.build(), caster.getId());
    }

    public void doPrepareFire() {
        // 判断是否是超时施放
        boolean overtime = false;
        if (fsm.getStateParam().getPrepareTimeTick() > 0) {
            overtime = fsm.getStateParam().getPrepareTimeStart() + conf.prepareTime >= caster.getTime();
        }

        MsgMagic.SCCastFire.Builder msg = MsgMagic.SCCastFire.newBuilder();
        msg.setId(caster.getId());
        msg.setMagicSn(conf.sn);
        msg.setCastTarget(magicTarget.buildDMagicTarget());
        msg.setCasterPosition(caster.getPosition().toMsg());
        this.caster.sendMsgToView(msg.build(), overtime ? 0L : caster.getId());
    }

    /*** On Skill Cast Finish*/
    public void doFinish() {
        caster.getUnitModule().getModSkill().onMagicFinish(conf, confContinuesStart);
        Event.fire(new OnMagicCastFinish(caster, conf.sn, magicTarget.getSelectId()));
    }

    public void doEffects(EMagicEffectMode mode) {
        for (int i = 0; i < effects.size(); ++i) {
            this.doEffectOnTargets(mode, i, null);
        }
    }

    public void doEffectOnTargets(EMagicEffectMode mode, int index, List<WorldObject> targets) {
        if (index < 0 || index >= effects.size()) {
            return;
        }

        MagicEffect effect = effects.get(index);
        if (effect == null) {
            return;
        }

        mode.doEffect(effect, new EffectContext(proxy, caster, sourceId, this, conf, index));
    }

    //---------------------------------------------------------------------
    // target position

    /**
     * 初始化结算类效果目标位置
     *
     * @param effect          EffectCommon
     * @param casterPosition  casterPosition
     * @param targetPosition  targetPosition
     * @param targetDirection targetDirection
     */
    public static void initEffectCommonTargetPosition(EffectCommon effect,
                                                      UnitObject caster,
                                                      Vector3D casterPosition,
                                                      Vector3D targetPosition,
                                                      Vector3D targetDirection) {
        ModifierConfEffectCommon modifier;
        if (caster == null)
            modifier = new ModifierConfEffectCommon(effect.conf);
        else
            modifier = caster.getModifier().getConfEffectCommonModifier(effect.conf);
        switch (modifier.regionType) {
            case MagicConst.REGION_TYPE_SINGLE: {
            }
            break;
            case MagicConst.REGION_TYPE_ORIGIN_OFFSET_ROUND: {
                double offset1 = modifier.regionParam2;
                double offset2 = modifier.regionParam3;

                effect.setPosition(casterPosition.sum(targetDirection.mul(offset1)).sum(targetDirection.skew().mul(offset2)));
            }
            break;
            case MagicConst.REGION_TYPE_OFFSET_ROUND: {
                double offset1 = modifier.regionParam2;
                double offset2 = modifier.regionParam3;

                effect.setPosition(targetPosition.sum(targetDirection.mul(offset1)).sum(targetDirection.skew().mul(offset2)));
            }
            break;
            case MagicConst.REGION_TYPE_RING: {
                effect.setPosition(targetPosition);
            }
            break;
            case MagicConst.REGION_TYPE_SECTOR: {
                double radius_max = modifier.regionParam1;
                effect.targetBegin = casterPosition;
                effect.targetEnded = casterPosition.sum(targetDirection.mul(radius_max));
            }
            break;
            case MagicConst.REGION_TYPE_RECTANGLE_ORIGIN: {
                double length = modifier.regionParam1;
                double offset = modifier.regionParam3;
                Vector3D direction = MathUtils.floatEqual(modifier.regionParam4, 0.f) ?
                        targetDirection : targetDirection.rotateCW(modifier.regionParam4);
                effect.targetBegin = casterPosition.sum(direction.mul(offset));
                effect.targetEnded = casterPosition.sum(direction.mul(length + offset));
            }
            break;
            case MagicConst.REGION_TYPE_RECTANGLE_TARGET: {
                double length = modifier.regionParam1;

                effect.targetBegin = targetPosition;
                effect.targetEnded = targetPosition.sum(targetDirection.mul(length));
            }
            break;
            case MagicConst.REGION_TYPE_RECTANGLE: {
                effect.targetBegin = casterPosition;
                effect.targetEnded = targetPosition;
            }
            break;
        }
    }

    public void addEvent(BasicEvent event) {

        eventProcessor.add(event);
    }

    public void addReaction(AbstractReaction reaction) {
        this.caster.getUnitModule().getModReaction().addReaction(reaction);
        this.reactions.add(reaction);
    }

    //-----------------------------------reaction end ------------------------------------

    public MagicEffect getMagicEffect(int index) {
        if (index < 0 || index >= effects.size()) {
            return null;
        }

        return effects.get(index);
    }

    public Vector3D getTargetPosition(int index) {
        Vector3D result = new Vector3D();
        MagicEffect effect = this.getMagicEffect(index);
        if (effect == null) {
            return result;
        }

        return effect.getPosition();
    }

    public void addHit(long targetId) {
        magicEnv.addEffected(targetId);
    }

    public int getHitCount() {
        return magicEnv.getHitCount();
    }

    public boolean isShiftEnable() {
        return magicEnv.shiftEnable;
    }

    public void setShiftEnable(boolean shiftEnable) {
        magicEnv.shiftEnable = shiftEnable;
    }

    public boolean isFinished() {
        return this.fsm.getCurrentState() == MagicState.STATE_IDLE;
    }

    public boolean isIncludeTriggerFlags(int flag) {
        return (triggerFlags & flag) > 0;
    }

    public void buildCombinator(UnitObject combinator) {
        this.combinations.add(combinator.getId());
    }

    private void initialEffects() {
        ModifierConfMagic magicModifier = caster.getModifier().getConfMagicModifier(conf);
        for (int i = 1; i <= 14; ++i) {
            // 效果类型：1.伤害 2.位移 3.真子弹 4.专有效果(比较特殊的全部塞到里面)
            int type = magicModifier.getEffectType("effectType" + i);
            // 效果id
            int sn = magicModifier.getEffectId("effectId" + i);
            // 效果延时
            int delay = magicModifier.getEffectDelay("effectDelay" + i);

            if (type <= 0 || sn <= 0) {
                continue;
            }

            int index = effects.size();

            switch (type) {
                // 伤害
                case MagicConst.EFFECT_TYPE_COMMON: {
                    ConfEffectCommon conf = ConfEffectCommon.get(sn);
                    if (conf == null) {
                        LogServer.conf.error("ConfEffectCommon sn {} doesn't exist !！！", sn);
                        break;
                    }
                    EffectCommon effect = new EffectCommon(conf, index, delay);

                    // 重新计算延迟时间
                    ModifierConfEffectCommon effectModifier = caster.getModifier().getConfEffectCommonModifier(conf);
                    if (effectModifier.regionType == MagicConst.REGION_TYPE_SINGLE) {
                        UnitObject target = caster.getScene().getWorldObjectAs(UnitObject.class, magicTarget.getTargetId());
                        if (target != null && effectModifier.regionParam1 > 0.f) {
                            double distance = caster.distance(target);
                            long realDelay = (long) (distance / effectModifier.regionParam1 * 1000);
                            effect.setDelay(effect.getDelay() + realDelay);
                        }
                    }

                    // 初始化目标位置
                    initEffectCommonTargetPosition(effect, caster, casterPosition, castTargetPosition, castTargetDirection);

                    effects.add(effect);
                }
                break;

                // 位移
                case MagicConst.EFFECT_TYPE_MOTION: {
                    ConfEffectMotion conf = ConfEffectMotion.get(sn);
                    EffectMotion effect = new EffectMotion(conf, sn, index, delay);

                    effects.add(effect);
                }
                break;

                // 子弹
                case MagicConst.EFFECT_TYPE_BULLET: {
                    ConfEffectBullet conf = ConfEffectBullet.getNotNull(sn);
                    EffectBullet effect = new EffectBullet(conf, sn, index, delay);
                    effect.setPosition(
                            effect.getEffectTargetPosition(this, effect.conf.targetType,
                                    effect.conf.targetParam1, effect.conf.targetParam2, effect.conf.targetParam3));

                    effects.add(effect);
                }
                break;

                // 专有
                case MagicConst.EFFECT_TYPE_FLEXIBLE: {
                    //
                    EffectFlexible effect = new EffectFlexible(this, sn, index, delay);
                    effect.setPosition(
                            effect.getEffectTargetPosition(
                                    this, effect.conf.targetType,
                                    effect.conf.get_targetParam1_upgrade(getLevel()),
                                    effect.conf.get_targetParam2_upgrade(getLevel()),
                                    effect.conf.get_targetParam3_upgrade(getLevel())));

                    effects.add(effect);
                }
                break;

                default: {
                    LogServer.skill.error("ConfMagic {} 错误的效果类型{}", conf.sn, type);
                }
                break;
            }
        }

    }

    static class MagicEnv {
        /** 已生效目标ID */
        private Set<Long> effectIds = new HashSet<>();

        @Getter
        private int hitCount;

        /** Shift位移效果是否生效 */
        private boolean shiftEnable = true;

        private int getEffectedCount() {
            return effectIds.size();
        }

        private void addEffected(long targetId) {
            this.effectIds.add(targetId);
            this.hitCount++;
        }

        private boolean isEffected(long targetId) {
            return this.effectIds.contains(targetId);
        }
    }
}
