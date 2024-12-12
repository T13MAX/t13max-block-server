package com.t13max.game.entity.module.aura;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 光环清除器
 *
 * @Author t13max
 * @Date 17:37 2024/12/11
 */
@Getter
public class AuraCleaner {
    //是否异步清除
    private boolean asyncCleanup;
    //异步清除模式
    private int asyncCleanupMode;
    //异步清除标记位
    private int asyncCleanupFlags;
    //异步清除AuraApp
    private final List<AuraAsyncCleanup> asyncCleaners = new ArrayList<>();

    public void cleanupAsync(int mode, int flags) {
        this.asyncCleanup = true;
        this.asyncCleanupMode = mode;
        this.asyncCleanupFlags = flags;
    }

    public void cleanupAsync(int mode, AuraBuff app) {
        AuraAsyncCleanup context = new AuraAsyncCleanup(mode, app.getTargetId());
        this.asyncCleaners.add(context);
        app.includeFlags(AuraConst.AURA_APP_FLAGS_ASYNC_CLEAN);
    }

    boolean update(Aura aura) {
        // 异步清除app
        if (!asyncCleaners.isEmpty()) {
            for (AuraAsyncCleanup cleaner : asyncCleaners) {
                aura.cleanupBuff(cleaner.mode, cleaner.targetId);
            }

            asyncCleaners.clear();
        }

        if (aura.isSingleBuff() && aura.isEmpty()) {
            cleanupAsync(AuraConst.AURA_REMOVE_BY_INVALID, 0);
        }

        // 异步清除aura
        if (this.asyncCleanup) {
            this.asyncCleanup = false;
            aura.cleanup(this.asyncCleanupMode, this.asyncCleanupFlags);
            this.asyncCleanupFlags = 0;

            return true;
        }

        return false;
    }

    private static class AuraAsyncCleanup {
        //清除类型
        public int mode;
        //目标ID
        public long targetId;

        public AuraAsyncCleanup(int mode, long targetId) {
            this.mode = mode;
            this.targetId = targetId;
        }
    }
}
