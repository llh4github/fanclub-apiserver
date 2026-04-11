/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.anchor

import llh.fanclubvup.apiserver.entity.anchor.AnchorLiveSchedule
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveScheduleAddInput
import llh.fanclubvup.apiserver.service.BaseDatabaseService
import llh.fanclubvup.common.BID
import java.time.LocalDate

interface AnchorLiveScheduleService : BaseDatabaseService<AnchorLiveSchedule> {

    fun saveList(input: List<AnchorLiveScheduleAddInput>): Int

    /**
     * 判断指定房间的指定周是否有日程
     */
    fun hasSchedule(bId: BID, targetWeek: LocalDate): Boolean
}
