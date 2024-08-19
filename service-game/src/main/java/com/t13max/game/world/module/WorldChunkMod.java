package com.t13max.game.world.module;

import com.t13max.game.consts.Const;
import com.t13max.game.entity.IEntity;
import com.t13max.game.pos.Position;
import com.t13max.game.util.Log;
import com.t13max.game.util.PosUtil;
import com.t13max.game.world.World;
import com.t13max.game.world.chunk.Chunk;
import com.t13max.game.world.chunk.ChunkGenerator;
import com.t13max.game.world.chunk.FlatChunkGenerator;
import com.t13max.persist.manager.DataManager;
import com.t13max.persist.data.chunk.ChunkData;
import com.t13max.util.TimeUtil;
import game.enums.BlockEnum;
import lombok.Getter;

import java.util.*;
import java.util.stream.Stream;

/**
 * 区块管理模块
 * 存放区块数据对象 区块加载卸载
 *
 * @author: t13max
 * @since: 16:20 2024/7/25
 */
@Getter
public class WorldChunkMod extends WorldModule {

    //世界区块生成器
    private final ChunkGenerator chunkGenerator;

    //区块map
    private final Map<Integer, Chunk> chunkMap = new HashMap<>();

    public WorldChunkMod(World world) {
        super(world);
        //后续增加读取配置 创建各种ChunkGenerator
        this.chunkGenerator = new FlatChunkGenerator(Arrays.asList(new FlatChunkGenerator.FlatPair(BlockEnum.BED_BLOCK, 1), new FlatChunkGenerator.FlatPair(BlockEnum.DIRTY_BLOCK, 3)));
    }

    public Chunk loadChunk(int chunkId) {
        ChunkData chunkData = DataManager.inst().findById(owner.getName() + Const.CHUNK_NAME, chunkId);
        if (chunkData == null) {
            chunkData = createChunk(chunkId);
        }
        Chunk chunk = new Chunk(chunkData);
        this.chunkMap.put(chunkId, chunk);
        return chunk;
    }

    /**
     * 新建一个区块
     * 这种操作是不是应该异步来做呢?
     *
     * @Author t13max
     * @Date 16:04 2024/8/19
     */
    private ChunkData createChunk(int chunkId) {
        return chunkGenerator.generateChunkData(chunkId);
    }

    public Chunk getChunk(int chunkId) {
        return chunkMap.get(chunkId);
    }

    public Chunk getChunk(Position position) {
        return chunkMap.get(PosUtil.getChunkId(position));
    }

    @Override
    public void enterWorld(IEntity entity) {

        //遍历进入
        List<Chunk> chunkList = get25Chunk(entity.getPosition());
        chunkList.forEach(chunk -> chunk.enterWorld(entity));
    }

    @Override
    public void leaveWorld(IEntity entity) {
        //遍历离开
        List<Chunk> chunkList = get25Chunk(entity.getPosition());
        chunkList.forEach(chunk -> chunk.leaveWorld(entity));
    }

    @Override
    public void onEntityMoved(IEntity entity, Position oldPos, Position newPos) {

        if (!checkCross(oldPos, newPos)) {

            return;
        }
        Chunk oldChunk = getChunk(oldPos);
        if (oldChunk == null) {
            //异常处理
            return;
        }
        Chunk newChunk = getChunk(newPos);
        if (newChunk == null) {
            //异常处理
            return;
        }

        //跨越
        if (oldChunk != newChunk) {
            onEntityCross(entity, oldChunk, newChunk);
        }
    }

    /**
     * 校验是否跨越区块
     *
     * @Author t13max
     * @Date 16:10 2024/8/14
     */
    private boolean checkCross(Position oldPos, Position newPos) {
        return oldPos.getChunkX() != newPos.getChunkX() && oldPos.getChunkZ() != newPos.getChunkZ();
    }

    /**
     * 实体跨越区块
     *
     * @Author t13max
     * @Date 16:17 2024/8/19
     */
    private void onEntityCross(IEntity entity, Chunk oldChunk, Chunk newChunk) {
        //从老的里面删除
        oldChunk.leaveWorld(entity);
        //加到新的里面
        newChunk.enterWorld(entity);
    }

    /**
     * 获取5x5范围的区块
     *
     * @Author t13max
     * @Date 16:17 2024/8/19
     */
    public List<Chunk> get25Chunk(Position position) {
        List<Integer> posList = get25Pos(position);
        List<Chunk> result = new LinkedList<>();
        for (Integer pos : posList) {

            Chunk chunk = this.chunkMap.get(pos);
            if (chunk == null) {
                //理论上 他一定存在 因为会加载7x7的
                Log.world.error("区块维未加载, position={}", position);
                continue;
            }
            result.add(chunk);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * 获取5x5的区块id
     *
     * @Author t13max
     * @Date 16:18 2024/8/19
     */
    public List<Integer> get25Pos(Position position) {
        return getNPos(5, position);
    }

    /**
     * 获取NxN的区块id
     *
     * @Author t13max
     * @Date 16:18 2024/8/19
     */
    public List<Integer> getNPos(int n, Position position) {
        short x = position.getChunkX();
        short z = position.getChunkZ();
        List<Integer> result = new LinkedList<>();
        for (byte i = 0; i < n; i++) {
            for (byte j = 0; j < n; j++) {
                //if (i == 0 || j == 0) continue;
                result.add(PosUtil.getChunkId(x + i, z + j));
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public void tick() {
        int nowSeconds = TimeUtil.nowSeconds();

        //根据seconds 加载等级 去 tick

        List<Integer> unloadIds = new LinkedList<>();
        for (Chunk chunk : this.chunkMap.values()) {
            //需要卸载
            if (chunk.checkUnload()) {
                unloadIds.add(chunk.getChunkId());
            }
        }
    }
}
