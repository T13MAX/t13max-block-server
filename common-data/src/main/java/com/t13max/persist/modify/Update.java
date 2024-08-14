package com.t13max.persist.modify;


import com.t13max.persist.data.IData;

public interface Update {

    static Option state(Object obj) {
        if (obj instanceof Update update) {
            byte option = update.option();
            if (Option.INSERT.match(option)) {
                update.saving();
                return Option.INSERT;
            } else if (Option.UPDATE.match(option)) {
                update.saving();
                return Option.UPDATE;
            }
        }
        return Option.NONE;
    }

    static boolean saveAble(IData data) {
        return state(data) != Option.NONE;
    }

    void update();

    void saving();

    void clear();

    <T extends IData> byte option();

    void insert();

    static <T extends IData> void insert(T t) {
        if (t instanceof Update update) {
            update.insert();
        }
    }

    static <T extends IData> void update(T t) {
        if (t instanceof Update update) {
            update.update();
        }
    }

    static <T extends IData> void clear(T t) {
        if (t instanceof Update update) {
            update.clear();
        }
    }
}
