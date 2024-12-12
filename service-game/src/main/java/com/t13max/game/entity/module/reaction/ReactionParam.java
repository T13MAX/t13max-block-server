package com.t13max.game.entity.module.reaction;

import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.aura.Aura;
import com.t13max.util.TimeUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author t13max
 * @since 14:07 2024/12/10
 */
public class ReactionParam {

    private final long triggerMills;

    private final Map<String, Object> paramMap = new HashMap<>();

    public ReactionParam(long triggerMills) {
        this.triggerMills = triggerMills;
    }

    public ReactionParam(float value, float diff, IEntity target, int magicSn) {
        this.triggerMills = TimeUtil.nowMills();
        //后续 优化
    }

    public ReactionParam(Aura aura) {
        this.triggerMills = TimeUtil.nowMills();
    }
}
