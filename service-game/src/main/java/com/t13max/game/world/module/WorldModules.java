package com.t13max.game.world.module;

import com.t13max.game.entity.IEntity;
import com.t13max.game.exception.GameException;
import com.t13max.game.pos.Vector3D;
import com.t13max.game.world.World;
import com.t13max.util.PackageUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 世界模块集合
 *
 * @author: t13max
 * @since: 16:18 2024/7/25
 */
public class WorldModules {

    private final World owner;

    private final Map<Class<? extends WorldModule>, WorldModule> moduleMap = new HashMap<>();

    public WorldModules(World owner) {
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
            Set<Class<?>> classSet = PackageUtil.scanCache("com.t13max.game.world.module");
            for (Class<?> clazz : classSet) {
                // 只需要加载TemplateHelper注解数据
                if (!WorldModule.class.isAssignableFrom(clazz) || Modifier.isAbstract(clazz.getModifiers())) {
                    continue;
                }
                // 创建实例
                Object inst = clazz.getDeclaredConstructor(World.class).newInstance(owner);
                WorldModule module = (WorldModule) inst;
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
    public <T extends WorldModule> T getWorldModule(Class<T> clazz) {
        return (T) moduleMap.get(clazz);
    }

    public WorldEntityMod getEntityMod() {
        return this.getWorldModule(WorldEntityMod.class);
    }

    public WorldDetourMod getDetourMod() {
        return this.getWorldModule(WorldDetourMod.class);
    }

    /**
     * tick
     *
     * @Author t13max
     * @Date 15:48 2024/12/6
     */
    public void pulse() {
        //是不是应该控制一下顺序?
        this.moduleMap.values().forEach(WorldModule::pulse);
    }

    public void pulsePerSec() {
        //是不是应该控制一下顺序?
        this.moduleMap.values().forEach(WorldModule::pulsePerSec);
    }

    /**
     * 实体进入世界 被对应的世界调用
     *
     * @Author t13max
     * @Date 15:43 2024/12/6
     */
    public void enterWorld(IEntity entity) {
        this.moduleMap.values().forEach(module -> module.enterWorld(entity));
    }

    /**
     * 实体离开世界
     *
     * @Author t13max
     * @Date 15:44 2024/12/6
     */
    public void leaveWorld(IEntity entity) {
        this.moduleMap.values().forEach(module -> module.leaveWorld(entity));
    }

    /**
     * 实体移动
     *
     * @Author t13max
     * @Date 14:10 2024/8/14
     */
    public void onEntityMoved(IEntity entity, Vector3D oldPos, Vector3D newPos) {
        this.moduleMap.values().forEach(module -> module.onEntityMoved(entity, oldPos, newPos));
    }
}
