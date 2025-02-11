package com.t13max.game.entity.module.skill;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author t13max
 * @Date 16:31 2024/12/12
 */
@Getter
@Setter
public class MagicCDData {
    private int sn;
    private long cd;

    public MagicCDData(int sn) {
        this.sn = sn;
    }

    public MagicCDData(int sn, long cd) {
        this.sn = sn;
        this.cd = cd;
    }

    public MagicCDData(MagicCDData data) {
        this.sn = data.sn;
        this.cd = data.cd;
    }

    public void reduce(long reduce) {
        this.cd = Math.max(0, (cd - reduce));
    }
}
