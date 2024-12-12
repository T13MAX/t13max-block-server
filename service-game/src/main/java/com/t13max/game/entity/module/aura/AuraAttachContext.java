package com.t13max.game.entity.module.aura;

import java.util.ArrayList;
import java.util.List;

/**
 * 光环附加上下文
 *
 * @Author t13max
 * @Date 17:42 2024/12/11
 */
public class AuraAttachContext {
    public AuraBuff overlapBuff;
    public AuraBuff refreshBuff;
    public List<AuraBuff> replaceBuffs = new ArrayList<>();
}
