package com.t13max.game.server;

import lombok.experimental.UtilityClass;

/**
 * Block服务器工厂类
 * 根据配置创建对应的Server并初始化
 *
 * @Author t13max
 * @Since 20:41 2024/7/14
 */
@UtilityClass
public class BlockServerFactory {

    public IBlockServer createBlockServer() {
        return new DedicatedServer();
    }
}
