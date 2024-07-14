package com.t13max.game;

import com.t13max.game.config.BlockConfig;
import com.t13max.game.server.BlockServerFactory;
import com.t13max.game.server.IBlockServer;

/**
 * 服务器主类
 *
 * @author: t13max
 * @since: 11:32 2024/7/12
 */
public class GameApplication {

    public static void main(String[] args) {

        //先加载配置
        BlockConfig blockConfig = BlockConfig.INSTANCE;

        //创建服务器
        IBlockServer blockServer = BlockServerFactory.createBlockServer();

        //block 启动!
        blockServer.runServer();
    }
}
