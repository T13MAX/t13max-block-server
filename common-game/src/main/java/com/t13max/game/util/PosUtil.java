package com.t13max.game.util;

import com.t13max.game.pos.Position;
import lombok.experimental.UtilityClass;

/**
 * 坐标工具类
 *
 * @author: t13max
 * @since: 18:06 2024/7/25
 */
@UtilityClass
public class PosUtil {

    private final int mask_8 = 0xff;
    private final int mask_16 = 0xffff;
    private final int mask_32 = 0xffffffff;


    public int getX(short pos) {
        return pos & mask_8;
    }

    public int getZ(short pos) {
        return pos >> 8 & mask_8;
    }

    public int getY(short pos) {
        return pos >>> 16;
    }

    public int getChunkX(int chunkId, short pos) {
        return getChunkX(chunkId) * 16 + getX(pos);
    }

    public int getChunkZ(int chunkId, short pos) {
        return getChunkZ(chunkId) * 16 + getZ(pos);
    }

    public short getPos(int x, int y, int z) {
        return (short) (y << 16 + z << 8 + x);
    }

    public short getPos(Position position) {
        return (short) (position.getY() << 16 + position.getZ() << 8 + position.getX());
    }

    /**
     * 获取区块的x坐标
     *
     * @Author t13max
     * @Date 15:16 2024/8/14
     */
    public short getChunkX(int chunkId) {
        return (short) (chunkId & mask_16);
    }

    public short getChunkZ(int chunkId) {
        return (short) (chunkId >> 16 & mask_16);
    }

    public int getChunkId(short x, short z) {
        return ((int) z) << 16 + x;
    }

    public int getChunkId(int x, int z) {
        return getChunkId((short) x, (short) z);
    }

    public int getChunkId(Position position) {
        return getChunkId(position.getChunkX(), position.getChunkZ());
    }
}
