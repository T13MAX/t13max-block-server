package com.t13max.persist.modify;

public enum Option {
    NONE(0), UPDATE(1), INSERT(2), DELETE(4);

    public final int code;

    private Option(int code) {
        this.code = code;
    }

    public boolean match(int op) {
        return (this.code & op) != 0;
    }

}