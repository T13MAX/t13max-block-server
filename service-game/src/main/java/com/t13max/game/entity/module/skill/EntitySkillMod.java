package com.t13max.game.entity.module.skill;

import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.EntityModule;
import com.t13max.game.entity.module.combat.CombatContextResult;
import com.t13max.game.entity.module.skill.logic.MagicPassive;
import com.t13max.game.entity.module.skill.overlay.MagicOverlay;
import com.t13max.game.pos.Vector3D;
import com.t13max.template.temp.TemplateMagic;
import lombok.Getter;

import java.util.*;

/**
 * 实体技能模块
 *
     * @Author t13max
     * @Date 16:25 2024/12/13
 */
public class EntitySkillMod extends EntityModule {

    //技能槽位映射
    private final Map<Integer, Magic> magicSlots = new HashMap<>();
    //法术列表
    private final Map<Integer, Magic> magics = new HashMap<>();
    //禁用技能类型
    private final Map<Integer, List<Integer>> disableTypes = new HashMap<>();
    //连招列表
    private final Map<Integer, MagicContinuesContext> successiveInfos = new HashMap<>();
    //被动技能集合
    @Getter
    private final Map<Integer, MagicPassive> passiveMagics = new HashMap<>();
    //防止多次替换导致的冲突
    private int replaceId;
    //施法中的技能
    private MagicLogic currentMagic;
    @Getter
    private MagicLogic lastMagic;
    //法术叠层
    @Getter
    private final MagicOverlay magicOverlay;
    //法术CD
    @Getter
    private final MagicCD magicCD;
    //免cd免消耗
    @Getter
    private boolean wtf = false;
    //动态阻挡
    @Getter
    private final List<DynamicBlockEffect> fightBlocks = new ArrayList<>();

    private final Multimap<String, Integer> groupPassives = ArrayListMultimap.create();

    public EntitySkillMod(IEntity owner) {
        super(owner);
        this.magicOverlay = new MagicOverlay(owner);
        this.magicCD = new MagicCD(owner);
    }

    @Override
    public int pulse(long now) {
        if (currentMagic != null && currentMagic.isFinished()) {
            setCurrentMagic(null);
        }

        this.magicOverlay.pulse(now);

        // 同步修改内容
        modifier.syncModified(owner);

        if (!fightBlocks.isEmpty()) {
            boolean removed = fightBlocks.removeIf(effect -> {
                if (now > effect.getExpiredTime()) {
                    MsgMagic.SCDynamicBlockEnd.Builder msg = MsgMagic.SCDynamicBlockEnd.newBuilder();
                    msg.setCasterId(owner.getId());
                    msg.setId(effect.getDDynamicBlock().getId());
                    owner.sendMsgToView(msg.build(), 0L);
                    return true;
                }
                return false;
            });
            if (removed) {
                owner.serializeRoleInfo();
            }
        }
        return 0;
    }

    @Override
    public void enterWorld() {

        this.startPassiveMagic();
    }

    @Override
    public void leaveWorld() {
        this.cleanPassiveMagic();
        this.successiveInfos.clear();
        if (!fightBlocks.isEmpty()) {
            fightBlocks.clear();
            owner.serializeRoleInfo();
        }
    }

    public void initMagicList(MsgDef.DMagic dMagic) {
        Magic magic = new Magic(dMagic);
        TemplateMagic TemplateMagic = TemplateMagic.get(magic.getSn());
        if (TemplateMagic == null) {
            LogServer.fight.error("ModSkill init magic failed. TemplateMagic {} doesn't exist.", magic.getSn());
            return;
        }

        if (TemplateMagic.isPassiveMagic) {
            LogServer.skill.error("Initial magic fail, magic {} is passive magic", magic.getSn());
        } else {
            magics.put(magic.getSn(), magic);
            magicSlots.put(dMagic.getLoc(), magic);
            // 叠层技能管理
            this.addOverlayMagic(magic);
        }
    }

