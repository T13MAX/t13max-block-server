package com.t13max.persist.collection;

import com.t13max.persist.modify.Update;
import dev.morphia.annotations.Transient;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class XSet<T> implements Set<T>, Serializable {

    private Set<T> dataSet;
    @Setter
    @Getter
    @Transient
    private Update update;

    public XSet() {
        this.dataSet = new HashSet<>();
    }

    public XSet(Update update) {
        this.dataSet = new HashSet<>();
        this.update = update;
    }


    public XSet(Object obj) {
        if (obj instanceof Update) {
            this.update = (Update) obj;
        }
        this.dataSet = new HashSet<>();
        if (obj instanceof Collection) {
            this.dataSet.addAll(((Collection<T>) obj));
        }
    }


    @Override
    public int size() {
        return dataSet.size();
    }

    @Override
    public boolean isEmpty() {
        return dataSet.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return dataSet.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return dataSet.iterator();
    }

    @Override
    public Object[] toArray() {
        return dataSet.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return dataSet.toArray(a);
    }

    @Override
    public boolean add(T t) {
        boolean add = dataSet.add(t);
        update();
        return add;
    }

    @Override
    public boolean remove(Object o) {
        boolean remove = dataSet.remove(o);
        update();
        return remove;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return dataSet.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean b = dataSet.addAll(c);
        update();
        return b;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean b = dataSet.retainAll(c);
        update();
        return b;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean b = dataSet.removeAll(c);
        update();
        return b;
    }

    @Override
    public void clear() {
        dataSet.clear();
        update();
    }

    private void update() {
        if (update != null) {
            update.update();
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
    public String toString() {
        return "XSet{" + "data=" + dataSet + '}';
    }

    public int hashCode() {
        return dataSet.hashCode();
    }
}
