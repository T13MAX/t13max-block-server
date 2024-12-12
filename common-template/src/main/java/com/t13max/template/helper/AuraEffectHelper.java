package com.t13max.template.helper;

import com.t13max.template.ITemplate;
import com.t13max.template.temp.TemplateAura;
import com.t13max.template.temp.TemplateMagicEffect;

/**
 * @author t13max
 * @since 16:50 2024/12/11
 */
public class AuraEffectHelper extends TemplateHelper<TemplateMagicEffect>{

    public AuraEffectHelper(String fileName) {
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
