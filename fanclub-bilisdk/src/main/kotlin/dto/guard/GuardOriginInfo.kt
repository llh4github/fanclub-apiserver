/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.guard

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 原始信息
 */
data class GuardOriginInfo(
    @JsonProperty("name")
    val name: String = "",

    @JsonProperty("face")
    val face: String = ""
)
