/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.liveroom

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 演播室信息
 */
data class StudioInfo(
    @JsonProperty("status")
    val status: Int,  // 状态

    @JsonProperty("master_list")
    val masterList: List<StudioMaster> = emptyList()  // 主播列表
)
