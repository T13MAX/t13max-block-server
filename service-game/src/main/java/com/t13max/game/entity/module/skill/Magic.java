package com.t13max.game.entity.module.skill;


import lombok.Getter;
import lombok.Setter;

/**
 * 法术
 *
 * @Author t13max
 * @Date 16:02 2025/2/11
 */
@Getter
@Setter
public class Magic {
    /**** 法术Sn */
    protected int sn;
    /*** 法术等级 */
    protected int level;
    /*** 法术位置 */
    protected int loc;
    /*** 是否解锁 */
    protected boolean unlock = true;
    /*** 真实法术Sn */
    protected int realSn;
    /*** 原始技能sn 替换前的技能*/
    protected int originSn;

    public Magic(int sn, int level) {
        this.sn = sn;
        this.level = level;
        this.loc = -1;
    }

    public Magic(int sn, int level, int loc) {
        this.sn = sn;
        this.level = level;
        this.loc = loc;
        this.originSn = sn;
    }


    public int getPassive() {
        return 0;
    }
}
