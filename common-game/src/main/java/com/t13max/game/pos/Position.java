package com.t13max.game.pos;

import com.t13max.game.consts.Const;
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

    private short x;

    private short y;

    private short z;

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
        this.x = (short) x;
        this.z = (short) z;
    }

    /**
     * 三个做标的构造方法
     *
     * @Author t13max
     * @Date 13:44 2024/7/15
     */
    public Position(int x, int y, int z) {
        this.x = (short) x;
        this.y = (short) y;
        this.z = (short) z;
    }

    public short getChunkX() {
        return (short)(x / Const.CHUNK_LENGTH);
    }

    public short getChunkZ() {
        return (short)(z / Const.CHUNK_LENGTH);
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
