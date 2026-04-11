/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.consts

import llh.fanclubvup.common.consts.CacheKeyPrefix
import java.time.LocalDate

object StatisticsCacheKey {

    const val DANMU_STATISTICS_PREFIX = CacheKeyPrefix.DANMU_STATS_CACHE_KEY + "danmu"
    const val NICKNAME_CHANGE_PREFIX = CacheKeyPrefix.DANMU_STATS_CACHE_KEY + "nickname-change"

    fun nicknameChange(): String {
        return NICKNAME_CHANGE_PREFIX
    }

    /**
     * 弹幕发送数量统计key
     */
    fun danmuCount(roomId: Long, date: LocalDate = LocalDate.now()): String {
        return "$DANMU_STATISTICS_PREFIX:$roomId:${date}"
    }
}
