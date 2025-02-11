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
public class TemplateMagic implements ITemplate {


    public int id;

    public int cdTime;

    public int slotCd;
    public int overlay  ;
    public long overlayRecovery;
}