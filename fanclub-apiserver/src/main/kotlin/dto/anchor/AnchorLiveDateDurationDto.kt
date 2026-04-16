package llh.fanclubvup.apiserver.dto.anchor

import java.time.LocalDate

/**
 * 日期-时长数据类
 * @param liveDate  直播日期
 * @param liveDuration  直播时长，单位秒
 */
data class AnchorLiveDateDurationDto(
    val liveDate: LocalDate,
    val liveDuration: Long,
)
