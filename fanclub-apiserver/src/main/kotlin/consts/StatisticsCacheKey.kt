package llh.fanclubvup.apiserver.consts

import java.time.LocalDate

object StatisticsCacheKey {

    const val DANMU_STATISTICS_PREFIX = "fanclub-statistics:danmu"
    const val NICKNAME_CHANGE_PREFIX = "fanclub-statistics:nickname-change"

    fun nicknameChange(): String {
        return NICKNAME_CHANGE_PREFIX
    }
    /**
     * 弹幕发送数量统计key
     */
    fun danmuCount(date: LocalDate = LocalDate.now()): String {
        return "$DANMU_STATISTICS_PREFIX:${date}"
    }
}
