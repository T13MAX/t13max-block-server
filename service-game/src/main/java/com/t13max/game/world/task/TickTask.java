package com.t13max.game.world.task;

import com.t13max.game.world.BlockWorld;

/**
 * tick任务
 *
 * @author: t13max
 * @since: 11:29 2024/7/15
 */
public class TickTask implements Runnable {

    private BlockWorld blockWorld;

    public TickTask(BlockWorld blockWorld) {
        this.blockWorld = blockWorld;
    }

    @Override
    public void run() {
        blockWorld.tick();
    }
}
