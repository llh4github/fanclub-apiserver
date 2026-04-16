/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.entity.anchor

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import llh.fanclubvup.apiserver.consts.enums.LiveRecordStatus
import llh.fanclubvup.apiserver.entity.AnchorRoomIdAware
import llh.fanclubvup.apiserver.entity.BaseEntity
import llh.fanclubvup.common.consts.DatetimeConstant
import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.Table
import java.time.LocalDateTime

@Entity
@Table(name = "anchor_live_record")
@Schema(title = "主播直播记录")
interface AnchorLiveRecord : BaseEntity, AnchorRoomIdAware {


    @Key(group = "room_live_record_uk")
    @Column(name = "live_key")
    @get:Schema(title = "直播场次key", description = "直播场次key", example = "114514")
    val liveKey: String

    @Column(name = "live_time")
    @get:Schema(title = "直播开始时间", description = "直播开始时间", example = "2023-01-01 00:00:00")
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DatetimeConstant.DATE_TIME_FORMAT)
    val liveTime: LocalDateTime

    @get:Schema(title = "直播状态")
    @Column(name = "live_status")
    val liveStatus: LiveRecordStatus

    @Column(name = "end_live_time")
    @get:Schema(title = "直播结束时间", description = "直播结束时间", example = "2023-01-01 00:00:00")
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DatetimeConstant.DATE_TIME_FORMAT)
    val endLiveTime: LocalDateTime?

    @Column(name = "live_duration")
    @get:Schema(title = "直播时长(秒)", description = "异常情况下为 null", example = "114514")
    val liveDuration: Long?
}
