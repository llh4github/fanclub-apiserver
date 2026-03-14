/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.guard

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 列表项
 */
data class GuardListItem(
    @JsonProperty("ruid")
    val ruid: Long = 0L,

    @JsonProperty("rank")
    val rank: Int = 0,

    @JsonProperty("accompany")
    val accompany: Int = 0,

    @JsonProperty("uinfo")
    val uinfo: GuardUserinfo? = null,

    @JsonProperty("score")
    val score: Int = 0
)
