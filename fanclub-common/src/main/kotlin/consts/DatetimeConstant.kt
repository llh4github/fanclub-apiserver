/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.common.consts

import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DatetimeConstant {
    /**
     * 日期时间格式: yyyy-MM-dd HH:mm:ss
     */
    const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"

    /**
     * 日期时间格式: yyyyMMddHH:mm:ss
     */
    const val DATE_TIME_FORMAT_COMPACT = "yyyyMMddHH:mm:ss"

    val DATE_TIME_FORMATTER_COMPACT: DateTimeFormatter =
        DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_COMPACT).withZone(ZoneId.systemDefault())

    val DATE_TIME_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).withZone(ZoneId.systemDefault())

    const val BEIJING_TIME_ZONE = "Asia/Shanghai"
}
