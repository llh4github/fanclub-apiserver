/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.common.utils

import java.time.*

object LocalDateTimeUtil {

    val ZERO_TIME: LocalTime = LocalTime.of(0, 0, 0, 0)
    val END_TIME: LocalTime = LocalTime.of(23, 59, 59, 999999999)

    /**
     * 一天的总秒数：86400 秒 (24 * 60 * 60)
     */
    const val SECONDS_PER_DAY = 86400L

    /**
     * 将秒数转换为 LocalDateTime
     *
     * @param seconds 秒数
     * @return LocalDateTime 对象
     */
    fun toLocalDateTime(seconds: Long) =
        Instant.ofEpochSecond(seconds).atZone(ZoneId.systemDefault()).toLocalDateTime()

    /**
     * 将毫秒数转换为 LocalDateTime
     *
     * @param ms 毫秒数
     * @return LocalDateTime 对象
     */
    fun toLocalDateTimeEpochMilli(ms: Long): LocalDateTime =
        Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDateTime()

    fun diffSeconds(start: LocalDateTime, end: LocalDateTime): Long {
        return Duration.between(start, end).seconds
    }

    fun dayOfStart(date: LocalDateTime): LocalDateTime {
        return date.withHour(0).withMinute(0).withSecond(0).withNano(0)
    }

    fun isSameDay(date1: LocalDateTime, date2: LocalDateTime): Boolean {
        return date1.toLocalDate() == date2.toLocalDate()
    }

    fun dayOfStart(date: LocalDate): LocalDateTime {
        return LocalDateTime.of(date, ZERO_TIME)
    }

    fun dayOfEnd(date: LocalDate): LocalDateTime {
        return LocalDateTime.of(date, END_TIME)
    }

    fun dayOfEnd(date: LocalDateTime): LocalDateTime {
        return date.withHour(23).withMinute(59).withSecond(59).withNano(999999999)
    }

    /**
     * 获取指定日期所在周的周一和周日
     *
     * @param date 输入日期
     * @return Pair<周一零点, 周日结束时间>，两个 LocalDateTime 对象
     */
    fun weekRange(date: LocalDate): Pair<LocalDateTime, LocalDateTime> {
        val monday = LocalDateTime.of(date.with(DayOfWeek.MONDAY), ZERO_TIME)
        val sunday = LocalDateTime.of(date.with(DayOfWeek.SUNDAY), END_TIME)
        return Pair(monday, sunday)
    }

    /**
     * 获取指定日期所在周的周一和周日
     *
     * @param date 输入日期时间
     * @return Pair<周一零点, 周日结束时间>，两个 LocalDateTime 对象
     */
    fun weekRange(date: LocalDateTime): Pair<LocalDateTime, LocalDateTime> {
        return weekRange(date.toLocalDate())
    }
}
