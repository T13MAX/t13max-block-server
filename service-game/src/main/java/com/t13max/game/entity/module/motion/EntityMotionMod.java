package com.t13max.game.entity.module.motion;

import com.google.protobuf.Empty;
import com.t13max.game.consts.Const;
import com.t13max.game.consts.UnitBits;
import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.module.EntityModule;
import com.t13max.game.entity.module.motion.attach.*;
import com.t13max.game.entity.module.reaction.EntityReactionMod;
import com.t13max.game.entity.module.reaction.ReactionParam;
import com.t13max.game.entity.module.reaction.ReactionTriggerEnum;
import com.t13max.game.pos.Position;
import com.t13max.game.pos.Vector3D;
import com.t13max.game.util.Log;
import com.t13max.game.util.TickTimer;
import com.t13max.game.world.module.WorldEntityMod;
import com.t13max.util.TimeUtil;
import game.enums.EntityEnum;
import game.enums.MotionEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private long lastSyncPosTime;
    @Getter
    private int motionFlags;
    private final TickTimer stateTimer = new TickTimer(Const.STANDING_CHECK);

    //版本号 处理站立/移动状态转换aba 自增
    @Getter
    private int version = 0;
    private int preVersion = 0;

    public EntityMotionMod(IEntity owner) {
        super(owner);
    }

    @Override
    protected int tick(long now) {
        super.tick(now);

        if (moveAttachment != null && moveAttachment.isStarted()) {
            int result = moveAttachment.tick(now);
            if (moveAttachment.isFinished() || result != 0) {
                this.onMotionEnded(!moveAttachment.isIncludeFlags(MotionConst.MOTION_IGNORE_MSG));
                moveAttachment.cleanup();
                moveAttachment = null;
            }
        }

        if (EntityEnum.PLAYER == owner.getEntityData().getEntityEnum()) {
            if (owner.getBit(UnitBits.MOVING)) {
                // 移动触发器
                if (stateTimer.isPeriod(now)) {
                    this.owner.getEntityModules().getEntityModule(EntityReactionMod.class)
                            .trigger(ReactionTriggerEnum.MOVING_ONE_PERIOD, new ReactionParam(now));
                }
            } else {
                // 保持站立触发器
                if (stateTimer.isPeriod(now)) {
                    this.owner.getEntityModules().getEntityModule(EntityReactionMod.class)
                            .trigger(ReactionTriggerEnum.STANDING_ONE_PERIOD, new ReactionParam(now));
                }
            }
        }
        return 0;
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

    /**
     * 路点移动
     *
     * @Author t13max
     * @Date 16:23 2024/12/10
     */
    public void movePosition(List<Vector3D> targets, int flags) {
        boolean isCreate = createMoveAttachment(MotionEnum.POSITION);
        if (isCreate) {
            ((MovePosition) moveAttachment).initial(targets, flags);
            moveAttachment.startup();
        }
    }

    public void movePosition(List<Vector3D> targets, int flags, int speed) {
        boolean isCreate = createMoveAttachment(MotionEnum.POSITION);
        if (isCreate) {
            ((MovePosition) moveAttachment).initial(targets, flags);
            ((MovePosition) moveAttachment).setSpeed(speed);
            moveAttachment.startup();
        }
    }

    /**
     * 巡逻路点移动
     *
     * @Author t13max
     * @Date 16:24 2024/12/10
     */
    public void movePatrol(int wayPointId, int delay, int flags) {
        boolean isCreate = createMoveAttachment(MotionEnum.PATROL);
        if (isCreate) {
            MovePatrol movePatrol = ((MovePatrol) moveAttachment);
            if (movePatrol.doPatrol(wayPointId, delay, flags)) {
                movePatrol.startup();
            }
        }
    }

    /**
     * 逃避目标
     * 逃避目标移动行为 会规避警戒范围内的目标
     *
     * @Author t13max
     * @Date 16:24 2024/12/10
     */
    public void moveEvade(IEntity baseTarget, float evadeDist, int auraSn, Function<IEntity, IEntity> alertFunc) {
        if (!owner.isMovable()) {
            return;
        }
        boolean isCreate = createMoveAttachment(MotionEnum.EVADE);
        if (isCreate) {
            MoveEvade movementChase = (MoveEvade) moveAttachment;

            movementChase.moveEvade(baseTarget, evadeDist, auraSn, alertFunc, 0);
        }
    }


    /**
     * 跟随目标
     *
     * @Author t13max
     * @Date 16:25 2024/12/10
     */
    public void moveFollow(long targetId, float distance) {
        moveFollow(targetId, distance, null, true);
    }

    /**
     * 跟随目标
     *
     * @Author t13max
     * @Date 16:25 2024/12/10
     */
    public void moveFollow(long targetId, float distance, Integer angle, boolean syncSpeed) {
        if (!owner.isMovable()) {
            return;
        }

        boolean isCreate = createMoveAttachment(MotionEnum.FOLLOW);
        if (isCreate) {
            MoveFollow movementFollow = ((MoveFollow) moveAttachment);
            if (movementFollow.follow(targetId, distance, syncSpeed, angle, 0)) {
                if (movementFollow.startup() < 0) {
                    stopMotion(false);
                }
            }
        }
    }

    /**
     * 闲逛移动
     *
     * @Author t13max
     * @Date 16:25 2024/12/10
     */
    public void moveWander(Vector3D center, float radius, int interval) {
        boolean isCreate = createMoveAttachment(MotionEnum.WANDER);
        if (isCreate) {
            MoveWander moveWander = ((MoveWander) moveAttachment);
            moveWander.doWander(center, radius, interval);
            moveWander.startup();
        }
    }

    /**
     * 跳跃移动
     *
     * @Author t13max
     * @Date 16:26 2024/12/10
     */
    public void moveJump(List<Vector3D> targets, int flags, long time) {
        boolean isCreate = createMoveAttachment(MotionEnum.JUMP);
        if (isCreate) {
            ((MoveJump) moveAttachment).initial(targets, flags, time);
            moveAttachment.startup();
        }
    }

    /**
     * 方向移动
     *
     * @param startPos
     * @param moveDir
     * @param farthestPos
     * @param flags
     */
    public void moveDirection(Vector3D startPos, Vector3D moveDir, Vector3D faceDir, Vector3D farthestPos, MotionEnum motionEnum, int flags) {
        boolean isCreate = createMoveAttachment(MotionEnum.DIRECTION);
        if (isCreate) {
            ((MoveDirection) moveAttachment).initial(startPos, moveDir, faceDir, farthestPos, motionEnum, flags);
            moveAttachment.startup();
            onMotionBegan(!moveAttachment.isIncludeFlags(MotionConst.MOTION_IGNORE_MSG));
        }
    }

    private boolean createMoveAttachment(MotionEnum motionEnum) {

        if (moveAttachment != null) {
            moveAttachment.cleanup();
            this.moveAttachment = null;
        }
        switch (motionEnum) {

            case POSITION -> {
                moveAttachment = new MovePosition(owner);
            }
            case CHASE -> {
                moveAttachment = new MoveChase(owner);
            }
            case FLY -> {
                moveAttachment = new MoveFly(owner);
            }
            case FOLLOW -> {
                moveAttachment = new MoveFollow(owner);
            }
            case JUMP -> {
                moveAttachment = new MoveJump(owner);
            }
            case DIRECTION -> {
                moveAttachment = new MoveDirection(owner);
            }
            case WANDER -> {
                moveAttachment = new MoveWander(owner);
            }
            case SPREAD -> {
                //moveAttachment = new MoveS(owner);
            }
            case EVADE -> {
                moveAttachment = new MoveEvade(owner);
            }
            case PATROL -> {
                moveAttachment = new MovePatrol(owner);
            }
            default -> {

            }
        }

        return true;
    }

    void restartTimer() {
        this.stateTimer.reStart();
        this.preVersion = this.version;
        this.version = ++this.version % 1000;
    }

    public int stopMotion(boolean sendMsg) {
        if (moveAttachment != null) {
            this.onMotionEnded(sendMsg);
            moveAttachment.cleanup();
            moveAttachment = null;
        }
        return 0;
    }

    public int onMotionBegan(boolean sendMsg) {
        if (moveAttachment == null) {
            Log.world.error("moveAttachment is null, id:{}", owner.getId());
            return -1;
        }

        if (setMotionBit(UnitBits.MOVING) == 0) {
            this.restartTimer();
        }

        MotionInfo motionInfo = moveAttachment.getMotionInfo();
        owner.changePosition(motionInfo.position);
        owner.changeDirection(motionInfo.direction);
        lastSyncPosTime = TimeUtil.nowMills();

        if (sendMsg) {
            MotionInfo info = moveAttachment.getMotionInfo();
            if (info == null) {
                Log.world.error("MotionInfo is null, id:{}", owner.getId());
                return -1;
            }

            //发送消息
            owner.sendMsgToView(Empty.getDefaultInstance(), 0);
        }

        return 0;
    }

    public int onMotionEnded(boolean sendMsg) {
        if (cleanMotionBit(UnitBits.MOVING) != 0) {
            return -1;
        }

        if (sendMsg) {
            //发送消息
        }

        this.restartTimer();
        return 0;
    }

    /**
     * 瞬移
     *
     * @Author t13max
     * @Date 16:34 2024/12/10
     */
    public void changePosition(Vector3D pos) {
        stopMotion(false);

        owner.changePosition(pos);

        //发送消息
        //SceneMsgHelper.sendChangePositionToView(owner);
    }

    /**
     * 改变朝向
     *
     * @Author t13max
     * @Date 16:35 2024/12/10
     */
    public void changeDirection(Vector3D dir) {
        owner.changeDirection(dir);
        //发送消息
        //SceneMsgHelper.sendChangeDirectionToView(owner, 0L);
    }

    /**
     * 客户端同步的朝向
     *
     * @Author t13max
     * @Date 16:36 2024/12/10
     */
    public void syncDirection(Vector3D dir) {
        owner.changeDirection(dir);
        //发送消息
        //SceneMsgHelper.sendChangeDirectionToView(owner, owner.getId());
    }

    /**
     * 获取移动状态信息
     *
     * @Author t13max
     * @Date 16:35 2024/12/10
     */
    public MotionInfo getMotionInfo() {
        if (moveAttachment != null) {
            return moveAttachment.getMotionInfo();
        }

        return null;
    }

    /**
     * 移动模式已开始
     *
     * @Author t13max
     * @Date 16:35 2024/12/10
     */
    public boolean isMotionStarted() {
        return moveAttachment != null && moveAttachment.isStarted();
    }

    /**
     * 移动模式已结束
     *
     * @Author t13max
     * @Date 16:35 2024/12/10
     */
    public boolean isMotionFinished() {
        return moveAttachment == null || moveAttachment.isFinished();
    }

    /**
     * 移动中
     *
     * @Author t13max
     * @Date 16:35 2024/12/10
     */
    public boolean isMoving() {
        return owner.getBit(UnitBits.MOVING);
    }

    /**
     * 是否追敌中
     *
     * @Author t13max
     * @Date 16:35 2024/12/10
     */
    public boolean isChasing() {
        return isMoving() && moveAttachment instanceof MoveChase;
    }

    /**
     * 是否逃避目标中
     *
     * @Author t13max
     * @Date 16:36 2024/12/10
     */
    public boolean isEvade() {
        return isMoving() && moveAttachment instanceof MoveEvade;
    }

    /**
     * 校验速度是否正常
     *
     * @Author t13max
     * @Date 16:36 2024/12/10
     */
    public boolean validateMoveSpeed(Vector3D position) {
        long time = Math.max(0, TimeUtil.nowMills() - lastSyncPosTime);
        double maxDistance = owner.getSpeed() / 1000 * time + getIgnoreDist();
        double dis = owner.getPosition().approximateDistanceIgnoreY(position);
        return dis < maxDistance;
    }

    /**
     * 刷新移动标记位
     *
     * @Author t13max
     * @Date 16:37 2024/12/10
     */
    public void refreshMotionFlags() {
        this.motionFlags = owner.getWorld().getWorldModules().getDetourMod().getMotionFlags();
        /*if (!owner.isKindOf(SceneRole.class)) {
            this.motionFlags = motionFlags & ~PathFinding.SAMPLE_POLYFLAGS_SWIM;
        }*/
    }

    public int setMotionBit(int motionBit) {
        if (owner.getBit(motionBit)) {
            return -1;
        }

        owner.setBit(motionBit, true);

        return 0;
    }

    public int cleanMotionBit(int motionBit) {
        if (!owner.getBit(motionBit)) {
            return -1;
        }

        owner.setBit(motionBit, false);

        return 0;
    }

    public int getIgnoreDist() {
        // 可容忍前后端误差
        return 10;
    }

    /**
     * 沿着与其他对象之间的直线挪动，设定离其他对方的距离
     * @Author t13max
     * @Date 16:39 2024/12/10
     */
    public void moveTowards(IEntity other, double distance) {
        stopMotion(false);

        owner.moveTowards(other, distance);

        //发送消息
        //SceneMsgHelper.sendChangePositionToView(owner);
    }

}

