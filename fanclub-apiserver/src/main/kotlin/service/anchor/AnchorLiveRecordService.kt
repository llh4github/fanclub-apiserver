/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.anchor

import llh.fanclubvup.apiserver.entity.anchor.AnchorLiveRecord
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveRecordEndLiveInput
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveRecordLiveStatus
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveRecordQueryWeekSpec
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveTimeRecord
import llh.fanclubvup.apiserver.service.BaseDatabaseService

interface AnchorLiveRecordService : BaseDatabaseService<AnchorLiveRecord> {

    fun updateEndLiveStatus(input: AnchorLiveRecordEndLiveInput): Int

    /**
     * 结束太久没有结束的直播记录
     */
    fun finishLiveForOvertime()

    /**
     * 获取直播状态
     */
    fun fetchLiveStatus(roomId: Long): AnchorLiveRecordLiveStatus

    /**
     * 获取最新[last]条 正常结束的直播数据
     */
    fun fetchEndLiveRecord(roomId: Long, last: Int): List<AnchorLiveRecordLiveStatus>

    /**
     * 获取周直播记录
     */
    fun fetchWeekLiveRecord(spec: AnchorLiveRecordQueryWeekSpec): List<AnchorLiveTimeRecord>
}
