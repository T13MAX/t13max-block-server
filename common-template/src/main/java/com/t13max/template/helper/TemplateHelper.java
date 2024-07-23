package com.t13max.template.helper;

import com.t13max.game.exception.CommonException;
import com.t13max.game.util.Log;
import com.t13max.template.ITemplate;
import com.t13max.template.util.JsonUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * helper抽象父类
 *
 * @author: t13max
 * @since: 14:56 2024/5/23
 */
public abstract class TemplateHelper<T extends ITemplate> {

    protected String fileName;

    protected Map<Integer, T> DATA_MAP;

    protected Map<Integer, T> TEMP_DATA_MAP;

    public TemplateHelper(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 表校验
     *
     * @Author t13max
     * @Date 15:01 2024/5/23
     */
    public abstract boolean configCheck();

    /**
     * 获取class类型
     *
     * @Author t13max
     * @Date 15:10 2024/5/23
     */
    public abstract <T extends ITemplate> Class<T> getClazz();

    /**
     * 临时数据转正
     *
     * @Author t13max
     * @Date 15:01 2024/5/23
     */
    public void transfer() {
        this.DATA_MAP = TEMP_DATA_MAP;
        this.TEMP_DATA_MAP = null;
    }

    /**
     * 重新加载数据
     *
     * @Author t13max
     * @Date 15:03 2024/5/23
     */
    public void reload() {

        Log.template.info("{}从新加载开始!", fileName);

        if (!this.doLoad()) {
            //打印日志 告知没有reload成功
            Log.template.error("加载表失败! fileName={}", fileName);
        }
    }

    /**
     * 首次load
     *
     * @Author t13max
     * @Date 15:04 2024/5/23
     */
    public void load() {

        if (!doLoad()) {
            //直接抛出异常 不让起服
            throw new CommonException("加载表失败");
        }
    }

    public boolean doLoad() {
        TEMP_DATA_MAP = new HashMap<>();
        List<ITemplate> iTemplates = JsonUtils.readCommodityTxt(fileName, this.getClazz());
        if (iTemplates == null || iTemplates.isEmpty()) {
            return false;
        }
        iTemplates.forEach(e -> TEMP_DATA_MAP.put(e.getId(), (T) e));
        return true;
    }


    /**
     * 根据id获取数据
     *
     * @Author t13max
     * @Date 15:02 2024/5/23
     */
    public T getTemplate(int id) {
        return DATA_MAP.get(id);
    }

    /**
     * 获取所有
     *
     * @Author t13max
     * @Date 15:23 2024/5/23
     */
    public Collection<T> getAll() {
        return DATA_MAP.values();
    }

    /**
     * 专门用于校验用的获取所有数据
     *
     * @Author t13max
     * @Date 20:31 2024/5/27
     */
    public Collection<T> getTempAll() {
        return TEMP_DATA_MAP.values();
    }

}
