/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.anchor

import llh.fanclubvup.apiserver.entity.anchor.AnchorLiveDuration
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveDurationDateDuration
import llh.fanclubvup.apiserver.service.BaseDatabaseService
import java.time.LocalDate

interface AnchorLiveDurationService : BaseDatabaseService<AnchorLiveDuration> {

    /**
     * 计算指定房间指定日期的直播时长
     * @param roomId 房间ID
     * @param date 日期(通常为昨天)
     * @return 更新数据的条数
     */
    fun computeLiveDuration(roomId: Long, date: LocalDate): Int

    fun fetchLiveDurationHistory(roomId: Long, date: LocalDate): List<AnchorLiveDurationDateDuration>
}
