package com.t13max.util;

import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * 随机工具类
 *
 * @Author t13max
 * @Date 14:33 2024/4/16
 */
@UtilityClass
public class RandomUtil {

    private final static String SEMICOLON = ";";
    private final static String COMMA = ",";

    /**
     * 给一个概率，返回是否中签
     */
    public static boolean gaiLvTo(double num) {
        if (num <= 0) {
            return false;
        }
        if (num >= 100) {
            return true;
        }
        return getRanNumByIntervalDouble(0, 100) < num;

//        double r = ThreadLocalRandom.current().nextDouble() * 100;
//        if (r < num) {
//            return true;
//        }
//        return false;
    }

    /**
     * 给一个区间，返回一个中间一个随机数 包含min,max
     */
    public static int getRanNumByInterval(int min, int max) {
        if (min == max) {
            return min;
        }
        if (max < min) {
            int oldMax = max;
            max = min;
            min = oldMax;
        }
        return (int) (ThreadLocalRandom.current().nextDouble() * (max - min + 1)) + min;
    }

    public static int getRanNumByInterval(int max) {
        return getRanNumByInterval(0, max);
    }


    public static long getRanNumByInterval(long min, long max) {
        if (min == max) {
            return min;
        }
        if (max < min) {
            long oldMax = max;
            max = min;
            min = oldMax;
        }
        return (long) (ThreadLocalRandom.current().nextDouble() * (max - min + 1)) + min;
    }

    /**
     * 给一个区间，返回一个中间一个随机数 无法包含min,max
     */
    public static double getRanNumByIntervalDouble(double min, double max) {
        if (min == max) {
            return min;
        }
        if (max < min) {
            double oldMax = max;
            max = min;
            min = oldMax;
        }
        return ThreadLocalRandom.current().nextDouble() * (max - min) + min;
    }

    /**
     * 给一个区间，返回一个中间一个list队列随机数
     */
    public static Set<Integer> getRanListByInterval(int min, int max, int n) {
        Set<Integer> list = new HashSet<>();
//        if (max - min < n) {
//            return list;
//        }
        n = Math.min(n, max - min + 1);
        for (int i = 0; i < n; i++) {
            if (list.size() == n) {
                return list;
            }
            int num = (int) (ThreadLocalRandom.current().nextDouble() * (max - min + 1)) + min;
            if (list.contains(num)) {
                --i;
            } else {
                list.add(num);
            }
        }
        return list;
    }

    public static int randomIndexByWeight(int[] weight) {
        if (Objects.isNull(weight)) {
            throw new IllegalArgumentException("weightArray is null");
        }
        if (weight.length == 0) {
            throw new IllegalArgumentException("weightArray.length==0");
        }

        int total = 0;
        for (int num : weight) {
            if (num <= 0) {
                continue;
            }
            total += num;
        }
        int randNum = getRanNumByInterval(1, total);
        int index = 0;
        total = 0;
        for (int k = 0; k < weight.length; k++) {
            total += weight[k];
            if (randNum <= total) {
                index = k;
                break;
            }
        }
        return index;
    }

