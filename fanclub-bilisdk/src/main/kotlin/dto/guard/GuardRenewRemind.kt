/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.guard

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 续费提醒
 */
data class GuardRenewRemind(
    @JsonProperty("content")
    val content: String = "",

    @JsonProperty("type")
    val type: Int = 0,

    @JsonProperty("hint")
    val hint: String = ""
)
