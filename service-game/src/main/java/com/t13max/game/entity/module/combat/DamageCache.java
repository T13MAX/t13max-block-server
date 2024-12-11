package com.t13max.game.entity.module.combat;


import com.t13max.game.entity.IEntity;
import lombok.Getter;

/**
 * 伤害缓存
 *
 * @Author t13max
 * @Date 15:04 2024/12/11
 */
public class DamageCache {

    private final IEntity owner;

    private IEntity caster;
    private long sourceId;
    private int magicSn;
    private int effectSn;
    private int flags;
    private float damage;
    private float absorbedDamage;
    @Getter
    private boolean empty = true;

    public DamageCache(IEntity owner) {
        this.owner = owner;
    }

    public void addDamage(IEntity caster, long sourceId,
                          int magicSn, int effectSn,
                          int flags,
                          float damage, float absorbedDamage,
                          boolean flush) {
        this.caster = caster;
        this.magicSn = magicSn;
        this.sourceId = sourceId;
        this.effectSn = effectSn;
        this.flags |= flags;
        this.damage += damage;
        this.absorbedDamage += absorbedDamage;
        this.empty = false;

        if (flush) flush();
    }

    public void flush() {
        //发送消息
        //DamageProcess.sendDamageMsg(caster, sourceId, owner, magicSn, effectSn, flags, damage, absorbedDamage);
        clear();
    }

    private void clear() {
        this.damage = 0;
        this.flags = 0;
        this.empty = true;
    }
}