    /**
     * 更新槽位技能 (增量)
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public void magicUpdate(Collection<MsgDef.DMagic> addMagics, boolean sendMsg) {
        // dMagic是客户端数据需要转换一下
        for (MsgDef.DMagic dMagic : addMagics) {
            Magic transMagic = new Magic(dMagic);
            this.magics.compute(transMagic.getSn(), (sn, previous) -> {
                if (previous != null) {
                    this.magicSlots.remove(previous.getLoc());

                    // 移除原等级被动
                    int passive = previous.getPassive();
                    if (passive > 0) {
                        removePassiveMagic(passive);
                    }

                } else {
                    this.addOverlayMagic(transMagic);
                }

                return transMagic;
            });

            // 技能附带有被动
            int newPassive = transMagic.getPassive();
            if (newPassive > 0) {
                addPassiveMagic(newPassive, transMagic.getLevel());
            }

            // 更新槽位技能
            Magic oldMagic = this.magicSlots.put(transMagic.getLoc(), transMagic);
            if (oldMagic != null) {
                this.magics.remove(oldMagic.getSn());

                // 移除原技能被动
                int oldPassive = oldMagic.getPassive();
                if (oldPassive > 0) {
                    removePassiveMagic(oldPassive);
                }
            }
        }

        if (sendMsg) sendMsg();
    }

    /**
     * 删除槽位技能
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public void removeMagic(Collection<Integer> removeIds, boolean sendMsg) {
        for (int removeId : removeIds) {
            Magic magic = this.magics.remove(removeId);
            if (magic != null) {
                magicSlots.remove(magic.getLoc());

                // 移除技能附属被动
                int passive = magic.getPassive();
                if (passive > 0) {
                    removePassiveMagic(passive);
                }
            }
        }

        if (sendMsg) sendMsg();
    }

    /**
     * 清理法术列表
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public void cleanMagicList() {

        magics.clear();
    }

    /**
     * 直接结算伤害
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public int castDamage(IEntity target, EffectContext context, ConfEffectCommon effectConf, float damage) {
        if (target.getBitSetEx(UnitBits.BITEX_INVINCIBLE)) {
            return -1;
        }

        CombatContextResult result = new CombatContextResult();
        result.damage = damage;

        DamageProcess.executeResult(context, target, result, effectConf);

        return ErrorCode.SUCCESS;
    }

    /**
     * 以EffectCommon流程直接结算
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public int castCommonEffect(IEntity target, EffectContext context, ConfEffectCommon effectConf) {
        int effectType = context.getEffectType();
        if (effectType > 0 && !EffectHelper.canEffect(owner, target, effectType)) {
            return -1;
        }

        new EffectCommon(effectConf, -1, 0).doEffectOnTarget(context, target, 1);

        return ErrorCode.SUCCESS;
    }

    /**
     * 向目标施法，立即生效
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public int castImmediately(MagicTarget castTarget, TemplateMagic magicConf, int level, EMagicSource source) {
        int flags = MagicConst.TRIGGERED_IGNORE_CONDITION | MagicConst.TRIGGERED_IGNORE_CONSUME | MagicConst.TRIGGERED_IGNORE_CASTING
                | MagicConst.TRIGGERED_IGNORE_CAST_BEGAN_MSG | MagicConst.TRIGGERED_IGNORE_CAST_ENDED_MSG;

        MagicLogic magicLogic = new MagicLogic(owner, magicConf, level, flags, null);
        magicLogic.initial(source);

        int result = magicLogic.startup(castTarget);
        if (result < 0) {
            return result;
        }

        magicLogic.doEffects(EMagicEffectMode.MODE_DELAYED);

        return magicLogic.interrupt(MagicConst.INTERRUPT_JUMP_STEPS | MagicConst.INTERRUPT_DONT_SENDMSG);
    }

    /**
     * 向目标施法，走标准流程
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public int castNormal(MagicTarget castTarget, TemplateMagic magicConf, int level, boolean replaceCurrent, EMagicSource source) {
        int triggerFlags = MagicConst.TRIGGERED_IGNORE_CONDITION | MagicConst.TRIGGERED_IGNORE_CONSUME | MagicConst.TRIGGERED_IGNORE_DIRECTION;
        MagicLogic magicLogic = new MagicLogic(owner, magicConf, level, triggerFlags, null);
        magicLogic.initial(source);
        int result = magicLogic.startup(castTarget);
        if (result < 0) {
            return result;
        }

        if (replaceCurrent) {
            setCurrentMagic(magicLogic);
        }

        magicLogic.pulse(0L);

        return result;
    }

    /**
     * 按目标释放法术
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public int castOn(IEntity target, TemplateMagic magicConf, int level, int triggerFlags, EMagicSource source, long selectId) {
        MagicTarget castTarget = new MagicTarget(target, selectId);
        return cast(magicConf, castTarget, level, triggerFlags, source);
    }

    //按位置释放法术*/
    public int castOn(Vector3D pos, TemplateMagic magicConf, int level, int triggerFlags, EMagicSource source, long selectId) {
        MagicTarget castTarget = new MagicTarget(pos, selectId);
        return cast(magicConf, castTarget, level, triggerFlags, source);
    }

