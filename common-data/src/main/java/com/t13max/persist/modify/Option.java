package com.t13max.persist.modify;

public enum Option {

    //无状态
    NONE(0),
    //发生更新
    UPDATE(1),
    //新插入
    INSERT(2),
    //删除
    DELETE(4),
    //保存中
    SAVING(5);

    public final int code;

    private Option(int code) {
        this.code = code;
    }

    public boolean match(int op) {
        return (this.code & op) != 0;
    }

}