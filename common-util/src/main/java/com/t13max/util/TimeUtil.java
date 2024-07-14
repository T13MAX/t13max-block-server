package com.t13max.util;

import lombok.experimental.UtilityClass;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

/**
 * @author: t13max
 * @since: 11:50 2024/4/11
 */
@UtilityClass
public class TimeUtil {

    public int SECOND = 1000;

    public long nowMills() {
        return System.currentTimeMillis();
    }

    /**
     * 地区id， 默认服务器时区，如果有需要后续可以通过配置设置
     */
    public static final ZoneId ZONE_ID = ZoneId.systemDefault();
    /**
     * 秒-对应的毫秒数
     */
    public static final int SEC = 1000;
    /**
     * 分-对应的毫秒数
     */
    public static final int MIN = 60 * SEC;
    /**
     * 小时-对应的毫秒数
     */
    public static final int HOUR = 60 * MIN;
    /**
     * 天-对应的毫秒数
     */
    public static final long DAY = 24 * HOUR;
    /**
     * 周-对应的毫秒数
     */
    public static final long WEEK = 7 * DAY;
    /**
     * 30天对应的毫秒数
     */
    public static final long MONTH = 30 * DAY;
    /**
     * 年(按照365天计算)-对应的毫秒数
     */
    public static final long YEAR = 365 * DAY;
    /**
     * 十年
     */
    public static final long TEN_YEAR = 10 * YEAR;