    /**
     * 施法
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public int cast(TemplateMagic TemplateMagic, MagicTarget castTarget, int level, int triggerFlags, boolean isPrepare, EMagicSource source) {
        // 技能配置不存在
        if (TemplateMagic == null) {
            return ErrorCode.MAGIC_CONF_NOFOUND;
        }

        // 检测XP(能量不够无法释放)
        if (!this.checkXP(TemplateMagic)) {
            return ErrorCode.MAGIC_CAST_MP_LIMIT;
        }
        // 检测SP(技能点不够无法释放)
        if (!this.checkSP(TemplateMagic)) {
            return ErrorCode.MAGIC_CAST_SP_LIMIT;
        }
        ModifierTemplateMagic modifier = owner.getModifier().getTemplateMagicModifier(TemplateMagic);
        // 检测MP
        if (!this.checkMP(modifier)) {
            return ErrorCode.MP_NOT_ENOUGH;
        }

        // 连招支持
        TemplateMagic confContinueStart = null;
        if (modifier.isContinus) {
            if (castTarget.getActuallySn() <= 0) {
                return ErrorCode.MAGIC_CONF_NOFOUND;
            }

            MagicContinuesContext context = successiveInfos.compute(TemplateMagic.sn, (sn, value) -> {
                if (value == null)// 连招中不存在当前技能，则将当前技能放到连招列表中
                    return new MagicContinuesContext(TemplateMagic.get(sn));
                if (value.templateMagicContinues != null) {
                    // 连招CD间隔是否合法
                    if (owner.getTime() > value.continuesStartTime) {
                        value.templateMagicContinues = null;
                    }
                }
                return value;
            });

            // 重定向施放技能
            if (context.templateMagicContinues != null) {
                TemplateMagic = context.templateMagicContinues;
            }

            // 连招施放顺序是否合法
            if (TemplateMagic.sn != castTarget.getActuallySn()) {
                return ErrorCode.MAGIC_CONTINUE_SEQUENCE_NOT_VALID;
            }

            confContinueStart = context.templateMagic;
        }

        // 死亡无法释放
        if ((triggerFlags & MagicConst.TRIGGERED_IGNORE_DEATH) == 0 && owner.isDead()) {
            return ErrorCode.DIE_STATE_FORBID_USE_SKILL;
        }

        // 距离检测
        if (castTarget.isOutOfRange(owner, TemplateMagic.castRange)) {
            return ErrorCode.MAGIC_DISTANCE_LIMIT;
        }

        // 检测CD
        if (this.isInCD(TemplateMagic)) {
            return ErrorCode.MAGIC_CD_LIMIT;
        }

        // 检测Overlay
        if (this.needOverlay(TemplateMagic)) {
            return ErrorCode.MAGIC_OVERLAY_LIMIT;
        }

        // 各种检测通过了，则开始释放技能
        // 技能逻辑
        MagicLogic magicLogic = new MagicLogic(owner, TemplateMagic, level, triggerFlags, confContinueStart);
        magicLogic.initial(isPrepare, null, source);

        // 开始之后，就会起一个Event挂到EventProcessor列表中,不断的走pulse，直到IDLE，也就是技能释放结束
        // 有的技能是有前摇，所以TimeTick是不为0`
        int result = magicLogic.startup(castTarget);
        if (result != ErrorCode.SUCCESS) {
            return result;
        }

        // 记录：上一个技能 和 最新的技能为当前技能
        if (!magicLogic.isIncludeTriggerFlags(MagicConst.TRIGGERED_IMMEDIATELY)) {
            setCurrentMagic(magicLogic);
        }

        // 这里其实就是上面的magicLogic
        // 这个是做状态补偿用，有的前摇时间很短，一下子就到后摇了，保证至少update一次
        magicLogic.pulse(0L);

        return result;
    }

    public int cast(TemplateMagic TemplateMagic, MagicTarget castTarget, int level, int triggerFlags, EMagicSource source) {
        return cast(TemplateMagic, castTarget, level, triggerFlags, false, source);
    }

    /**
     * 代理释放
     * 技能效果结算正常以当前单位处理，但技能状态控制交由代理处理
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public int castProxy(IEntity proxy, int magicSn, MagicTarget castTarget, int level, EMagicSource source) {
        TemplateMagic TemplateMagic = TemplateMagic.getNotNull(magicSn);
        if (TemplateMagic == null) {
            return ErrorCode.MAGIC_NOT_EXIST;
        }
        // 检测CD
        if (this.isInCD(TemplateMagic)) {
            return ErrorCode.MAGIC_CD_LIMIT;
        }
        // 检测Overlay
        if (this.needOverlay(TemplateMagic)) {
            return ErrorCode.MAGIC_OVERLAY_LIMIT;
        }

        MagicLogic magicLogic = new MagicLogic(owner, TemplateMagic, level, 0, null);
        magicLogic.initial(false, proxy, source);
        int result = magicLogic.startup(castTarget);
        if (result != ErrorCode.SUCCESS) {
            return result;
        }

        proxy.getEntityModule().getEntitySkillMod().setCurrentMagic(magicLogic);
        proxy.getEntityModule().getEntitySkillMod().currentMagic.pulse(0L);

        return result;
    }


    /**
     * 尝试放技能 一般由客户端发起
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public int tryCast(int sn, int actualSn, Vector3D castDir, MagicTarget castTarget, boolean isPrepare) {
        TemplateMagic conf = TemplateMagic.get(sn);
        if (conf == null) {
            return ErrorCode.MAGIC_CONF_NOFOUND;
        }

        // 查找下玩家身上是否有这个技能(隐藏、替换、原始...),没这个技能不能释放
        Magic magic = getMagic(sn);
        if (magic == null) {
            return ErrorCode.MAGIC_NOT_EXIST;
        }

        // 技能未解锁不能释放
        if (!magic.isUnlock()) {
            return ErrorCode.MAGIC_LOCKED;
        }

        // 角色死了，不能释放
        if (owner.isDead()) {
            return ErrorCode.DIE_STATE_FORBID_USE_SKILL;
        }

        // 是否可用(有些模式不能用这个技能)
        int ret = isDisabled(conf);
        if (ret != ErrorCode.SUCCESS) {
            return ret;
        }

        // 这个槽位的技能正处于cd中，无法释放
        if (magicCD.isSlotCD(magic.getLoc())) {
            return ErrorCode.MAGIC_CD_LIMIT;
        }

        // 玩家死亡、有些场景不允许释放技能、不在位移状态 等不能释放技能
        if (!owner.canCast()) {
            return ErrorCode.MAGIC_STATUS_LIMIT;
        }

        ModifierTemplateMagic modifier = owner.getModifier().getTemplateMagicModifier(conf);
        // 检查一下释放状态，比如：眩晕、沉默、硬直、混乱 等状态下不能释放技能
        for (EStatusFlag eStatusFlag : EStatusFlag.values()) {
            if (eStatusFlag.containStatus(owner, modifier.allowCastStateType)) {
                return ErrorCode.MAGIC_STATUS_LIMIT;
            }
        }

        // 得到当前释放中的技能
        MagicLogic currMagic = getCurrentMagic();
        if (currMagic != null) {
            // 优先级越小，越优先释放
            if (conf.magicLevel <= currMagic.getConf().magicLevel) {
                if (currMagic.isSelfUnInterrupt()) {
                    return ErrorCode.MAGIC_INTERRUPT_PRIORITY_LIMIT;
                }
            }

            // 打断下当前技能
            interruptMagic(MagicConst.INTERRUPT_CAST_OTHER_MAGIC);
        }

        // todo:: check cast target
        // 调整技能选择的目标
        castTarget.adjustSelectId(owner.getId(), conf); // check the target and modify client submit select id

        // 后续技实际释放相信客户端，简单校验
        if (actualSn > 0) {
            castTarget.setActuallySn(actualSn);
            // 处理下连招技能
            handleSuccessiveMagic(modifier, this.successiveInfos, actualSn);
        }

        // 同步朝向: 释放技能的人朝向释放方向(客户端说朝向的方向)
        owner.changeDirection(castDir);

        // 真正的释放技能
        return cast(conf, castTarget, magic.getLevel(), 0, isPrepare, EMagicSource.SOURCE_NORMAL);
    }

    /**
     * 同其他单位释放组合技
     * 只是自己释放,当对方释放时需要反向调用
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public int castWith(IEntity combinator, int sn, MagicTarget castTarget) {
        int ret = cast(TemplateMagic.getNotNull(sn), castTarget, 1, 0, EMagicSource.SOURCE_NORMAL);
        if (ret != ErrorCode.SUCCESS) {
            return ret;
        }
        currentMagic.buildCombinator(combinator);

        return ErrorCode.SUCCESS;
    }

    public int interruptMagic(int flags) {
        if (currentMagic != null) {
            currentMagic.interrupt(flags);
            setCurrentMagic(null);

            return 0;
        }

        return -1;
    }

    public void interruptMagicWhenCast(int magicSn, boolean interruptSameMagic) {
        MagicLogic magicLogic = this.getCurrentMagic();
        if (magicLogic != null) {
            if (interruptSameMagic || magicLogic.getConf().sn != magicSn) {
                this.interruptMagic(0);
            }
        }
    }

    /**
     * 开启CD
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public void startCD(TemplateMagic TemplateMagic, int cdTime) {
        if (isWtf()) {
            return;
        }
        magicCD.start(TemplateMagic, cdTime);
    }

    /**
     * 刷新单位的所有cd冷却
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public void refreshCD() {

        magicCD.refresh(Collections.emptyList());
    }

    /**
     * 减武学技能cd
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public void reduceEquipCD(int reduce) {
        if (reduce == 0) {
            reduce = (int) Time.DAY;
        }
        for (int i = 0; i < MsgDef.EMagicSlot.SLOT_DODGE_VALUE; i++) {
            Magic magic = magicSlots.get(i);
            if (magic != null) {
                magicCD.reduce(magic.getSn(), reduce);
            }
        }
    }

    public void onMagicCastBegan(MagicLogic magicLogic) {
        for (MagicPassive magicPassive : this.passiveMagics.values()) {
            magicPassive.getMagicReaction().get(magicLogic.getConf().sn).forEach(reaction -> {
                reaction.reset();
                magicLogic.addReaction(reaction);
            });
        }

        if (owner.isKindOf(SmartObject.class)) {
            // 智能体技能施放后
            SmartObject smartObject = owner.queryObject(SmartObject.class);
            smartObject.getAgent().ifPresent(agent -> agent.afterCastSkill(magicLogic.getConf()));
        }
    }

    public void onMagicCastEnded(TemplateMagic conf, TemplateMagic confStart) {
        // 叠层技能处理
        this.magicOverlay.onMagicCastEnded(conf);

        // 后续技能缓存
        if (confStart != null) {
            MagicContinuesContext context = successiveInfos.get(confStart.sn);
            if (context != null) {
                ModifierTemplateMagic modifier = owner.getModifier().getTemplateMagicModifier(conf);
                context.templateMagicContinues = TemplateMagic.get(modifier.continusMagic);
                if (conf == confStart) {
                    context.startPosition = currentMagic.getCasterPosition();
                }
                context.lastFinishTime = owner.getTime();
                // 加上400ms容忍
                context.continuesStartTime = owner.getTime() + modifier.continueInternal + 400;
            }
        }
    }

    public void onMagicFinish(TemplateMagic conf, TemplateMagic confStart) {

    }

//    //技能前摇打断+硬直*/
//    public void onMagicHit(CombatContextResult result) {
//        if (this.currentMagic != null && (result.flags & CombatConst.MASK_MISS) == 0) {
//            if (currentMagic.getConf().interruptWhenHurt &&
//                    this.currentMagic.getFsm().getCurrentState() == MagicState.STATE_CASTING) {
//                this.interruptMagic(0);
//                CommandStiffHit cmd = new CommandStiffHit(GlobalParam.MAGIC_INTERRUPT_STIFF_TIME());
//                owner.getEntityModule().getModCommand().add(cmd);
//            }
//        }
//    }

