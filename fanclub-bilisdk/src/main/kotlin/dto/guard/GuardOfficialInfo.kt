/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.guard

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 官方信息
 */
data class GuardOfficialInfo(
    @JsonProperty("role")
    val role: Int = 0,

    @JsonProperty("title")
    val title: String = "",

    @JsonProperty("desc")
    val desc: String = "",

    @JsonProperty("type")
    val type: Int = 0
)
