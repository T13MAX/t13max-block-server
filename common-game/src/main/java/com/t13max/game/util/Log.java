package com.t13max.game.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author: t13max
 * @since: 18:49 2024/7/23
 */
public class Log {
    public static Logger game = LogManager.getLogger("GAME");
    public static Logger template = LogManager.getLogger("TEMPLATE");
    public static Logger data = LogManager.getLogger("DATA");
    public static Logger world = LogManager.getLogger("WORLD");
}
