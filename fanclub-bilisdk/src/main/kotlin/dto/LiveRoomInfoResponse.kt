/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto

import com.fasterxml.jackson.annotation.JsonProperty
import llh.fanclubvup.bilisdk.dto.liveroom.LiveRoomData

/**
 * B 站直播间信息响应
 */
data class LiveRoomInfoResponse(
    @JsonProperty("data")
    val data: LiveRoomData? = null
): ScraperBaseResp()
