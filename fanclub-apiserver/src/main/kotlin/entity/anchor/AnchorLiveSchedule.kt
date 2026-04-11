/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.entity.anchor

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import llh.fanclubvup.apiserver.entity.BaseEntity
import llh.fanclubvup.common.BID
import llh.fanclubvup.common.consts.DatetimeConstant
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Table
import java.time.LocalDateTime

@Entity
@Schema(title = "主播直播日程安排")
@Table(name = "anchor_live_schedule")
interface AnchorLiveSchedule : BaseEntity {
    @get:Schema(title = "B站ID", description = "通常称为UID", example = "114514")
    val bid: BID

    @get:Schema(title = "直播主题", description = "直播主题", example = "今天是周日")
    val topic: String

    @get:Schema(title = "直播开始时间", description = "直播开始时间", example = "2023-01-01 00:00:00")
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DatetimeConstant.DATE_TIME_FORMAT)
    val startTime: LocalDateTime

    @get:Schema(title = "直播结束时间", description = "直播结束时间", example = "2023-01-01 00:00:00")
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DatetimeConstant.DATE_TIME_FORMAT)
    val endTime: LocalDateTime

}