    //-----------------------技能条件检测-----------------

    /**
     * 技能是否可以kaiqi
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public int canCast(TemplateMagic TemplateMagic) {
        if (TemplateMagic == null) {
            return ErrorCode.MAGIC_NOT_EXIST;
        }
        // 检测XP
        if (!this.checkXP(TemplateMagic)) {
            return ErrorCode.MAGIC_CAST_MP_LIMIT;
        }
        // 检测SP
        if (!this.checkSP(TemplateMagic)) {
            return ErrorCode.MAGIC_CAST_SP_LIMIT;
        }

        // 检测CD
        if (this.isInCD(TemplateMagic)) {
            return ErrorCode.MAGIC_CD_LIMIT;
        }

        // 玩家镜像绝学cd处理
        Magic magic = getMagic(TemplateMagic.sn);
        if (magic != null) {
            if (magicCD.isSlotCD(magic.loc)) {
                return ErrorCode.MAGIC_CD_LIMIT;
            }
        }

        // 检测Overlay
        if (this.needOverlay(TemplateMagic)) {
            return ErrorCode.MAGIC_OVERLAY_LIMIT;
        }

        // 是否可用(有些模式不能用这个技能)
        return isDisabled(TemplateMagic);
    }

    //是否在CD中*/
    public boolean isInCD(TemplateMagic conf) {
        return magicCD.isCD(conf);
    }

