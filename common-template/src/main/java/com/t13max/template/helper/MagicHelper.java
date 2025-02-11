package com.t13max.template.helper;

import com.t13max.template.ITemplate;
import com.t13max.template.temp.TemplateAura;
import com.t13max.template.temp.TemplateMagic;

/**
 * @author t13max
 * @since 16:50 2024/12/11
 */
public class MagicHelper extends TemplateHelper<TemplateMagic>{

    public MagicHelper(String fileName) {
        super(fileName);
    }

    @Override
    public boolean configCheck() {
        return false;
    }

    @Override
    public <T extends ITemplate> Class<T> getClazz() {
        return null;
    }
}
