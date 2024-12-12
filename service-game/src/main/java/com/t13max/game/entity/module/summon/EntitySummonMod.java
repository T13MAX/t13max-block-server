package com.t13max.game.entity.module.summon;


import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.EntityModule;
import com.t13max.game.pos.Vector3D;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 召唤物模块
 *
 * @Author t13max
 * @Date 11:20 2024/12/12
 */
public class EntitySummonMod extends EntityModule {

    //召唤物
    public static final int MAX_SUMMON = 20;

    //我的召唤物列表（互斥）
    private final List<Long> mineSummons = new ArrayList<>();

    // 召唤者（互斥）
    @Getter
    private IEntity caller;

    //生命周期是否同召唤者
    private boolean lifecycle;

    public EntitySummonMod(IEntity owner) {
        super(owner);
    }

    @Override
    public void leaveWorld() {
        super.leaveWorld();
        if (!mineSummons.isEmpty()) {
            var iterator = mineSummons.iterator();
            while (iterator.hasNext()) {
                long summonId = iterator.next();
                iterator.remove();
                IEntity entity = this.owner.getWorld().getWorldModules().getEntityMod().getEntity(summonId);
                if (entity != null) {
                    entity.leaveWorld(entity.getWorld());
                }
            }
        }
    }

    /**
     * 召唤
     *
     * @Author t13max
     * @Date 11:25 2024/12/12
     */
    public IEntity summon(int summonId, Vector3D pos, boolean lifecycle, Consumer<IEntity> beforeSpawn) {
        IEntity entity = null;

        // 出生前行为
        if (beforeSpawn != null) {
            beforeSpawn.accept(entity);
        }

        return entity;
    }

    public IEntity summon(int creatureSn, Vector3D pos, boolean lifecycle) {
        return summon(creatureSn, pos, lifecycle, null);
    }

    /**
     * 绑定从属关系
     *
     * @Author t13max
     * @Date 11:26 2024/12/12
     */
    public void bindOwner(IEntity caller, boolean lifecycle) {

        if (this.caller != null) {
            throw new UnsupportedOperationException("不允许从属关系变化");
        }

        this.caller = caller;
        this.lifecycle = lifecycle;

        caller.getEntityModules().getSummonMod().mineSummons.add(owner.getId());
    }

    /**
     * 召唤物失效
     */
    public void summonInvalid(IEntity summon) {
        mineSummons.remove(summon.getId());
        summon.getEntityModules().getSummonMod().caller = null;
    }

    /**
     * 更新主人摘要
     *
     * @Author t13max
     * @Date 11:28 2024/12/12
     */
    public void updateOwnerSummary() {
        if (mineSummons.isEmpty()) {
            return;
        }

    }

    /**
     * 单位死亡
     *
     * @Author t13max
     * @Date 11:28 2024/12/12
     */
    public void onDeath() {
        if (!mineSummons.isEmpty()) {
            List<Long> tempList = new ArrayList<>(mineSummons);
            for (long id : tempList) {
                IEntity entity = owner.getWorld().getWorldModules().getEntityMod().getEntity(id);
                if (entity == null) {
                    continue;
                }

                entity.getEntityModules().getSummonMod().onOwnerDeath();
            }
        }

    }

    /**
     * 召唤者死亡
     *
     * @Author t13max
     * @Date 11:30 2024/12/12
     */
    private void onOwnerDeath() {
        if (lifecycle) {
            owner.killMySelf();
        }
    }

    /**
     * 清除所有召唤物
     *
     * @Author t13max
     * @Date 11:31 2024/12/12
     */
    public void allClearWithoutPet() {
        List<IEntity> removeList = new ArrayList<>();
        for (long id : mineSummons) {
            IEntity entity = owner.getWorld().getWorldModules().getEntityMod().getEntity(id);
            if (entity == null) {
                continue;
            }
            removeList.add(entity);
        }
        removeList.forEach(v -> v.leaveWorld(owner.getWorld()));
    }

    /**
     * 清理指定类型的召唤物
     *
     * @Author t13max
     * @Date 11:31 2024/12/12
     */
    public void cleanSummon(Class<? extends IEntity> clazz) {
        for (long id : mineSummons) {
            IEntity entity = owner.getWorld().getWorldModules().getEntityMod().getEntity(id);
            if (entity == null) {
                continue;
            }
            if (entity.isKindOf(clazz)) {
                //删除
            }
        }
    }

    /**
     * 杀死指定召唤物
     *
     * @Author t13max
     * @Date 11:31 2024/12/12
     */
    public void killSummons(int templateId) {
        for (long id : mineSummons) {
            IEntity entity = owner.getWorld().getWorldModules().getEntityMod().getEntity(id);
            if (entity == null) {
                continue;
            }
            /*if (entity.getTemplateId() == templateId) {
                entity.killMySelf();
            }*/
        }
    }

    /**
     * 返回所有的召唤物
     *
     * @return
     */
    public List<IEntity> getMineSummons() {
        List<IEntity> res = new ArrayList<>(8);
        for (long id : mineSummons) {
            IEntity summon = owner.getWorld().getWorldModules().getEntityMod().getEntity(id);
            if (summon == null) {
                continue;
            }
            res.add(summon);
        }

        return res;
    }

}
