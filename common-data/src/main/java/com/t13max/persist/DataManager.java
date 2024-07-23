package com.t13max.persist;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.t13max.common.manager.ManagerBase;
import dev.morphia.Datastore;
import dev.morphia.Morphia;

/**
 * 数据管理类
 *
 * @Author: t13max
 * @Since: 22:16 2024/7/14
 */
public class DataManager extends ManagerBase {

    private MongoClient mongoClient;

    private Datastore datastore;

    public static DataManager inst() {
        return inst(DataManager.class);
    }

    @Override
    protected void onShutdown() {
        super.onShutdown();
    }

    @Override
    public void init() {

        mongoClient = MongoClients.create("mongodb://localhost:27017");

        // 创建Datastore并连接到数据库
        datastore = Morphia.createDatastore(mongoClient, "my_database");

    }
}
