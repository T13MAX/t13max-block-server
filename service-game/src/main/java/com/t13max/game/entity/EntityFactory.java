package com.t13max.game.entity;

import com.t13max.persist.data.entity.EntityData;
import com.t13max.persist.data.entity.PlayerData;
import com.t13max.persist.data.entity.ZombieData;
import game.enums.EntityEnum;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

/**
 * 实体工厂类 根据类型创建对应的实体
 *
 * @author t13max
 * @since 15:32 2024/8/19
 */
@UtilityClass
@Log4j2
public class EntityFactory {

    /**
     * 根据类型和data创建内存实体
     *
     * @Author t13max
     * @Date 15:36 2024/8/19
     */
    public static IEntity createEntity(EntityEnum entityEnum, EntityData entityData) {

        IEntity result = null;

        switch (entityEnum) {

            case PLAYER -> {
                if (entityData instanceof PlayerData playerData) {
                    result = new PlayerEntity(playerData);
                }
            }
            case ZOMBIE -> {
                if (entityData instanceof ZombieData zombieData) {
                    result = new ZombieEntity(zombieData);
                }
            }
            default -> {
                log.error("EntityFactory.createEntity, 未知实体类型 or entityData与类型不符 enum={}, entityData={}", entityEnum, entityData.getClass().getSimpleName());
            }
        }

        return result;
    }

}
