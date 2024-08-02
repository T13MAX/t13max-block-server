package com.t13max.persist.collection;

import com.t13max.persist.modify.Update;
import dev.morphia.annotations.Transient;
import lombok.Getter;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class XMap<K, V> implements Map<K, V>, Serializable {

    private Map<K, V> map;
    @Getter
    @Transient
    private Update update;

    public XMap(Update update) {
        this.map = new HashMap<>();
        this.update = update;
    }

    public XMap() {
        this.map = new HashMap<>();
    }


    public XMap(Object obj) {
        if (obj instanceof Update) {
            this.update = (Update) obj;
        }
        this.map = new HashMap<>();
        if (obj instanceof Map) {
            this.map.putAll((Map<? extends K, ? extends V>) obj);
        }
    }

    public boolean setUpdate(Object obj) {
        if (obj instanceof Update && update == null) {
            this.update = (Update) obj;
            return true;
        }
        return false;
    }


    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        V put = map.put(key, value);
        update();
        return put;
    }

    @Override
    public V remove(Object key) {
        V remove = map.remove(key);
        if (update != null) {
            update.update();
        }
        return remove;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
        update();

    }

    @Override
    public void clear() {
        map.clear();
        update();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public String toString() {
        return "XMap{" + "map=" + map + '}';
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    private void update() {
        if (update != null) {
            update.update();
        }
    }

    public int hashCode() {
        return map.hashCode();
    }

}
