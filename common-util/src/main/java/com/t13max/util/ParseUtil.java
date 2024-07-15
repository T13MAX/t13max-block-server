package com.t13max.util;

import lombok.experimental.UtilityClass;

import java.util.LinkedList;
import java.util.List;

/**
 * @author: t13max
 * @since: 15:19 2024/4/23
 */
@UtilityClass
public class ParseUtil {

    public static final String PART = ";";

    public static List<Integer> getIntList(String str) {
        return getIntList(str, ",");
    }

    public static List<Integer> getIntList(String str, String separation) {
        String[] split = str.split(separation);
        List<Integer> result = new LinkedList<>();
        for (String item : split) {
            result.add(Integer.parseInt(item));
        }
        return result;
    }
}
