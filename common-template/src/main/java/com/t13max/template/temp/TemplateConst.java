package com.t13max.template.temp;

import com.t13max.template.ITemplate;
import lombok.Data;

/**
 * 常量对象
 *
 * @author: t13max
 * @since: 14:02 2024/4/11
 */
@Data
public class TemplateConst implements ITemplate {

    private int id;

    private String params;

}
