package com.t13max.game.entity.module.aura;

import com.google.protobuf.Message;
import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.PlayerEntity;
import com.t13max.game.entity.module.EntityModule;
import com.t13max.game.exception.GameException;
import com.t13max.template.helper.AuraHelper;
import com.t13max.template.manager.TemplateManager;
import com.t13max.template.temp.TemplateAura;
import world.api.SCAuraBegan;
import world.api.SCAuraEnded;
import world.api.SCAuraUpdate;
import world.entity.PBAura;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 光环模块
 *
 * @author t13max
 * @since 10:37 2024/12/10
 */
public class EntityAuraMod extends EntityModule {

    //自身拥有的buff效果
    private final Map<Integer, List<AuraBuff>> ownedBuffs = new HashMap<>();

    //自身拥有的光环
    private final List<Aura> ownedAuras = new ArrayList<>();

    private final List<Aura> readyAuras = new ArrayList<>();

    public EntityAuraMod(IEntity owner) {
        super(owner);
    }


    @Override
    public void enterWorld() {
        super.enterWorld();

        var iterator = readyAuras.iterator();
        while (iterator.hasNext()) {
            Aura aura = iterator.next();
            aura.startup(owner, 0);
            iterator.remove();
        }
    }

    @Override
    public void leaveWorld() {
        super.leaveWorld();
        for (Aura aura : ownedAuras) {
            aura.onLeaveWorld();
        }

        // 离开场景时清理一下别人施加的光环
        List<Aura> invalidList = flatMapBuffStream()
                .filter(auraBuff -> auraBuff.getAura().getOwner() != auraBuff.getTargetValid())
                .map(AuraBuff::getAura)
                .toList();
        for (Aura aura : invalidList) {
            aura.cleanupBuff(AuraConst.AURA_REMOVE_BY_INVALID, owner.getId());
        }
    }

    public void cleanup(int mode, int flags) {
        // 清除aura
        List<Aura> tmpAuras = new ArrayList<>(ownedAuras);
        for (Aura aura : tmpAuras) {
            aura.cleanup(mode, flags);
        }

        // 清除buff
        Collection<AuraBuff> buffs = flatMapBuffStream().collect(Collectors.toSet());
        for (AuraBuff auraBuff : buffs) {
            auraBuff.getAura().cleanupBuff(mode, owner.getId());
        }
    }

    //---------------------------------------------------------------------
    // create or delete

    /**
     * 加载持久化光环
     *
     * @Author t13max
     * @Date 16:47 2024/12/11
     */
    public void addAuraPersist(PBAura pbAura) {
        // 配置不存在
        TemplateAura templateAura = TemplateManager.inst().helper(AuraHelper.class).getTemplate(pbAura.getTemplateId());
        if (templateAura == null) {
            return;
        }

        // 初始化光环
        Aura aura = new Aura(owner.queryObject(PlayerEntity.class), pbAura);
        if (owner.isInWorld())
            aura.startup(owner, 0);
        else
            this.readyAuras.add(aura);
    }

    public Aura createAura(IEntity caster, int auraId, int level, long param) {
        if (caster == null) {
            caster = owner;
        }
        TemplateAura templateAura = TemplateManager.inst().helper(AuraHelper.class).getTemplate(auraId);
        if (templateAura == null) {
            return null;
        }

        return Aura.createAndStart(owner, caster, templateAura, level, 1, null, param);
    }

    /**
     * 校验buff 同一种只能加一个
     *
     * @Author t13max
     * @Date 16:51 2024/12/11
     */
    public int checkAndAddBuff(int auraId) {
        if (this.containAuraBuff(auraId)) {
            return -1;
        }
        this.createAura(auraId);
        return auraId;
    }

    public Aura createAura(int auraSn) {
        return this.createAura(null, auraSn, 1, 0);
    }

    /**
     * 异步删除
     *
     * @Author t13max
     * @Date 17:36 2024/12/11
     */
    public void deleteAura(int auraSn) {
        AuraBuff app = this.getAuraBuffOrNull(auraSn);
        if (app == null) {
            return;
        }

        app.getAura().getCleaner().cleanupAsync(AuraConst.AURA_REMOVE_BY_CANCEL, 0);
    }

