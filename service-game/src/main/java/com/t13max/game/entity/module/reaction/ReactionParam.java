package com.t13max.game.entity.module.reaction;

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


}
