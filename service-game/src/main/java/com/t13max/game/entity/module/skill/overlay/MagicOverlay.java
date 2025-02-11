package com.t13max.game.entity.module.skill.overlay;


import com.t13max.game.entity.IEntity;
import com.t13max.template.temp.TemplateMagic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 法术叠加
 *
 * @Author t13max
 * @Date 16:41 2024/12/12
 */
public class MagicOverlay {

    //列表
    private final Map<Integer, MagicOverlayData> dataMap = new HashMap<>();

    //拥有者
    private final IEntity owner;

    public MagicOverlay(IEntity owner) {
        this.owner = owner;
    }


    public void pulse(long now) {
        if (!dataMap.isEmpty()) {
            for (MagicOverlayData data : dataMap.values()) {
                data.pulse(now);
            }
        }
    }

    public boolean hasOverlay(TemplateMagic templateMagic) {
        if (!dataMap.containsKey(templateMagic.id)) {
            return true;
        }

        return dataMap.get(templateMagic.id).hasOverlay();
    }

    public int add(TemplateMagic templateMagic) {
        if (templateMagic.overlay <= 0) {
            return -1;
        }

        MagicOverlayData obj = new MagicOverlayData(owner, templateMagic);
        this.dataMap.put(templateMagic.id, obj);

        return 0;
    }

    public boolean isEmpty() {
        return dataMap.isEmpty();
    }

    public void onMagicCastEnded(TemplateMagic templateMagic) {
        if (!this.dataMap.containsKey(templateMagic.id)) {
            return;
        }

        this.dataMap.get(templateMagic.id).onMagicCastEnded();
    }

}
