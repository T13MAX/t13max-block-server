package com.t13max.game.world.task;

import com.t13max.game.world.World;

/**
 * tick任务
 *
 * @author: t13max
 * @since: 11:29 2024/7/15
 */
public class TickTask implements Runnable {

    private final World world;

    public TickTask(World world) {
        this.world = world;
    }

    @Override
    public void run() {
        world.tick();
    }
}
