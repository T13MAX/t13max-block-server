package com.t13max.game.utils;

import lombok.experimental.UtilityClass;

/**
 * @author t13max
 * @since 16:21 2024/12/10
 */
@UtilityClass
public class Utils {

    public static double EPSILON = 1.0e-6;

    public static boolean isFloatEqual(float f1, float f2) {
        return Math.abs(f1 - f2) < 0.000001f;
    }

    public static boolean floatEqual(float f1, float f2) {
        return Math.abs(f1 - f2) < EPSILON;
    }

    public static boolean floatEqual(double d1, double d2) {
        return Math.abs(d1 - d2) < EPSILON;
    }
}
