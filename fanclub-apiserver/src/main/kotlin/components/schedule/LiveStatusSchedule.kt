/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.components.schedule

import llh.fanclubvup.apiserver.service.anchor.AnchorLiveRecordService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class LiveStatusSchedule(
    private val service: AnchorLiveRecordService
) {
    /**
     * 定时更新直播状态
     */
    @Scheduled(cron = "0 30 4 * * ?")
    fun updateLiveStatus() {
        service.finishLiveForOvertime()
    }
}
