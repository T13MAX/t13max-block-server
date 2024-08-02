package com.t13max.persist.modify;


import com.t13max.persist.data.IPersistData;

public interface Update {

    static Option state(Object obj) {
        if (obj instanceof Update) {
            Update update = (Update) obj;
            byte option = update.option();
            if (Option.INSERT.match(option)) {
                update.clear();
                return Option.INSERT;
            } else if (Option.UPDATE.match(option)) {
                update.clear();
                return Option.UPDATE;
            }
        }
        return Option.NONE;
    }

    static boolean saveAble(IPersistData data) {
        return state(data) != Option.NONE;
    }

    public void update();

    public void clear();

    public <T extends IPersistData> byte option();

    public void insert();

    static <T extends IPersistData> void insert(T t) {
        if (t instanceof Update) {
            Update update = (Update) t;
            update.insert();
        }
    }

    static <T extends IPersistData> void update(T t) {
        if (t instanceof Update) {
            Update update = (Update) t;
            update.update();
        }
    }

    static <T extends IPersistData> void clear(T t) {
        if (t instanceof Update) {
            Update update = (Update) t;
            update.clear();
        }
    }
}