    /**
     * 同步删除
     *
     * @Author t13max
     * @Date 17:38 2024/12/11
     */
    public void deleteAuraSync(int auraSn) {
        AuraBuff app = this.getAuraBuffOrNull(auraSn);
        if (app == null) {
            return;
        }

        app.getAura().cleanup(AuraConst.AURA_REMOVE_BY_CANCEL, 0);
    }

    /**
     * 获取需要持久化的光环
     *
     * @Author t13max
     * @Date 17:38 2024/12/11
     */
    public Collection<Aura> getPersistAuras() {
        return this.ownedAuras.stream()
                .filter(Aura::isPersist)
                .collect(Collectors.toList());
    }

    /**
     * 获取自身所有AuraBuff的Stream
     *
     * @Author t13max
     * @Date 17:38 2024/12/11
     */
    public Stream<AuraBuff> flatMapBuffStream() {

        return ownedBuffs.values().stream()
                .flatMap(List::stream);
    }

    /**
     * 根据友善度（增益、减益、中立）获取buff列表
     *
     * @Author t13max
     * @Date 17:38 2024/12/11
     */
    public List<AuraBuff> getBuffsByFriendly(int type) {

        return ownedBuffs.getOrDefault(type, Collections.emptyList());
    }

    /**
     * 为新来的效果buff腾出空间(因buff数量受限)
     *
     * @Author t13max
     * @Date 17:33 2024/12/11
     */
    public void spareSpace(TemplateAura templateAura) {
        List<AuraBuff> apps = ownedBuffs.get(templateAura.friendlyLevel);
        if (apps != null && apps.size() >= AuraConst.MAX_AURA_APPS_PER_FRIENDLY) {
            AuraBuff app = apps.get(0);
            app.getAura().cleanupBuff(AuraConst.AURA_REMOVE_BY_REPLACE, app.getTargetId());
        }
    }

    //---------------------------------------------------------------------
    // msg

    public void onAuraBuffBegan(AuraBuff auraBuff) {
        SCAuraBegan.Builder msg = SCAuraBegan.newBuilder();

        msg.setAura(auraBuff.toDAura());

        auraNotify(auraBuff.getAura(), msg.build());
    }

    public void onAuraBuffUpdate(long casterId, int reason, AuraBuff auraBuff) {
        SCAuraUpdate.Builder msg = SCAuraUpdate.newBuilder();

        auraNotify(auraBuff.getAura(), msg.build());
    }

    public void onAuraBuffEnded(AuraBuff auraBuff) {
        if (auraBuff.isIncludeFlags(AuraConst.AURA_APP_FLAGS_CLEAN_MSG_SENT)) {
            return;
        }
        auraBuff.includeFlags(AuraConst.AURA_APP_FLAGS_CLEAN_MSG_SENT);
        SCAuraEnded.Builder msg = SCAuraEnded.newBuilder();

        auraNotify(auraBuff.getAura(), msg.build());
    }

    /**
     * 身上是否有指定光环buff
     *
     * @Author t13max
     * @Date 17:40 2024/12/11
     */
    public boolean containAuraBuff(int auraId) {
        AuraBuff auraBuff = getAuraBuffOrNull(auraId);
        if (auraBuff == null) {
            return false;
        }

        return !auraBuff.isFinished();
    }

    public AuraBuff getAuraBuffByCaster(int auraId, long casterId) {
        TemplateAura templateAura = TemplateManager.inst().helper(AuraHelper.class).getTemplate(auraId);
        if (templateAura != null) {
            List<AuraBuff> apps = ownedBuffs.get(templateAura.friendlyLevel);
            if (apps != null) {
                for (AuraBuff auraBuff : apps) {
                    if (auraBuff.getAura().getTemplateAura().id == auraId && auraBuff.getAura().getCasterId() == casterId) {
                        return auraBuff;
                    }
                }
            }
        }
        return null;
    }

