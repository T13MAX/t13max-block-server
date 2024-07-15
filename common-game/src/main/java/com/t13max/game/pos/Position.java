package com.t13max.game.pos;

import lombok.Data;

import java.util.Objects;

/**
 * 位置
 *
 * @author: t13max
 * @since: 11:49 2024/7/15
 */
@Data
public class Position {

    private int x;

    private int y;

    private int z;

    /**
     * 空参构造
     *
     * @Author t13max
     * @Date 13:44 2024/7/15
     */
    public Position() {
    }

    /**
     * 纵坐标为0的构造方法
     *
     * @Author t13max
     * @Date 13:44 2024/7/15
     */
    public Position(int x, int z) {
        this.x = x;
        this.z = z;
    }

    /**
     * 三个做标的构造方法
     *
     * @Author t13max
     * @Date 13:44 2024/7/15
     */
    public Position(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * 重写equals和hashcode
     *
     * @Author t13max
     * @Date 13:45 2024/7/15
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y && z == position.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
