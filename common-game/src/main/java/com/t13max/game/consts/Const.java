package com.t13max.game.consts;

/**
 * 常量
 *
 * @Author t13max
 * @Since 20:45 2024/7/14
 */
public interface Const {
    //配置文件名
    String CONFIG_NAME = "config.yaml";
    //tick间隔
    int TICK_INTERVAL = 50;
    //第一次执行定时任务延迟
    int SCHEDULE_INIT_DELAY = 1000;
    //区块的长度
    int CHUNK_LENGTH = 16;
    //区块字符串
    String CHUNK_NAME = "chunk";
    //区块数量
    short CHUNK_NUM = 1024;
    //世界边长 1024*16==16384;
    short MAX_LENGTH = CHUNK_NUM * CHUNK_LENGTH;
}
