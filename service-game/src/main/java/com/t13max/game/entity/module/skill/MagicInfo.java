package com.t13max.game.entity.module.skill;

import com.t13max.game.entity.IEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @Author t13max
 * @Date 16:30 2024/12/12
 */
@Getter
@Setter
public class MagicInfo implements Serializable {
    //法术sn
    private int sn;
    //法术等级
    private int level;
    //法术槽位置
    private int loc;
    //是否解锁
    private boolean unlock;
    //释放权重
    private int weight;

    private List<Integer> continueList;

    private IEntity chooseTarget;

    public MagicInfo() {
        this.chooseTarget = null;
    }

    public MagicInfo(int sn, int level) {
        this.sn = sn;
        this.level = level;
        this.unlock = true;

    }

    /**
     * 带选定目标的技能
     *
     * @Author t13max
     * @Date 16:31 2024/12/12
     */
    public MagicInfo(int sn, int level, IEntity target) {
        this(sn, level);
        this.chooseTarget = target;
    }

    public void setContinueList(List<Integer> continueList) {
        if (continueList == null || continueList.isEmpty()) {
            return;
        }
        this.continueList = continueList;
    }
}
