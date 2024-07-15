package com.t13max.template.helper;

import com.t13max.template.ITemplate;
import com.t13max.template.temp.TemplateHero;

/**
 * 实体
 *
 * @author: t13max
 * @since: 15:14 2024/5/23
 */
public class EntityHelper extends TemplateHelper<TemplateHero> {

    public EntityHelper() {
        super("entity.json");
    }

    @Override
    public boolean configCheck() {
        return true;
    }

    @Override
    public Class<? extends ITemplate> getClazz() {
        return TemplateHero.class;
    }

}
