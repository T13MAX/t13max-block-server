package com.t13max.game.entity.module.skill;


import lombok.Getter;

/**
 * @Author t13max
 * @Date 16:53 2024/12/12
 */
@Getter
public class DynamicBlockEffect {

    private final long expiredTime;

    public DynamicBlockEffect(long expiredTime) {
        this.expiredTime = expiredTime;
    }
}
