/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.anchor

import llh.fanclubvup.apiserver.consts.enums.LiveRecordStatus
import llh.fanclubvup.apiserver.entity.anchor.AnchorLiveRecord
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveRecordEndLiveInput
import llh.fanclubvup.apiserver.service.BaseDatabaseService

interface AnchorLiveRecordService : BaseDatabaseService<AnchorLiveRecord> {

    fun updateEndLiveStatus(input: AnchorLiveRecordEndLiveInput): Int

    /**
     * 结束太久没有结束的直播记录
     */
    fun finishLiveForOvertime()

    fun isLive(roomId: Long): LiveRecordStatus
}