    public AuraBuff getAuraBuffOrNull(int auraId) {
        TemplateAura templateAura = TemplateManager.inst().helper(AuraHelper.class).getTemplate(auraId);
        if (templateAura != null) {
            List<AuraBuff> apps = ownedBuffs.get(templateAura.friendlyLevel);
            if (apps != null) {
                for (AuraBuff app : apps) {
                    if (app.getAura().getTemplateAura().id == auraId) {
                        return app;
                    }
                }
            }
        }

        return null;
    }

    /**
     * 根据光环类型获取其光环buff数量
     *
     * @Author t13max
     * @Date 17:41 2024/12/11
     */
    public int getAuraBuffSizeByType(int type) {

        return ownedBuffs.getOrDefault(type, Collections.emptyList()).size();
    }

    /**
     * 根据光环组获取buff叠层总数
     *
     * @Author t13max
     * @Date 17:41 2024/12/11
     */
    public int getAuraBuffOverlapTotal(int group) {
        return getAuraBuffsStream(group)
                .map(AuraBuff::getOverlap)
                .reduce(Integer::sum)
                .orElse(0);
    }

    public Stream<AuraBuff> getAuraBuffsStream(int group) {

        return ownedBuffs.values().stream()
                .flatMap(List::stream)
                .filter(buff -> buff.getAura().getTemplateAura().signGroup == group)
                .filter(auraBuff -> !auraBuff.isFinished());
    }

    /**
     * 增加一个aura到集合,不做其它处理
     *
     * @Author t13max
     * @Date 16:55 2024/12/11
     */
    void add(Aura aura) {
        if (!ownedAuras.contains(aura)) {
            ownedAuras.add(aura);
        }
    }

    /**
     * 从集合中,删除一个aura,不做其它处理
     *
     * @Author t13max
     * @Date 16:55 2024/12/11
     */
    void remove(Aura aura) {
        ownedAuras.remove(aura);
    }

    /**
     * 增加一个aura buff到集合,不做其它处理
     *
     * @Author t13max
     * @Date 16:55 2024/12/11
     */
    void addAuraBuff(AuraBuff auraBuff) {
        TemplateAura conf = auraBuff.getAura().getTemplateAura();
        if (conf == null) {
            return;
        }

        List<AuraBuff> buffList = ownedBuffs.computeIfAbsent(conf.friendlyLevel, k -> new ArrayList<>());

        buffList.add(auraBuff);

        owner.serializeRoleInfo();
    }

    /**
     * 从集合中,删除一个aura buff,不做其它处理
     *
     * @Author t13max
     * @Date 16:55 2024/12/11
     */
    void removeAuraBuff(AuraBuff auraBuff) {
        TemplateAura conf = auraBuff.getAura().getTemplateAura();
        if (conf == null) {
            return;
        }

        List<AuraBuff> buffs = ownedBuffs.get(conf.friendlyLevel);
        if (buffs == null || buffs.size() <= 0) {
            return;
        }

        buffs.remove(auraBuff);

        owner.serializeRoleInfo();
    }

