package com.t13max.persist.manager;

import com.t13max.common.manager.ManagerBase;
import com.t13max.persist.collection.XList;
import com.t13max.persist.collection.XMap;
import com.t13max.persist.collection.XSet;
import com.t13max.persist.data.IData;
import com.t13max.persist.modify.Option;
import com.t13max.persist.modify.Update;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: t13max
 * @since: 15:47 2024/8/7
 */
public class AutoSaveManager extends ManagerBase {

    private final static int MAX_ELEMENTS = 500;

    //存库线程
    private final ScheduledExecutorService saveDataExecutor = Executors.newSingleThreadScheduledExecutor();
    //变化数据映射表 用于存完通知回去
    private final Map<Class<? extends IData>, Map<Long, IData>> changeDataMap;
    //三个异步入库队列 insert和update可不可以合成一个?
    private final Map<Class<? extends IData>, LinkedBlockingQueue<? extends IData>> insertDataMap;
    private final Map<Class<? extends IData>, LinkedBlockingQueue<? extends IData>> updateDataMap;
    private final Map<Class<? extends IData>, LinkedBlockingQueue<? extends IData>> deleteDataMap;
    //标记 正在执行save
    private final AtomicBoolean running = new AtomicBoolean(false);

    private long lastActionTimestamp;

    public AutoSaveManager() {
        this.insertDataMap = new ConcurrentHashMap<>();
        this.updateDataMap = new ConcurrentHashMap<>();
        this.deleteDataMap = new ConcurrentHashMap<>();
        this.changeDataMap = new ConcurrentHashMap<>();
    }

    public static AutoSaveManager inst() {
        return inst(AutoSaveManager.class);
    }

    @Override
    protected void onShutdown() {
        saveAll();
    }

    /**
     * 保存所有
     *
     * @Author t13max
     * @Date 15:50 2024/8/7
     */
    private void saveAll() {
        persistence();
    }

    @Override
    public void init() {

        //异步存库线程 启动!
        saveDataExecutor.scheduleAtFixedRate(this::tickSave, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void tickSave() {
        persistence();
    }

    public LinkedBlockingQueue<IData> getQueue(Class<? extends IData> clazz, Map<Class<? extends IData>, LinkedBlockingQueue<? extends IData>> cache) {
        LinkedBlockingQueue<? extends IData> queue = cache.get(clazz);
        if (queue == null) {
            synchronized (this) {
                queue = cache.get(clazz);
                if (queue == null) {
                    queue = new LinkedBlockingQueue<>();
                    cache.put(clazz, queue);
                }
            }
        }
        return (LinkedBlockingQueue<IData>) queue;
    }

    /**
     * 批量保存数据
     *
     * @Author t13max
     * @Date 16:51 2024/8/7
     */
    public <T extends IData> void batchSave(Collection<T> dataList) {
        if (dataList.isEmpty()) {
            return;
        }
        for (T data : dataList) {
            save(data);
        }
    }

    /**
     * 保存数据
     * 改用高效反射?
     *
     * @Author t13max
     * @Date 16:18 2024/8/7
     */
    public void save(IData data) {
        Option state = Update.state(data);
        LinkedBlockingQueue<IData> queue;
        Class<? extends IData> clazz = data.getClass();
        if (state == Option.INSERT) {
            queue = getQueue(clazz, insertDataMap);
        } else if (state == Option.UPDATE) {
            queue = getQueue(clazz, updateDataMap);
        } else {
            return;
        }
        try {
            Constructor<? extends IData> constructor = data.getClass().getDeclaredConstructor();
            var newData = constructor.newInstance();
            for (Field field : data.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                setValue(data, field, newData);
            }
            queue.add(newData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除数据
     *
     * @Author t13max
     * @Date 16:20 2024/8/7
     */
    public void delete(IData data) {
        Class<? extends IData> clazz = data.getClass();
        LinkedBlockingQueue<IData> queue = getQueue(clazz, deleteDataMap);
        queue.add(data);
    }

    /**
     * 持久化!
     *
     * @Author t13max
     * @Date 16:17 2024/8/7
     */
    public boolean persistence() {

        long now = System.currentTimeMillis();

        if (!running.compareAndSet(false, true)) {
            return false;
        }
        lastActionTimestamp = now;

        try {

            insertDataMap.values().parallelStream().forEach(queue -> {
                try {
                    while (!queue.isEmpty()) {
                        List<IData> dataList = new LinkedList<>();
                        queue.drainTo(dataList, MAX_ELEMENTS);
                        String simpleName = dataList.get(0).getClass().getSimpleName();

                        DataManager.inst().saveList(dataList);
                        //TODO 失败重试

                        if (!queue.isEmpty()) {
                            TimeUnit.MILLISECONDS.sleep(5);
                        }
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
            updateDataMap.values().parallelStream().forEach(queue -> {
                try {

                    while (!queue.isEmpty()) {

                        List<IData> dataList = new LinkedList<>();
                        queue.drainTo(dataList, MAX_ELEMENTS);

                        DataManager.inst().saveList(dataList);
                        //TODO 失败重试

                        if (!queue.isEmpty()) {
                            TimeUnit.MILLISECONDS.sleep(5);
                        }
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
            deleteDataMap.values().parallelStream().forEach(queue -> {
                try {
                    while (!queue.isEmpty()) {
                        List<IData> dataList = new LinkedList<>();
                        queue.drainTo(dataList, MAX_ELEMENTS);

                        DataManager.inst().deleteList(dataList);
                        if (!queue.isEmpty()) {
                            TimeUnit.MILLISECONDS.sleep(5);
                        }
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        } catch (
                Throwable throwable) {
            throwable.printStackTrace();

        } finally {
            running.set(false);
        }
        return true;
    }

    /**
     * 给字段赋值 对X集合特殊处理
     *
     * @Author t13max
     * @Date 16:51 2024/8/7
     */
    private void setValue(IData data, Field field, IData newData) throws IllegalAccessException {
        Object value = field.get(data);
        if (Objects.nonNull(value)) {
            Class<?> czl = value.getClass();
            if (List.class.isAssignableFrom(czl)) {
                List<?> list = new XList<>(value);
                field.set(newData, list);
            } else if (Set.class.isAssignableFrom(czl)) {
                Set<?> sets = new XSet<>(value);
                field.set(newData, sets);
            } else if (Map.class.isAssignableFrom(czl)) {
                Map<?, ?> mps = new XMap<>(value);
                field.set(newData, mps);
            }
        }
        field.set(newData, value);
    }
}
