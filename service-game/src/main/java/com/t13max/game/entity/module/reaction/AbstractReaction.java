package com.t13max.game.entity.module.reaction;

import com.t13max.game.entity.IEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 行为骨架实现类
 *
 * @author t13max
 * @since 18:03 2024/12/11
 */
@Getter
@Setter
public abstract class AbstractReaction {
    //类型
    protected final ReactionEnum reactionEnum;
    //是否已结束
    protected boolean finished;
    //是否已删除
    protected boolean deleted;

    public AbstractReaction(ReactionEnum reactionEnum) {
        this.reactionEnum = reactionEnum;
    }

    public abstract int doAction(IEntity owner, ReactionParam param);

    /**
     * 重置状态
     *
     * @Author t13max
     * @Date 10:41 2024/12/12
     */
    public void reset() {
        finished = false;
        deleted = false;
    }
}
