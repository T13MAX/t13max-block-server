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
public class TemplateHero implements ITemplate {


    private int id;
    private String name;
    private int atk;
    private int def;
    private int hp;
    private int speed;
    private int element;
    private int mastery;
    private int[] skill;


}