/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.guard

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 大航海信息
 */
data class GuardGuard(
    @JsonProperty("level")
    val level: Int = 0,

    @JsonProperty("expired_str")
    val expiredStr: String = ""
)
