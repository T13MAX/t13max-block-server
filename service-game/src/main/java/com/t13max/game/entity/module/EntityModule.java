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

    protected final IEntity owner;

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
    protected void initModule() {

    }

    /**
     * 实体模块tick
     *
     * @Author t13max
     * @Date 16:16 2024/12/9
     */
    protected int tick(long now) {

        return 0;
    }

    public void enterWorld() {

    }

    public void leaveWorld() {
    }
}
