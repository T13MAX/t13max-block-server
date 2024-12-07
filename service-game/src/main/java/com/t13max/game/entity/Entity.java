package com.t13max.game.entity;

import com.t13max.game.pos.Position;
import com.t13max.game.world.World;
import com.t13max.game.world.chunk.Chunk;
import lombok.Getter;

/**
 * 实体 顶级父类
 *
 * @author: t13max
 * @since: 15:53 2024/7/25
 */
@Getter
public abstract class Entity implements IEntity {

    protected long id;

    //所在世界
    protected World world;

    //所在位置
    protected Position position;

    public Entity() {
    }

    public Entity(long id) {
        this.id = id;
    }

    /**
     * tick!
     *
     * @Author t13max
     * @Date 16:08 2024/7/25
     */
    @Override
    public void tick() {

    }

    /**
     * 实体进入世界
     *
     * @Author t13max
     * @Date 15:59 2024/12/6
     */
    @Override
    public void enterWorld(World world) {

        if (this.world != null) {
            //错误处理
            return;
        }

        enterWorldBefore();

        this.world = world;
        this.world.enterWorld(this);

        enterWorldAfter();
    }

    /**
     * 实体离开世界
     *
     * @Author t13max
     * @Date 16:00 2024/12/6
     */
    @Override
    public void leaveWorld(World world) {

        if (this.world == null || this.world != world) {
            return;
        }

        leaveWorldBefore();

        this.world.leaveWorld(this);
        this.world = null;

        leaveWorldAfter();
    }

    /**
     * 进入世界前
     *
     * @Author t13max
     * @Date 16:00 2024/12/6
     */
    protected void enterWorldBefore() {

    }

    /**
     * 进入世界后
     *
     * @Author t13max
     * @Date 16:00 2024/12/6
     */
    protected void enterWorldAfter() {

    }

    /**
     * 离开世界前
     *
     * @Author t13max
     * @Date 16:00 2024/12/6
     */
    protected void leaveWorldBefore() {

    }

    /**
     * 离开世界后
     *
     * @Author t13max
     * @Date 16:00 2024/12/6
     */
    protected void leaveWorldAfter() {

    }

    /**
     * 位置变化
     *
     * @Author t13max
     * @Date 16:06 2024/12/6
     */
    @Override
    public void changePosition(Position newPos) {
        Position oldPos = this.position;
        this.position = newPos;
        this.world.onObjectMoved(this, oldPos, newPos);
    }

    /**
     * 实体所在区块变化
     * 被区块模块调用
     *
     * @Author t13max
     * @Date 16:03 2024/12/6
     */
    @Override
    public void onChunkChanged(Chunk leaveChunk, Chunk enterChunk) {
        //获取新的格子在原先格子的方向
        int chunkDir = leaveChunk.where(enterChunk);
        //离开原视野单元格
        onChunkLeave(leaveChunk, chunkDir);
        //进入新的视野单元格
        onChunkEnter(enterChunk, chunkDir);
    }

    /**
     * 实体进入某区块
     * 同步消息等 区块的实体进入再上一层已经被调用
     *
     * @Author t13max
     * @Date 16:21 2024/12/6
     */
    private void onChunkEnter(Chunk enterChunk, int chunkDir) {

        //同步消息给相关实体
    }

    /**
     * 实体离开某区块
     *
     * @Author t13max
     * @Date 16:21 2024/12/6
     */
    private void onChunkLeave(Chunk leaveChunk, int chunkDir) {

        //同步消息给相关实体
    }

}