    //XP检测，不通过返回false*/
    public boolean checkXP(TemplateMagic conf) {
        if (conf.consumeXP > 0) {
            return owner.getEntityModule().getModAttr().getXP() >= conf.consumeXP;
        }

        return true;
    }

    public boolean checkMP(ModifierTemplateMagic modifierTemplateMagic) {
        if (modifierTemplateMagic.costMp > 0) {
            return owner.getEntityModule().getModAttr().getMP() >= modifierTemplateMagic.costMp;
        }

        return true;
    }

    public boolean checkSP(TemplateMagic conf) {
        if (conf.inspectSP > 0) {
            return owner.getEntityModule().getModAttr().getSP() >= conf.inspectSP;
        }

        return true;
    }

    public boolean needOverlay(TemplateMagic conf) {
        return !magicOverlay.hasOverlay(conf);
    }

    public MagicLogic getCurrentMagic() {
        return currentMagic;
    }

    public Magic getMagic(int sn) {
        // 隐藏技能
        if (concealMagics.containsKey(sn) && concealMagics.get(sn).isActive(owner.getTime())) {
            return concealMagics.get(sn);
        }

        // 有替换技能 读替换技能
        if (!replaceMagics.isEmpty()) {
            for (int slot : keepSlots) {
                Magic magic = magicSlots.get(slot);
                if (magic != null && magic.getSn() == sn) {
                    return magic;
                }
            }
            return replaceMagics.get(sn);
        }

        // 原始技能
        if (magics.containsKey(sn)) {
            return magics.get(sn);
        }

        return this.getRealMagic(sn);
    }

