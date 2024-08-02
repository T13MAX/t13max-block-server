package com.t13max.persist;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.connection.ConnectionPoolSettings;
import com.t13max.common.manager.ManagerBase;
import com.t13max.game.config.BlockConfig;
import com.t13max.game.config.DataConfig;
import com.t13max.persist.data.IPersistData;
import dev.morphia.Datastore;
import dev.morphia.DeleteOptions;
import dev.morphia.InsertOneOptions;
import dev.morphia.Morphia;
import dev.morphia.query.filters.Filters;
import org.bson.Document;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 数据管理类
 * <p>
 * 存库队列异步保存 world的线程定期触发把变化的数据扔到存库队列 存库队列慢慢保存
 * 保存完成后 通知world存完了 如果是可以卸载的数据 就可以卸载了
 * 卸载需要没有对象加载它持续一定时间才会触发
 *
 * @Author: t13max
 * @Since: 22:16 2024/7/14
 */
public class DataManager extends ManagerBase {

    private MongoClient mongoClient;

    private Datastore datastore;

    private final Queue<IPersistData> persistQueue = new LinkedBlockingQueue<>();

    public static DataManager inst() {
        return inst(DataManager.class);
    }

    @Override
    protected void onShutdown() {
        mongoClient.close();
    }

    @Override
    public void init() {

        DataConfig dataConfig = BlockConfig.INSTANCE.getData();

        // 配置连接池设置
        ConnectionPoolSettings connectionPoolSettings = ConnectionPoolSettings.builder()
                .maxSize(dataConfig.getMaxSize()) // 最大连接数
                .minSize(dataConfig.getMinSize()) // 最小连接数
                .maxWaitTime(dataConfig.getWaitTime(), TimeUnit.MILLISECONDS) // 获取连接的最大等待时间
                .maxConnectionIdleTime(dataConfig.getMaxIdle(), TimeUnit.SECONDS) // 连接的最大空闲时间
                .build();

        // 配置MongoClient设置
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(dataConfig.getUrl()))
                .applyToConnectionPoolSettings(builder -> builder.applySettings(connectionPoolSettings))
                .build();

        // 创建MongoClient
        mongoClient = MongoClients.create(settings);

        // 创建Datastore并连接到数据库
        datastore = Morphia.createDatastore(mongoClient, dataConfig.getDatabase());
    }

    /**
     * 根据id查找数据
     *
     * @Author t13max
     * @Date 15:51 2024/8/2
     */
    public <T extends IPersistData> T findById(Class<T> clazz, long id) {
        return datastore.find(clazz, new Document("_id", id)).first();
    }

    public <T extends IPersistData> T findById(String collectionName, long id) {
        return (T) datastore.find(collectionName).filter(Filters.eq("_id", id)).first();
    }

    /**
     * 条件查询
     *
     * @Author t13max
     * @Date 15:51 2024/8/2
     */
    public <T extends IPersistData> T findById(Class<T> clazz, Document filter) {
        return datastore.find(clazz, filter).first();
    }

    /**
     * 删除指定数据
     *
     * @Author t13max
     * @Date 15:51 2024/8/2
     */
    public <T extends IPersistData> boolean delete(T data) {
        DeleteResult deleteResult = datastore.delete(data);
        return deleteResult.getDeletedCount() > 0;
    }

    public <T extends IPersistData> boolean delete(String collectionName, T data) {
        DeleteResult deleteResult = datastore.delete(data, new DeleteOptions().collection(collectionName));
        return deleteResult.getDeletedCount() > 0;
    }

    /**
     * 保存
     *
     * @Author t13max
     * @Date 15:53 2024/8/2
     */
    public <T extends IPersistData> T save(T data) {
        return datastore.save(data);
    }

    /**
     * 保存到指定表
     *
     * @Author t13max
     * @Date 16:19 2024/8/2
     */
    public <T extends IPersistData> T save(T data, String collectionName) {
        return datastore.save(data, new InsertOneOptions().collection(collectionName));
    }


}
