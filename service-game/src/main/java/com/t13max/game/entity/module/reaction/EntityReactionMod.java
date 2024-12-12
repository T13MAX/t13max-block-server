package com.t13max.game.entity.module.reaction;

import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.EntityModule;

import java.util.*;

/**
 * 实体反应器模块
 * 在各种行为之后触发的一些反应器
 *
 * @author t13max
 * @since 14:05 2024/12/10
 */
public class EntityReactionMod extends EntityModule {

    //反应器集合
    private final Map<ReactionEnum, Set<AbstractReaction>> type2Reactions = new EnumMap<>(ReactionEnum.class);

    //待销毁集合
    private final List<AbstractReaction> objDestructed = new ArrayList<>();

    public EntityReactionMod(IEntity owner) {
        super(owner);
    }

    @Override
    public int pulse(long now) {
        return this.pulseDestructed();
    }

    @Override
    public int pulsePerSec(long now) {
        super.pulsePerSec(now);

        trigger(ReactionEnum.REACTION_PER_SECOND, null);
        return 0;
    }

    /**
     * 把待销毁的销毁
     *
     * @Author t13max
     * @Date 10:52 2024/12/12
     */
    private int pulseDestructed() {
        if (!objDestructed.isEmpty()) {
            for (AbstractReaction inst : objDestructed) {
                Set<AbstractReaction> reactions = type2Reactions.get(inst.getReactionEnum());
                if (reactions != null) {
                    reactions.remove(inst);
                }
            }

            objDestructed.clear();
        }
        return 0;
    }

    /**
     * 添加一个反应器
     *
     * @Author t13max
     * @Date 10:51 2024/12/12
     */
    public void addReaction(AbstractReaction reaction) {
        this.type2Reactions.compute(reaction.getReactionEnum(), (type, reactions) -> {
            if (reactions == null) {
                reactions = new HashSet<>();
            }
            reactions.add(reaction);

            return reactions;
        });
    }

    /**
     * 移除反应器
     *
     * @Author t13max
     * @Date 10:50 2024/12/12
     */
    public void remReaction(AbstractReaction inst) {
        if (inst == null) {
            return;
        }

        inst.setFinished(true);
        inst.setDeleted(true);

        this.objDestructed.add(inst);
    }

    /**
     * 触发反应器
     *
     * @Author t13max
     * @Date 10:51 2024/12/12
     */
    public void trigger(ReactionEnum reactionEnum, ReactionParam param) {
        Collection<AbstractReaction> reactions = this.type2Reactions.get(reactionEnum);
        if (reactions == null || reactions.isEmpty()) {
            return;
        }
        Set<AbstractReaction> copy = new HashSet<>(reactions);
        for (AbstractReaction reaction : copy) {
            if (reaction.isFinished() || reaction.isDeleted()) {
                continue;
            }

            reaction.doAction(owner, param);

            if (reaction.isFinished()) {
                this.remReaction(reaction);
            }
        }
    }
}
