/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.dto.ai

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import llh.fanclubvup.common.consts.DatetimeConstant
import org.springframework.core.ParameterizedTypeReference
import java.time.LocalDateTime

/**
 * 直播日程
 *
 * 用于接收AI结构化输出数据的
 */
data class AnchorLiveScheduleItem(
    @JsonProperty(required = true, value = "topic")
    val topic: String,
    @JsonProperty(required = true, value = "startTime")
    @JsonFormat(pattern = DatetimeConstant.DATE_TIME_FORMAT_COMPACT, timezone = "GMT+8")
    val startTime: LocalDateTime,
    @JsonProperty(required = true, value = "endTime")
    @JsonFormat(pattern = DatetimeConstant.DATE_TIME_FORMAT_COMPACT, timezone = "GMT+8")
    val endTime: LocalDateTime
)

object AnchorLiveScheduleItemTypeRef : ParameterizedTypeReference<List<AnchorLiveScheduleItem>>() {
}