    /**
     * 根据权重随机一个
     */
    public static <T> T random(List<T> list, Function<T, Integer> function) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        int[] weights = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            weights[i] = function.apply(list.get(i));
        }
        int index = randomIndexByWeight(weights);
        return list.get(index);
    }

    /**
     * 根据权重随机N个
     */
    public static <T> List<T> randoms(List<T> list, int resultNum, Function<T, Integer> function) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        int[] weights = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            weights[i] = function.apply(list.get(i));
        }
        List<T> listResult = new LinkedList<>();
        for (int i = 0; i < resultNum; i++) {
            int index = randomIndexByWeight(weights);
            listResult.add(list.get(index));
        }
        return listResult;
    }

    public static <T> T random(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        int index = getRanNumByInterval(list.size() - 1);
        return list.get(index);
    }

    public static <T> List<T> random(List<T> list, int num) {
        if (list == null || list.isEmpty() || list.size() < num) {
            return null;
        }
        Set<T> result = new HashSet<>();
        while (result.size() < num) {
            int index = getRanNumByInterval(list.size() - 1);
            result.add(list.get(index));
        }
        return new LinkedList<>(result);
    }


    //权重进行随机 返回随机的id
    public static int getRandomByWeight(Map<Integer, Integer> map) {
        int max = 0;
        List<Integer> idlist = new ArrayList<>();
        List<Integer> probabilitylist = new ArrayList<>();

        for (Entry<Integer, Integer> entry : map.entrySet()) {
            idlist.add(entry.getKey());
            max = entry.getValue() + max; //权重值
            probabilitylist.add(max);
        }

        int ranNumByInterval = RandomUtil.getRanNumByInterval(1, max);
        for (int x = 0; x < probabilitylist.size(); x++) {
            if (probabilitylist.get(x) >= ranNumByInterval) {
                return idlist.get(x);
            }
        }
        return -1;
    }

    //权重进行随机 返回随机的id
    public static int getRandomByWeight(String randomString) {
        int max = 0;
        List<Integer> idlist = new ArrayList<>();
        List<Integer> probabilitylist = new ArrayList<>();
        String[] split = randomString.split(SEMICOLON);
        for (String s : split) {
            String[] split1 = s.split(COMMA);
            idlist.add(Integer.parseInt(split1[0]));//id
            max = Integer.parseInt(split1[1]) + max;//权重值
            probabilitylist.add(max);
        }
        int ranNumByInterval = RandomUtil.getRanNumByInterval(1, max);
        for (int x = 0; x < probabilitylist.size(); x++) {
            if (probabilitylist.get(x) >= ranNumByInterval) {
                return idlist.get(x);
            }
        }
        return -1;
    }

    // 权重进行随机 返回随机的多个id
    public static Set<Integer> getRandomManyByWeight(String randomString, int num) {
        int numInit = num;
        Set<Integer> randomIds = new HashSet<>();

        for (int i = 0; i < num; i++) {
            // 防止死循环(最多10次)
            if (num - numInit >= 10) {
                break;
            }

            int attributeId = getRandomByWeight(randomString);
            if (randomIds.contains(attributeId)) {
                ++num;
                continue;
            }
            randomIds.add(attributeId);
        }

//        if (num != numInit) {
//            System.err.println("===num - numInit:" + (num - numInit) + "===============");
//        }
//        if (num - numInit >= 10) {
//            System.err.println("-------------" + (num - numInit) + "-----------------");
//        }
        return randomIds;
    }


    // 随机num个不重复元素[元素数量小于num, 则使用当前所有元素集合即可]
    public Set<Integer> random(String str, int resultNum) {
        return random(str, resultNum, COMMA);
    }

    public Set<Integer> random(String str, int resultNum, String splitStr) {
        Set<Integer> result = new HashSet<>();
        if (resultNum <= 0) {
            return result;
        }

        List<Integer> list = new LinkedList<>();
        if (splitStr.equals(COMMA)) {
            list = decodeToIntList(str);
        }

        if (resultNum >= list.size()) {
            result.addAll(list);
        } else {
            while (list.size() > 0 && result.size() < resultNum) {
                Integer random = random(list);
                result.add(random);
                list.remove(random);
            }
        }

        return result;
    }

    private List<Integer> decodeToIntList(String str) {
        List<Integer> list = new LinkedList<>();
        String[] split = str.split(",");
        for (String s : split) {
            list.add(Integer.parseInt(s));
        }
        return list;
    }

    // 随机num个不重复元素 -- 优先不重复元素, 如果分隔出来的元素数量不足则再次随机出不足的那几个重复的元素
    public static List<Integer> randomSplitStrByNum(String idStr, int num, String splitStr) {

        Set<Integer> allIds = RandomUtil.random(idStr, num, splitStr);
        if (allIds == null || allIds.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        List<Integer> idList = new ArrayList<>();

        // 随机出来的id数量不够
        int lackNum = num - idList.size();
        if (lackNum > 0) {
            for (int i = 0; i < lackNum; i++) {
                int randomId = RandomUtil.random(idList);

                idList.add(randomId);
            }
        }

        return idList;
    }


    public static void main(String[] args) {
        Set<Integer> ids = random("1;2;3;5;6;", 3, ";");
        for (int id : ids) {
            System.err.println(id);
        }
    }

}
