package com.t13max.game.entity.module;

import com.t13max.game.entity.IEntity;
import com.t13max.game.exception.GameException;
import com.t13max.game.world.World;
import com.t13max.game.world.module.WorldModule;
import com.t13max.util.PackageUtil;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 实体模块基类
 *
 * @author: t13max
 * @since: 15:50 2024/7/25
 */
@Getter
public abstract class EntityModule {

    private final IEntity owner;

    private final Map<Class<? extends EntityModule>, EntityModule> moduleMap = new HashMap<>();

    public EntityModule(IEntity owner) {
        this.owner = owner;
        initModule();
    }

    /**
     * 初始化所有模块
     *
     * @Author t13max
     * @Date 16:36 2024/7/25
     */
    private void initModule() {
        try {
            Set<Class<?>> classSet = PackageUtil.scan("com.t13max.game.entity.module");
            for (Class<?> clazz : classSet) {
                // 只需要加载TemplateHelper注解数据
                if (!EntityModule.class.isAssignableFrom(clazz) || Modifier.isAbstract(clazz.getModifiers())) {
                    continue;
                }
                // 创建实例
                Object inst = clazz.getDeclaredConstructor(IEntity.class).newInstance(this);
                EntityModule module = (EntityModule) inst;
                moduleMap.put(module.getClass(), module);
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new GameException(e);
        }
    }

    /**
     * 根据类型获取模块
     *
     * @Author t13max
     * @Date 16:36 2024/7/25
     */
    public <T extends EntityModule> T getEntityModule(Class<T> clazz) {
        return (T) moduleMap.get(clazz);
    }

    public void tick() {
        this.moduleMap.values().forEach(EntityModule::tick);
    }
}