    public Magic getMagicBySlot(int slot) {

        return magicSlots.get(slot);
    }

    public Magic getRealMagic(int sn) {
        PlayerEntity playerEntity = owner.queryObject(PlayerEntity.class);
        if (playerEntity == null /*|| !playerEntity.getEntityModule().getModVehicle().isRiding()*/) {
            return null;
        }

        // 坐骑替换技能
        for (Magic obj : magics.values()) {
            if (obj.getRealSn() == sn) {
                return obj;
            }
        }

        return null;
    }

    public void initial(MsgLs.LSEnterScene msg) {
        MsgDef.DMagicInfo dMagicInfo = msg.getMagicInfo();
        dMagicInfo.getMagicsList().forEach(this::initMagicList);
        // 被动
        msg.getPassivesList().forEach(passive -> addPassiveMagic(passive.getKey(), passive.getValue()));
        for (var dGroup : msg.getGroupPassivesList()) {
            replaceGroupPassives(dGroup.getMagicKey(), new HashSet<>(dGroup.getMagicsList()));
        }

        // cd信息
        for (MsgDef.DMagicCooldown dMagicCD : dMagicInfo.getMagicsCDList()) {
            MagicCDData magicCD = new MagicCDData(dMagicCD.getSn(), dMagicCD.getCooldown());
            getMagicCD().addCD(magicCD);
        }
        // 槽位cd
        dMagicInfo.getSlotCDList().forEach(cd -> magicCD.addSlotCD(cd.getSn(), cd.getCooldown()));
        // 叠层技能
        for (MsgDef.DMagicOverlay dOverlay : dMagicInfo.getMagicOverlayList()) {
            magicOverlay.add(dOverlay);
        }
    }

