package com.t13max.game.entity.module.skill;


import com.t13max.game.pos.Vector3D;
import com.t13max.template.temp.TemplateMagic;

/**
 * 法术连招上下文
 *
 * @Author t13max
 * @Date 16:51 2024/12/12
 */
public class MagicContinuesContext {

    //连招开始技能
    public TemplateMagic templateMagic;
    //连招开始时的位置
    public Vector3D startPosition;
    //连招下次技能
    public TemplateMagic templateMagicContinues = null;
    //连招上次结束时间
    public long lastFinishTime = 0L;
    //连招下次开始时间
    public long continuesStartTime = 0L;

    public MagicContinuesContext(TemplateMagic templateMagic) {
        this.templateMagic = templateMagic;
    }

}