    /**
     * 光环级 关系
     *
     * @Author t13max
     * @Date 16:55 2024/12/11
     */
    AuraConflict getAuraConflict(Aura aura) {
        AuraConflict auraConflict = new AuraConflict();
        auraConflict.conflict = AuraConst.AURA_SIDE_EFFECT_ATTACH;
        for (Aura existAura : ownedAuras) {
            // cleanup ready
            if (existAura.getCleaner().isAsyncCleanup()) {
                continue;
            }

            // need same aura
            if (existAura.getTemplateId() != aura.getTemplateId()) {
                continue;
            }

            // different source
            if (existAura.getCasterId() != aura.getCasterId()) {
                continue;
            }

            auraConflict.existAuras.add(existAura);

            int conflict = this.getAuraConflictType(aura, existAura);
            switch (conflict) {
                case AuraConst.AURA_CONFLICT_OVERLAP_REFRESH: {
                    // 叠加并刷新时间
                    if (auraConflict.conflict <= AuraConst.AURA_SIDE_EFFECT_OVERLAP_REFRESH) {
                        auraConflict.conflict = AuraConst.AURA_SIDE_EFFECT_OVERLAP_REFRESH;
                        auraConflict.aura = existAura;
                    }
                }
                break;
                case AuraConst.AURA_CONFLICT_OVERLAP: {
                    // 叠加
                    if (auraConflict.conflict <= AuraConst.AURA_SIDE_EFFECT_OVERLAP) {
                        auraConflict.conflict = AuraConst.AURA_SIDE_EFFECT_OVERLAP;
                        auraConflict.aura = existAura;
                    }
                }
                break;
                case AuraConst.AURA_CONFLICT_REFRESH_EXISTS: {
                    // 刷新现有同id buff
                    if (auraConflict.conflict <= AuraConst.AURA_SIDE_EFFECT_REFRESH_EXISTS) {
                        auraConflict.conflict = AuraConst.AURA_SIDE_EFFECT_REFRESH_EXISTS;
                        auraConflict.aura = existAura;
                    }
                }
                break;
                case AuraConst.AURA_CONFLICT_APPEND_DURATION: {
                    // 刷新现有同id buff时间
                    if (auraConflict.conflict <= AuraConst.AURA_SIDE_EFFECT_APPEND_DURATION) {
                        auraConflict.conflict = AuraConst.AURA_SIDE_EFFECT_APPEND_DURATION;
                        auraConflict.aura = existAura;
                    }
                }
                break;
                case AuraConst.AURA_CONFLICT_REPLACE: {
                    // 替换现有光环(立即结束现有光环)
                    if (auraConflict.conflict <= AuraConst.AURA_SIDE_EFFECT_REPLACE) {
                        auraConflict.conflict = AuraConst.AURA_SIDE_EFFECT_REPLACE;
                        auraConflict.aura = existAura;
                    }
                }
                break;
                // 其他关系 光环不做处理 默认给个免疫
                // 理论上讲 这个时候
                default:
                    auraConflict.conflict = AuraConst.AURA_SIDE_EFFECT_IMMUNE;
            }
        }

        return auraConflict;
    }

    /**
     * 光环buff 冲抵关系
     *
     * @Author t13max
     * @Date 16:55 2024/12/11
     */
    int getAttachSideEffects(Aura aura, AuraAttachContext context) {
        AtomicInteger result = new AtomicInteger(AuraConst.AURA_SIDE_EFFECT_ATTACH);

        flatMapBuffStream().forEach(buff -> {
            if (buff.getAura().getCleaner().isAsyncCleanup()) {
                return;
            }

            TemplateAura existConf = buff.getAura().getTemplateAura();
            if (existConf == null) {
                return;
            }

            int conflictType = this.getConflictType(aura, buff.getAura());
            switch (conflictType) {
                // 叠加
                case AuraConst.AURA_CONFLICT_OVERLAP_REFRESH: {
                    context.overlapBuff = buff;

                    if (result.get() <= AuraConst.AURA_SIDE_EFFECT_OVERLAP) {
                        result.set(AuraConst.AURA_SIDE_EFFECT_OVERLAP);
                    }
                }
                break;
                // 冲抵
                case AuraConst.AURA_CONFLICT_REPLACE: {
                    context.replaceBuffs.add(buff);
                }
                break;
                // 免疫
                case AuraConst.AURA_CONFLICT_IMMUNE: {
                    result.set(AuraConst.AURA_SIDE_EFFECT_IMMUNE);
                }
                break;
                // 共存
                case AuraConst.AURA_CONFLICT_COEXISTENCE: {

                }
                break;
                // 刷新现有同id buff
                case AuraConst.AURA_CONFLICT_REFRESH_EXISTS: {
                    context.refreshBuff = buff;

                    if (result.get() <= AuraConst.AURA_SIDE_EFFECT_REFRESH_EXISTS) {
                        result.set(AuraConst.AURA_SIDE_EFFECT_REFRESH_EXISTS);
                    }
                }
                break;
                // 刷新现有同id buff时间
                case AuraConst.AURA_CONFLICT_APPEND_DURATION: {
                    context.refreshBuff = buff;

                    if (result.get() <= AuraConst.AURA_SIDE_EFFECT_APPEND_DURATION) {
                        result.set(AuraConst.AURA_SIDE_EFFECT_APPEND_DURATION);
                    }
                }
                break;
            }
        });

        return result.get();
    }