    /**
     * pattern of date yyyy-MM-dd
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    /**
     * pattern of time HH:mm:ss
     */
    public static final String TIME_PATTERN = "HH:mm:ss";
    /**
     * pattern of dateTime yyyy-MM-dd HH:mm:ss
     */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    //*******************   DateTimeFormatter是线程安全的   *******************//
    /**
     * formatter of date yyyy-MM-dd
     */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    /**
     * formatter of time HH:mm:ss
     */
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);
    /**
     * formatter of dateTime yyyy-MM-dd HH:mm:ss
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    public static final DateTimeFormatter QUARTZ_CORN = DateTimeFormatter.ofPattern("ss mm HH dd MM ? yyyy");

    /**
     * 时区偏移时间
     */
    public static final int TIME_ZONE_OFFSET = TimeZone.getTimeZone(ZONE_ID).getRawOffset();

    /**
     * 格式化时间戳，格式为(yyyy-MM-dd HH:mm:ss)
     *
     * @param timestamp 时间戳
     * @return 格式化的时间字符串(yyyy - MM - dd HH : mm : ss)
     */
    public static String formatTimestamp(long timestamp) {
        return formatTimestamp(timestamp, DATE_TIME_FORMATTER);
    }

    /**
     * 按指定格式化时间戳
     *
     * @param timestamp 时间戳
     * @param pattern   时间格式
     * @return 格式化的时间字符串
     */
    public static String formatTimestamp(long timestamp, String pattern) {
        return formatTimestamp(timestamp, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化时间戳
     *
     * @param timestamp 时间戳
     * @param formatter 时间格式化器
     * @return 格式化的时间字符串
     */
    public static String formatTimestamp(long timestamp, DateTimeFormatter formatter) {
        LocalDateTime dateTime = getDateTimeOfTimestamp(timestamp);
        return dateTime.format(formatter);
    }

    public static String format2QuartzCron(long timestamp) {
        return formatTimestamp(timestamp, QUARTZ_CORN);
    }

    /**
     * 从日期时间字符串解析时间戳
     *
     * @param dateTimeStr 日期时间字符串（yyyy-MM-dd HH:mm:ss）完整包含日期、时间
     * @return 时间戳
     */
    public static long parseToTimestamp(String dateTimeStr) {
        return parseToTimestamp(dateTimeStr, DATE_TIME_FORMATTER);
    }

    /**
     * 从时间字符串解析时间戳
     *
     * @param dateTimeStr 时间字符串，须跟时间格式匹配
     * @param formatter   时间格式化器
     * @return 时间戳
     */
    public static long parseToTimestamp(String dateTimeStr, DateTimeFormatter formatter) {
        // 支持全角半角
        dateTimeStr = dateTimeStr.replace("：", ":");
        LocalDateTime dateTime = parseDateTime(dateTimeStr, formatter);
        return getTimestampOfDateTime(dateTime);
    }

    /**
     * 从字符串解析时间，日期为1970-1-1
     *
     * @param timeStr   时间字符串，须与时间格式匹配
     * @param formatter 时间格式化器
     * @return 时间戳
     */
    public static long parseTime(String timeStr, DateTimeFormatter formatter) {
        LocalTime time = parse2LocalTime(timeStr, formatter);
        LocalDateTime dateTime = time.atDate(LocalDate.EPOCH);
        return getTimestampOfDateTime(dateTime);
    }

    /**
     * 从字符串解析时间，日期为1970-1-1
     *
     * @param timeStr 时间字符串，格式(HH:mm:ss)
     * @return 时间戳
     */
    public static long parseTime(String timeStr) {
        return parseTime(timeStr, TIME_FORMATTER);
    }

    /**
     * 从字符串解析时间，日期为当天
     *
     * @param timeStr   时间字符串，须与时间格式匹配
     * @param formatter 时间格式化器
     * @return 时间戳
     */
    public static long parseTimeAtToday(String timeStr, DateTimeFormatter formatter) {
        LocalDate today = LocalDate.now();
        LocalTime time = parse2LocalTime(timeStr, formatter);
        return getTimestampOfDateTime(today.atTime(time));
    }

    /**
     * 从字符串解析时间，日期为当天
     *
     * @param timeStr 时间字符串，格式(HH:mm:ss)
     * @return 时间戳
     */
    public static long parseTimeAtToday(String timeStr) {
        return parseTimeAtToday(timeStr, TIME_FORMATTER);
    }

    /**
     * 从字符串解析时间，日期为时间戳向后偏移日期
     *
     * @param timeStr   时间字符串，格式(HH:mm:ss)
     * @param timestamp 起始时间戳
     * @param daysToAdd 向后偏移日
     * @return 时间戳
     */
    public static long parseTimePlusDay(String timeStr, long timestamp, long daysToAdd) {
        LocalDate date = getDateOfTimestamp(timestamp);
        if (daysToAdd > 0) {
            date = date.plusDays(daysToAdd);
        }
        LocalTime time = LocalTime.parse(timeStr, TimeUtil.TIME_FORMATTER);
        return getTimestampOfDateTime(date.atTime(time));
    }

    /**
     * 从字符串解析指定日期的时间
     *
     * @param timeStr   时间字符串，须与时间格式匹配
     * @param formatter 时间格式化器
     * @param timestamp 指定日期的时间戳
     * @return 时间戳
     */
    public static long parseTimeAtSpecificDay(String timeStr, DateTimeFormatter formatter, long timestamp) {
        LocalDate day = getDateOfTimestamp(timestamp);
        LocalTime time = parse2LocalTime(timeStr, formatter);
        return getTimestampOfDateTime(day.atTime(time));
    }

    /**
     * 从字符串解析指定日期的时间
     *
     * @param timeStr   时间字符串，格式(HH:mm:ss)
     * @param timestamp 指定日期的时间戳
     * @return 时间戳
     */
    public static long parseTimeAtSpecificDay(String timeStr, long timestamp) {
        return parseTimeAtSpecificDay(timeStr, TIME_FORMATTER, timestamp);
    }

    /**
     * 从字符串解析日期，时间为0点0分0秒
     *
     * @param dateStr   日期字符串，须与时间格式匹配
     * @param formatter 时间格式化器
     * @return 时间毫秒数
     */
    public static long parseDate(String dateStr, DateTimeFormatter formatter) {
        LocalDate date = parse2LocalDate(dateStr, formatter);
        LocalDateTime dateTime = date.atStartOfDay();
        return getTimestampOfDateTime(dateTime);
    }

    /**
     * 从字符串解析日期，时间为0点0分0秒
     *
     * @param dateStr 日期字符串，格式(yyyy-MM-dd)
     * @return 时间毫秒数
     */
    public static long parseDate(String dateStr) {
        return parseDate(dateStr, DATE_FORMATTER);
    }

    /**
     * 是否是同一天
     *
     * @param timestamp1 时间戳a
     * @param timestamp2 时间戳b
     * @return 是否同一天
     */
    public static boolean isSameDay(long timestamp1, long timestamp2) {
        LocalDate date1 = getDateOfTimestamp(timestamp1);
        LocalDate date2 = getDateOfTimestamp(timestamp2);
        return isSameDay(date1, date2);
    }

    /**
     * 是否是同一周
     *
     * @param timestamp1 时间戳a
     * @param timestamp2 时间戳b
     * @return 是否同一周
     */
    public static boolean isSameWeek(long timestamp1, long timestamp2) {
        LocalDate date1 = getDateOfTimestamp(timestamp1);
        LocalDate date2 = getDateOfTimestamp(timestamp2);
        // 都将日期拉到周一再进行比较
        LocalDate monday1 = getMondayOfWeek(date1);
        LocalDate monday2 = getMondayOfWeek(date2);
        return isSameDay(monday1, monday2);
    }

    /**
     * 是否是同一月
     *
     * @param timestamp1 时间戳a
     * @param timestamp2 时间戳b
     * @return 是 true
     */
    public static boolean isSameMonth(long timestamp1, long timestamp2) {
        LocalDate date1 = getDateOfTimestamp(timestamp1);
        LocalDate date2 = getDateOfTimestamp(timestamp2);
        // 将日期拉到1日再进行比较
        LocalDate firstDay1 = getFirstDayOfMonth(date1);
        LocalDate firstDay2 = getFirstDayOfMonth(date2);
        return isSameDay(firstDay1, firstDay2);
    }

    /**
     * 获取两个时间之间的自然天数(timestamp1 < timestamp2)
     * 不考虑时间，所以非绝对天数
     *
     * @param timestamp1 时间戳一
     * @param timestamp2 时间戳二
     * @return 相隔自然天数
     */
    public static int getDaysBetween(long timestamp1, long timestamp2) {
        LocalDate date1 = getDateOfTimestamp(timestamp1);
        LocalDate date2 = getDateOfTimestamp(timestamp2);
        return (int) ChronoUnit.DAYS.between(date1, date2);
    }

    /**
     * 获取两个时间之间的周数，7天为一周(timestamp1 < timestamp2)
     *
     * @param timestamp1 时间戳一
     * @param timestamp2 时间戳二
     * @return 相隔的周数
     */
    public static int getWeeksBetween(long timestamp1, long timestamp2) {
        LocalDate date1 = getDateOfTimestamp(timestamp1);
        LocalDate date2 = getDateOfTimestamp(timestamp2);
        return (int) ChronoUnit.WEEKS.between(date1, date2);
    }

    /**
     * 获取两个时间之间的绝对天数(24小时算一天, timestamp1 < timestamp2)
     *
     * @param timestamp1 时间一
     * @param timestamp2 时间二
     * @return 相隔的绝对天数
     */
    public static int getDaysBetweenAbsolute(long timestamp1, long timestamp2) {
        return (int) Math.floorDiv(timestamp2 - timestamp1, DAY);
    }

    /**
     * 获取指定日期的零点
     *
     * @param timestamp 指定日期的时间戳
     * @return 时间戳
     */
    public static long getZeroOfDayTime(long timestamp) {
        LocalDate date = getDateOfTimestamp(timestamp);
        LocalDateTime dateTime = getZeroOfDay(date);
        return getTimestampOfDateTime(dateTime);
    }

    /**
     * 获取指定星期的时间戳 当前周的周几
     *
     * @param dayOfWeek 周几
     * @return 时间戳
     */
    public static long getSpecificDayTime(DayOfWeek dayOfWeek) {
        LocalDateTime now = LocalDate.now().atStartOfDay();
        LocalDateTime weekDay = now.minusDays(now.getDayOfWeek().getValue() - dayOfWeek.getValue());
        return getTimestampOfDateTime(weekDay);
    }

    /**
     * 获取当前天，从1970年1月1日到指定时间的天数
     *
     * @param timestamp 时间戳
     * @return 天数
     */
    public static int currentDay(long timestamp) {
        return (int) ((timestamp + TIME_ZONE_OFFSET) / DAY);
    }

    /**
     * 获取当前天，从1970年1月1日到指定时间的周数
     *
     * @param timestamp 时间戳
     * @return 周数
     */
    public static int currentWeek(long timestamp) {
        // 1970.1.1是周四, 需要减少四天
        timestamp -= TimeUtil.DAY * 4;

        return (int) ((timestamp + TIME_ZONE_OFFSET) / WEEK);
    }

    /**
     * 生成时间戳形如yyyy-MM-dd HH:mm:ss
     *
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String genTs() {
        return formatTimestamp(nowMills(), DATE_TIME_FORMATTER);
    }

    /**
     * 由时间戳获取dateTime
     *
     * @param timestamp 时间戳
     * @return LocalDateTime
     */
    public static LocalDateTime getDateTimeOfTimestamp(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZONE_ID);
    }

    /**
     * 由时间戳获取日期
     *
     * @param timestamp 时间戳
     * @return LocalDate
     */
    public static LocalDate getDateOfTimestamp(long timestamp) {
        return getDateTimeOfTimestamp(timestamp).toLocalDate();
    }

    /**
     * 由时间戳获取时间
     *
     * @param timestamp 时间戳
     * @return LocalTime
     */
    public static LocalTime getTimeOfTimestamp(long timestamp) {
        return getDateTimeOfTimestamp(timestamp).toLocalTime();
    }

    /**
     * 由日期时间获取时间戳
     *
     * @param dateTime 日期时间
     * @return 时间戳
     */
    public static long getTimestampOfDateTime(LocalDateTime dateTime) {
        return dateTime.atZone(ZONE_ID).toInstant().toEpochMilli();
    }

    /**
     * 解析日期时间
     *
     * @param dateTimeStr 日期时间字符串
     * @param formatter   DateTimeFormatter
     * @return LocalDateTime
     */
    static LocalDateTime parseDateTime(String dateTimeStr, DateTimeFormatter formatter) {
        return LocalDateTime.parse(dateTimeStr, formatter);
    }

    /**
     * 是否是同一天
     *
     * @param date1 日期一
     * @param date2 日期一
     * @return boolean
     */
    static boolean isSameDay(LocalDate date1, LocalDate date2) {
        return date1.equals(date2);
    }

    /**
     * 获取指定日期的当日零点
     *
     * @param date 日期
     * @return LocalDateTime
     */
    static LocalDateTime getZeroOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    /**
     * 获取指定日期的周一
     *
     * @param date 日期
     * @return LocalDate
     */
    static LocalDate getMondayOfWeek(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1);
    }

    /**
     * 获取指定日期的当月1号
     *
     * @param date 日期
     * @return LocalDate
     */
    static LocalDate getFirstDayOfMonth(LocalDate date) {
        return date.withDayOfMonth(1);
    }

    static LocalTime parse2LocalTime(String timeStr, DateTimeFormatter formatter) {
        return LocalTime.parse(timeStr, formatter);
    }

    static LocalDate parse2LocalDate(String dateStr, DateTimeFormatter formatter) {
        return LocalDate.parse(dateStr, formatter);
    }
}
