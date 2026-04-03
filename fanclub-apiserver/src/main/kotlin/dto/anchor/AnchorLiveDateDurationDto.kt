package llh.fanclubvup.apiserver.dto.anchor

import java.time.LocalDate

data class AnchorLiveDateDurationDto(
    val liveDate: LocalDate,
    /**
     * 直播时长，单位秒
     */
    val liveDuration: Long,
)
