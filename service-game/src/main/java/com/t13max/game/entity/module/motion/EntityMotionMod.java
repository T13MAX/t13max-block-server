package com.t13max.game.entity.module.motion;

import com.t13max.game.consts.Const;
import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.EntityModule;
import com.t13max.game.entity.module.motion.attach.MoveAttachment;
import com.t13max.game.entity.module.motion.attach.MoveChase;
import com.t13max.game.pos.Position;
import com.t13max.game.util.TickTimer;
import com.t13max.game.world.module.WorldEntityMod;
import game.enums.MotionEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * 实体移动模块
 *
 * @author: t13max
 * @since: 17:03 2024/7/25
 */
public class EntityMotionMod extends EntityModule {

    @Getter
    @Setter
    private MoveAttachment moveAttachment = null;

    private final TickTimer stateTimer = new TickTimer(Const.STANDING_CHECK);

    //处理站立/移动状态转换aba 自增
    @Getter
    private int aba = 0;

    public EntityMotionMod(IEntity owner) {
        super(owner);
    }

    @Override
    protected void tick() {
        super.tick();
    }

    /**
     * 客户端移动三件套之移动
     *
     * @Author t13max
     * @Date 15:51 2024/12/9
     */
    public void tryMove(Position startPos, Position movePos, Position farthestPos, Position faceDir, MotionEnum motionEnum) {


    }

    /**
     * 客户端三件套之同步位置
     *
     * @Author t13max
     * @Date 15:55 2024/12/9
     */
    public void trySyncPos(Position syncPos) {

    }

    /**
     * 客户端三件套之停止移动
     *
     * @Author t13max
     * @Date 15:55 2024/12/9
     */
    public void tryStopPos(Position stopPos, Position faceDir) {

    }

    /**
     * 追击目标
     *
     * @Author t13max
     * @Date 17:10 2024/12/9
     */
    public void moveChase(long targetId, float minRadius, float maxDistance) {
        if (!owner.isMovable()) {
            return;
        }
        WorldEntityMod worldEntityMod = owner.getWorld().getWorldModules().getWorldModule(WorldEntityMod.class);
        IEntity target = worldEntityMod.getEntity(targetId);
        if (target == null) {
            return;
        }
        boolean isCreate = createMoveAttachment(MotionEnum.CHASE);
        if (isCreate) {
            if (this.moveAttachment instanceof MoveChase moveChase) {
                //加上目标的半径
                minRadius += target.getModelRadius();
                maxDistance += target.getModelRadius();
                if (moveChase.startChase(targetId, minRadius, maxDistance, 0)) {
                    moveChase.startup();
                }
            }
        }
    }

    public boolean createMoveAttachment(MotionEnum motionEnum) {

        if (moveAttachment != null) {
            moveAttachment.cleanup();
            this.moveAttachment = null;
        }
        switch (motionEnum) {
            default -> {

            }
        }
        return true;
    }

}

