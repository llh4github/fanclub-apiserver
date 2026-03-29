/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.common.utils

import java.time.Instant
import java.time.ZoneId

object LocalDateTimeUtil {

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
    fun toLocalDateTimeEpochMilli(ms: Long) =
        Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDateTime()
}
