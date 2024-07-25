package com.t13max.game.util;

import lombok.experimental.UtilityClass;

/**
 * 坐标工具类
 *
 * @author: t13max
 * @since: 18:06 2024/7/25
 */
@UtilityClass
public class PosUtil {

    private int mask_8 = 0xff;
    private int mask_16 = 0xffff;
    private int mask_32 = 0xffffffff;


    public int getX(short pos) {
        return pos & mask_8;
    }

    public int getZ(short pos) {
        return pos >> 8 & mask_8;
    }

    public int getY(short pos) {
        return pos >>> 16;
    }

    public int getWorldX(long chunkId, short pos) {
        return getWorldX(chunkId) + getX(pos);
    }

    public int getWorldZ(long chunkId, short pos) {
        return getWorldZ(chunkId) + getZ(pos);
    }

    public short getPos(int x, int y, int z) {
        return (short) (y << 16 + z << 8 + x);
    }

    public int getWorldX(long chunkId) {
        return (int) (chunkId & mask_32);
    }

    public int getWorldZ(long chunkId) {
        return (int) (chunkId >> 32 & mask_32);
    }

    public long getChunkId(int x, int z) {
        return ((long) z) << 32 + x;
    }
}
