package com.t13max.game.entity.module;

import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.aura.EntityAuraMod;
import com.t13max.game.entity.module.motion.EntityMotionMod;
import com.t13max.game.entity.module.station.EntityStationMod;
import com.t13max.game.exception.GameException;
import com.t13max.util.PackageUtil;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 实体模块合集
 *
 * @author: t13max
 * @since: 15:51 2024/7/25
 */
@Getter
public class EntityModules {

    private final Map<Class<? extends EntityModule>, EntityModule> moduleMap = new HashMap<>();

    private final IEntity owner;

    public EntityModules(IEntity owner) {
        this.owner = owner;
        this.initModules();
    }

    private void initModules() {
        try {
            Set<Class<?>> classSet = PackageUtil.scanCache("com.t13max.game.entity.module");
            for (Class<?> clazz : classSet) {
                // 只需要加载TemplateHelper注解数据
                if (!EntityModule.class.isAssignableFrom(clazz) || Modifier.isAbstract(clazz.getModifiers())) {
                    continue;
                }
                // 创建实例
                Object inst = clazz.getDeclaredConstructor(IEntity.class).newInstance(owner);
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

    public EntityMotionMod getMotionMod() {
        return this.getEntityModule(EntityMotionMod.class);
    }

    public EntityStationMod getStationMod() {
        return this.getEntityModule(EntityStationMod.class);
    }

    public EntityAuraMod getAuraMod() {
        return this.getEntityModule(EntityAuraMod.class);
    }

    /**
     * 实体模块tick 被实体tick调用
     *
     * @Author t13max
     * @Date 16:17 2024/12/9
     */
    public void tick(long now) {
        this.moduleMap.values().forEach(module -> module.tick(now));
    }

    /**
     * 实体进入世界 被实体的enterWorld 'After' 调用
     *
     * @Author t13max
     * @Date 15:43 2024/12/6
     */
    public void enterWorld() {
        this.moduleMap.values().forEach(EntityModule::enterWorld);
    }

    /**
     * 实体离开世界
     *
     * @Author t13max
     * @Date 15:44 2024/12/6
     */
    public void leaveWorld() {
        this.moduleMap.values().forEach(EntityModule::leaveWorld);
    }
}
