package com.t13max.game;

import com.t13max.common.manager.ManagerBase;
import com.t13max.game.config.BlockConfig;
import com.t13max.game.server.BlockServerFactory;
import com.t13max.game.server.IBlockServer;
import com.t13max.persist.modify.DBDataGen;

/**
 * 服务器主类
 *
 * @author: t13max
 * @since: 11:32 2024/7/12
 */
public class GameApplication {

    public static void main(String[] args) {

        //修改data类字节码
        DBDataGen.init();

        //加载配置
        BlockConfig blockConfig = BlockConfig.INSTANCE;

        //初始化所有Manager
        ManagerBase.initAllManagers();

        //添加停服钩子
        addShutdownHook(ManagerBase::shutdown);

        //创建服务器
        IBlockServer blockServer = BlockServerFactory.createBlockServer();

        //block 启动!
        blockServer.runServer();
    }

    /**
     * 停服钩子
     *
     * @Author t13max
     * @Date 10:36 2024/7/15
     */
    public static void addShutdownHook(Runnable runnable) {
        Runtime.getRuntime().addShutdownHook(new Thread(runnable));
    }
}
