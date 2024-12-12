package com.t13max.game.entity;

/**
 * 间接对象
 *
 * @Author t13max
 * @Date 18:02 2024/12/11
 */
public class IndirectObject<T> {
    T obj;

    private boolean status;

    public IndirectObject(T obj) {
        this.obj = obj;
        this.status = true;
    }


    public T get() {

        return isInvalid() ? null : obj;
    }

    public void invalid() {

        this.status = false;
    }

    public void valid() {

        this.status = true;
    }

    public boolean isInvalid() {

        return this.obj == null || !this.status;
    }

}
