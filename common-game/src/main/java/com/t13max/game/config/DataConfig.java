package com.t13max.game.config;

import lombok.Data;

/**
 * 数据配置
 *
 * @author: t13max
 * @since: 16:05 2024/8/2
 */
@Data
public class DataConfig {

    private String url = "mongodb://localhost:27017";
    private int maxSize = 10;
    private int minSize = 5;
    private int waitTime = 1000;
    private int maxIdle = 60;
    private String database = "block_server";
}
