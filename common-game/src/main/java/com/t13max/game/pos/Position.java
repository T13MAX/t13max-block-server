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

    //未知
    public static final int DIR_UNKNOWN = -1;
    //就是自己
    public static final int DIR_SELF = 0;
    //上
    public static final int DIR_UP = 1;
    //右上
    public static final int DIR_RIGHT_UP = 2;
    //右
    public static final int DIR_RIGHT = 3;
    //右下
    public static final int DIR_RIGHT_DOWN = 4;
    //下
    public static final int DIR_DOWN = 5;
    //左下
    public static final int DIR_LEFT_DOWN = 6;
    //左
    public static final int DIR_LEFT = 7;
    //左上
    public static final int DIR_LEFT_UP = 8;
    //全部方向的数目
    public static final int DIR_NUM = 8;

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
        return (short) (x / Const.CHUNK_LENGTH);
    }

    public short getChunkZ() {
        return (short) (z / Const.CHUNK_LENGTH);
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

    /**
     * 两点之间的距离，忽视高度
     *
     * @Author t13max
     * @Date 15:28 2024/12/10
     */
    public double distance(Position pos) {
        double t1x = this.x;
        double t1z = this.z;
        double t2x = pos.x;
        double t2z = pos.z;

        return Math.sqrt(Math.pow((t1x - t2x), 2) + Math.pow((t1z - t2z), 2));
    }

    /**
     * 两点之间的距离,带高度
     *
     * @Author t13max
     * @Date 15:28 2024/12/10
     */
    public double distance3D(Position pos) {
        double t1x = this.x;
        double t1y = this.y;
        double t1z = this.z;
        double t2x = pos.x;
        double t2y = pos.y;
        double t2z = pos.z;

        return Math.sqrt(Math.pow((t1x - t2x), 2) + Math.pow((t1y - t2y), 2) + Math.pow((t1z - t2z), 2));
    }
}