    /**
     * 是否包含此被动技能
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public boolean containsPassiveMagic(int sn) {

        return passiveMagics.containsKey(sn);
    }

    /**
     * 更新同一个key下的被动
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public void replaceGroupPassives(String key, Set<Integer> newPassives) {
        List<Integer> previousPassives = (List<Integer>) groupPassives.get(key);
        if (LogServer.scene.isDebugEnabled()) {
            final String magicSnStr = Joiner.on(",").join(groupPassives.values().iterator());
            LogServer.scene.debug("更新被动技能组 {}.更新前:{}", key, magicSnStr);
        }
        if (newPassives.isEmpty()) {
            removePassiveMagics(previousPassives);
            groupPassives.removeAll(key);
        } else {
            for (Iterator<Integer> iterator = previousPassives.iterator(); iterator.hasNext(); ) {
                int passive = iterator.next();
                if (!newPassives.remove(passive)) {
                    removePassiveMagic(passive);
                    iterator.remove();
                }
            }
            addPassiveMagics(newPassives);
            groupPassives.putAll(key, newPassives);
        }
        if (LogServer.scene.isDebugEnabled()) {
            final String magicSnStr = Joiner.on(",").join(groupPassives.values().iterator());
            LogServer.scene.debug("{}. 更新后:{}", key, magicSnStr);
        }
    }

    /***
     * 添加被动技能
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public void addPassiveMagics(Collection<Integer> magics) {

        magics.forEach(passive -> addPassiveMagic(passive, 1));
    }

    /**
     * 添加被动技能
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public void addPassiveMagic(int magicSn, int level) {
        TemplateMagic TemplateMagic = TemplateMagic.get(magicSn);
        if (TemplateMagic == null) {
            LogServer.fight.error("Initial Passive magic failed. TemplateMagic sn {} doesn't exist.", magicSn);
            return;
        }
        if (!TemplateMagic.isPassiveMagic) {
            throw new RuntimeException("Magic sn " + magicSn + " isn't passive magic.");
        }

        MagicPassive magicPassive = passiveMagics.get(magicSn);
        if (magicPassive == null) {
            magicPassive = new MagicPassive(TemplateMagic, level);
            passiveMagics.put(magicSn, magicPassive);
        } else {
            magicPassive.cleanup(owner);
        }

        if (owner.isInWorld()) {
            magicPassive.startup(owner);
        }
    }

    /**
     * 移除被动技能
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public void removePassiveMagics(Collection<Integer> magics) {
        if (!magics.isEmpty()) {
            magics.forEach(this::removePassiveMagic);
        }
    }

    /**
     * 移除指定被动技能
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public void removePassiveMagic(int sn) {

        cleanPassiveMagic(sn, true);
    }

    //清理全部被动技能效果*/
    public void cleanPassiveMagic() {
        if (!owner.isInWorld()) {
            return;
        }

        this.passiveMagics.values().forEach(magicPassive -> magicPassive.cleanup(owner));
    }

    //获取多段技能的开始位置*/
    public Vector3D getMultiStartPosition() {
        if (currentMagic == null) {
            return null;
        }
        TemplateMagic confStart = currentMagic.getConfContinuesStart();
        if (confStart == null) {
            return null;
        }
        MagicContinuesContext context = successiveInfos.get(confStart.sn);
        if (context == null) {
            return null;
        }
        return context.startPosition;
    }

    /**
     * 取消禁用指定类型的技能
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public void enableMagicType(int magicType, int errorCode) {
        List<Integer> errorCodes = this.disableTypes.get(magicType);
        if (errorCodes != null) {
            var iterator = errorCodes.iterator();
            while (iterator.hasNext()) {
                if (iterator.next() == errorCode) {
                    iterator.remove();
                    break;
                }
            }
            if (errorCodes.isEmpty()) {
                disableTypes.remove(magicType);
            }
        }
    }

    /**
     * 禁用指定类型的技能
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public void disableMagicType(int magicType, int errorCode) {
        this.disableTypes.compute(magicType, (key, errorCodes) -> {
            if (errorCodes == null) {
                errorCodes = new ArrayList<>();
            }
            errorCodes.add(errorCode);

            return errorCodes;
        });
    }

    /**
     * 判定技能是否被禁用
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public int isDisabled(TemplateMagic TemplateMagic) {
        List<Integer> errorCodes = this.disableTypes.get(TemplateMagic.skillType);
        if (errorCodes == null || errorCodes.isEmpty()) {
            return ErrorCode.SUCCESS;
        }

        return errorCodes.get(0);
    }

    public boolean isReplacing() {

        return !replaceMagics.isEmpty();
    }

    public void sendMsg() {
        PlayerEntity sceneRole = owner.queryObject(PlayerEntity.class);
        if (sceneRole == null) {
            return;
        }

        MsgMagic.SCMagicQuery.Builder msg = MsgMagic.SCMagicQuery.newBuilder();
        if (!replaceMagics.isEmpty()) {
            for (Magic obj : replaceMagics.values()) {
                msg.addMagics(obj.toDMagic());
            }
            for (int slot : keepSlots) {
                // 需要保留的原技能槽位
                Magic magic = magicSlots.get(slot);
                if (magic != null)
                    msg.addMagics(magic.toDMagic());
            }
        } else {
            for (Magic obj : magics.values()) {
                msg.addMagics(obj.toDMagic());
            }
        }

        magicCD.buildMsg(msg);
        magicOverlay.buildMsg(msg);

        sceneRole.sendMsg(msg);
    }

    /**
     * 加入叠层技能管理
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    private void addOverlayMagic(Magic obj) {
        TemplateMagic conf = TemplateMagic.get(obj.getSn());
        if (conf == null) {
            return;
        }

        this.magicOverlay.add(conf);
    }

    //生效全部被动技能效果*/
    private void startPassiveMagic() {
        if (!owner.isInWorld())
            return;

        this.passiveMagics.values().forEach(magicPassive -> magicPassive.startup(owner));
    }