    /**
     * 获取冲抵类型
     *
     * @Author t13max
     * @Date 16:54 2024/12/11
     */
    private int getConflictType(Aura aura, Aura existAura) {
        TemplateAura templateAura = aura.getTemplateAura();
        TemplateAura existTemplateAura = existAura.getTemplateAura();
        // 同id, 按附加规则处理
        if (templateAura.id == existTemplateAura.id) {
            // 等级冲抵
            int sameLiveStyle = 0;//todo 光环冲抵templateAura.get_sameLiveStyle_upgrade(aura.getMagicLevel());
            switch (sameLiveStyle) {
                case AuraConst.AURA_CONFLICT_REPLACE:
                    if (aura.getLevel() >= existAura.getLevel())
                        return AuraConst.AURA_CONFLICT_REPLACE;
                    else
                        return AuraConst.AURA_CONFLICT_IMMUNE;
                    // 光环叠加、刷新属于光环级处理 走到这里说明了属于不同源
                case AuraConst.AURA_CONFLICT_OVERLAP:
                case AuraConst.AURA_CONFLICT_OVERLAP_REFRESH:
                case AuraConst.AURA_CONFLICT_REFRESH_EXISTS:
                case AuraConst.AURA_CONFLICT_APPEND_DURATION:
                    return AuraConst.AURA_CONFLICT_COEXISTENCE;
                default:
                    return sameLiveStyle;
            }
        }

        // 同组, 按组优先级处理
        if (templateAura.conflictGroup == existTemplateAura.conflictGroup) {
            int conflict = getGroupConflict(templateAura.conflictGroup);
            switch (conflict) {
                case AuraConst.AURA_CONFLICT_REPLACE:     // 优先级冲抵
                    return templateAura.groupPriority >= existTemplateAura.groupPriority ?
                            AuraConst.AURA_CONFLICT_REPLACE : AuraConst.AURA_CONFLICT_IMMUNE;
                case AuraConst.AURA_CONFLICT_IMMUNE:      // 免疫
                    return AuraConst.AURA_CONFLICT_IMMUNE;
                case AuraConst.AURA_CONFLICT_COEXISTENCE: // 共存
                    return AuraConst.AURA_CONFLICT_COEXISTENCE;
                default: {
                    throw new GameException("getConflictType error group conflict rule, id:" + templateAura.id + ", existId:" + existTemplateAura.id);
                }
            }
        }

        return 0;
    }

    private int getGroupConflict(int conflictGroup) {
        return 0;
    }

    private int getAuraConflictType(Aura aura, Aura existAura) {
        TemplateAura templateAura = aura.getTemplateAura();
        int level = aura.getLevel();
        int existLevel = existAura.getLevel();
        // 等级冲抵
        int sameLiveStyle = 0;//templateAura.get_sameLiveStyle_upgrade(aura.getMagicLevel());
        if (sameLiveStyle == AuraConst.AURA_CONFLICT_REPLACE) {
            if (level >= existLevel) {
                return AuraConst.AURA_CONFLICT_REPLACE;
            } else {
                return AuraConst.AURA_CONFLICT_IMMUNE;
            }
        } else if (sameLiveStyle == AuraConst.AURA_CONFLICT_OVERLAP_REFRESH) {
            return AuraConst.AURA_CONFLICT_OVERLAP_REFRESH;
        } else if (sameLiveStyle >= AuraConst.AURA_CONFLICT_OVERLAP_REFRESH && sameLiveStyle <= AuraConst.AURA_CONFLICT_APPEND_DURATION) {
            return sameLiveStyle;
        }

        return AuraConst.AURA_CONFLICT_COEXISTENCE;
    }

    private void auraNotify(Aura aura, Message message) {
        if (aura.isPrivate()) {
            owner.sendMsg(message);
        } else {
            owner.sendMsgToView(message, 0L);
        }
    }
}
