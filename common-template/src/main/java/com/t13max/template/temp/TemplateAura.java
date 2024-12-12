package com.t13max.template.temp;

import com.t13max.template.ITemplate;
import lombok.Data;

/**
 * 实体模板
 *
 * @author: t13max
 * @since: 14:02 2024/4/11
 */
@Data
public class TemplateAura implements ITemplate {


    public int id;

    public int friendlyLevel;
    public int signGroup;
    public int conflictGroup;
    public int groupPriority;
    public int refreshTime;
    public int duration;
    public int targetNumber;
    public int auraTarget;
    public int singleBuff;
    public int flags;
    public int overlapMax;
    public boolean reduceOverlap;
    public boolean manageByMagic;
    public boolean finishWhenOffline;
    public int[] effect;


}