package com.t13max.game.entity.module.skill;


import com.t13max.game.entity.IEntity;
import com.t13max.template.temp.TemplateMagic;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 法术CD
 *
 * @Author t13max
 * @Date 16:31 2024/12/12
 */
public class MagicCD {

    //法术CD
    protected Map<Integer, MagicCDData> coolDowns = new HashMap<>();
    //拥有者
    protected IEntity owner;

    public MagicCD(MagicCD magicCD) {
        this(magicCD.owner);
        magicCD.coolDowns.forEach((key, data) -> coolDowns.put(key, new MagicCDData(data)));
    }

    public MagicCD(IEntity owner) {
        this.owner = owner;
    }

    public void addCD(MagicCDData vo) {
        coolDowns.put(vo.getSn(), vo);
    }

    public Collection<MagicCDData> getCDs() {
        return coolDowns.values();
    }

    /**
     * CD开始
     */
    public void start(TemplateMagic templateMagic, int cdTime) {
        int magicSn = templateMagic.id;

        // 技能cd
        if (cdTime > 0) {
            int magicCD = templateMagic.cdTime;
            if (magicCD > 0) {
                long cd = owner.getTime() + magicCD;
                coolDowns.compute(magicSn, (key, vo) -> {
                    if (vo == null) {
                        vo = new MagicCDData(magicSn);
                    }
                    vo.setCd(cd);
                    return vo;
                });
            }
        }

        this.sendMagicCdUpdate(magicSn, true);
    }

    /**
     * CD检测
     *
     * @Author t13max
     * @Date 16:38 2024/12/12
     */
    public boolean isCD(TemplateMagic templateMagic) {
        MagicCDData vo = coolDowns.get(templateMagic.id);
        return vo != null && vo.getCd() > owner.getTime() + 100;
    }


    /**
     * CD减少
     *
     * @Author t13max
     * @Date 16:36 2024/12/12
     */
    public void reduce(int sn, int timeMS) {
        if (sn <= 0) {
            for (MagicCDData vo : coolDowns.values()) {
                vo.reduce(timeMS);
            }
        } else {
            MagicCDData vo = coolDowns.get(sn);
            if (vo != null) {
                vo.reduce(timeMS);
            }
        }

        this.sendMagicCdUpdate(sn, false);
    }

    /**
     * CD减少
     *
     * @Author t13max
     * @Date 16:35 2024/12/12
     */
    public void reduce(int magicId, float percent) {
        if (magicId <= 0) {
            for (MagicCDData vo : coolDowns.values()) {
                vo.reduce((int) (vo.getCd() * percent));
            }
        } else {
            MagicCDData vo = coolDowns.get(magicId);
            if (vo != null) {
                vo.reduce((int) (vo.getCd() * percent));
            }
        }

        this.sendMagicCdUpdate(magicId, false);

    }

    /**
     * CD增加
     *
     * @Author t13max
     * @Date 16:35 2024/12/12
     */
    public void increase(int magicId, int timeMS) {
        long currTime = owner.getTime();
        MagicCDData cdData = coolDowns.get(magicId);
        if (cdData == null) {
            cdData = new MagicCDData(magicId);
            cdData.setCd(currTime + timeMS);
        } else {
            if (cdData.getCd() <= currTime) {
                cdData.setCd(currTime + timeMS);
            } else {
                cdData.setCd(cdData.getCd() + timeMS);
            }
        }

        this.sendMagicCdUpdate(magicId, false);
    }

    public MagicCD clear() {
        MagicCD magicCD = new MagicCD(this);

        this.reduce(0, 100000 * 1000);

        return magicCD;
    }

    public MagicCD refresh(Collection<Integer> magics) {
        MagicCD magicCD = new MagicCD(this);

        if (magics.isEmpty()) {
            coolDowns.forEach((sn, cdData) -> cdData.reduce(TimeUnit.DAYS.toMillis(1)));
        } else {
            for (int magicSn : magics) {
                MagicCDData vo = coolDowns.get(magicSn);
                if (vo != null) {
                    vo.reduce(TimeUnit.DAYS.toMillis(1));
                }
            }
        }

        this.sendMagicCdUpdate(0, false);

        return magicCD;
    }


    public void copyFrom(MagicCD magicCD) {
        this.coolDowns.clear();
        this.coolDowns.putAll(magicCD.coolDowns);
        this.sendMagicCdUpdate(0, false);
    }

    private void sendMagicCdUpdate(int magicId, boolean check) {

    }

}
