package com.t13max.game.entity.module.reaction;

import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.EntityModule;

/**
 * 实体行为模块
 *
 * @author t13max
 * @since 14:05 2024/12/10
 */
public class EntityReactionMod extends EntityModule {

    public EntityReactionMod(IEntity owner) {
        super(owner);
    }

    public void trigger(ReactionTriggerEnum reactionTriggerEnum, ReactionParam reactionParam) {

    }
}
