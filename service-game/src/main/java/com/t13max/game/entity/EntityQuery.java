package com.t13max.game.entity;

/**
 * @author: t13max
 * @since: 16:04 2024/7/25
 */
public interface EntityQuery {

    @SuppressWarnings("unchecked")
    default <T extends EntityQuery> T queryObject(Class<T> clazz) {
        if (isKindOf(clazz)) {
            return (T) this;
        }
        return null;
    }

    default <T extends EntityQuery> boolean isKindOf(Class<T> clazz) {
        return clazz.isInstance(this);
    }
}