    /**
     * 清理指定被动技能
     *
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    private void cleanPassiveMagic(int sn, boolean remove) {
        MagicPassive magicPassive = remove ? this.passiveMagics.remove(sn) : this.passiveMagics.get(sn);
        if (magicPassive == null || !magicPassive.isActive()) {
            return;
        }

        magicPassive.cleanup(owner);
    }


    // GM 操作
    public void doWTF() {
        LogServer.skill.warn("玩家id={},name={} 开启了 WTF", owner.getId(), owner.getName());
        this.wtf = true;
        refreshCD();
        float maxXP = owner.getEntityModule().getModAttr().getXPMax();
        owner.getEntityModule().getModAttr().setXP(maxXP);
        SceneMsgHelper.sendUpdateXPToOwner(owner, maxXP);
    }

    public void endWTF() {
        this.wtf = false;
    }

    /**
     * 序列化技能信息
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public MsgDef.DMagicInfo buildDMagicInfo() {
        MsgDef.DMagicInfo.Builder dMagicInfo = MsgDef.DMagicInfo.newBuilder();
        magics.values().forEach(magic -> dMagicInfo.addMagics(magic.toDMagic()));
        dMagicInfo.addAllMagicsCD(magicCD.buildMagicsCDList());
        dMagicInfo.addAllSlotCD(magicCD.buildSlotsCDList());
        dMagicInfo.addAllMagicOverlay(magicOverlay.serializeOverlay());
        return dMagicInfo.build();
    }

    /**
     * 序列化被动技能列表信息
     * @Author t13max
     * @Date 16:25 2024/12/13
     */
    public List<MsgDef.DIntKeyIntValue> buildPassiveMagicListInfo() {
        List<MsgDef.DIntKeyIntValue> list = new ArrayList<>();
        passiveMagics.forEach((k, v) -> list.add(MsgDef.DIntKeyIntValue.newBuilder().setKey(k).setValue(v.getLevel()).build()));
        return list;
    }

    public Map<Integer, MagicConceal> getConcealMagics() {
        return concealMagics;
    }

    public TemplateMagic getActuallyMagic(TemplateMagic TemplateMagic) {
        if (TemplateMagic == null) {
            return null;
        }

        ModifierTemplateMagic modifier = owner.getModifier().getTemplateMagicModifier(TemplateMagic);

        if (!modifier.isContinus) {
            return TemplateMagic;
        }

        MagicContinuesContext context = successiveInfos.get(TemplateMagic.sn);
        if (context == null)
            return TemplateMagic;
        if (context.templateMagicContinues != null && owner.getTime() <= context.continuesStartTime) {
            return context.templateMagicContinues;
        }
        return TemplateMagic;
    }

    public void addDynamicBlock(int time, MsgDef.DDynamicBlock dDynamicBlock) {
        this.fightBlocks.add(new DynamicBlockEffect(owner.getTime() + time, dDynamicBlock));
        owner.serializeRoleInfo();
    }

    private void setCurrentMagic(MagicLogic magic) {
        // 上一个技能
        this.lastMagic = currentMagic;
        //当前最新技能
        this.currentMagic = magic;
    }

    public void handleSuccessiveMagic(ModifierTemplateMagic modifier, Map<Integer, MagicContinuesContext> successiveInfos, int successive) {
        if (!modifier.isContinus || successive <= 0 || TemplateMagic.get(successive) == null) {
            return;
        }
        TemplateMagic conf = modifier.sourceConf;
        MagicContinuesContext context = successiveInfos.get(conf.sn);
        if (context == null) {
            return;
        }

        final int SUCCESSIVE_MAX = 100;
        int successiveSn = conf.sn;

        for (int i = 0; i < SUCCESSIVE_MAX; ++i) {
            TemplateMagic successiveConf = TemplateMagic.get(successiveSn);
            if (successiveConf == null) {
                break;
            }
            ModifierTemplateMagic successiveModifier = owner.getModifier().getTemplateMagicModifier(successiveConf);
            // 实际释放技能修正
            if (successiveConf.sn == successive) {
                context.templateMagicContinues = successiveConf;
                break;
            }

            if (successiveModifier.continusMagic <= 0) {
                break;
            }

            successiveSn = successiveModifier.continusMagic;
        }
    }
}
