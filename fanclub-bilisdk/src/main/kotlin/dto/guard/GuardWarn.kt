/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.guard

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 大航海警告信息
 */
data class GuardWarn(
    @JsonProperty("is_warn")
    val isWarn: Int = 0,

    @JsonProperty("warn")
    val warn: String = "",

    @JsonProperty("expired")
    val expired: Int = 0,

    @JsonProperty("will_expired")
    val willExpired: Int = 0,

    @JsonProperty("address")
    val address: String = ""
)
