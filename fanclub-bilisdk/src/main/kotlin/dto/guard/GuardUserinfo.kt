/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.guard

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 用户信息
 */
data class GuardUserinfo(
    @JsonProperty("uid")
    val uid: Long = 0L,

    @JsonProperty("base")
    val base: GuardBase? = null,

    @JsonProperty("medal")
    val medal: GuardMedal? = null,

    @JsonProperty("wealth")
    val wealth: Any? = null,

    @JsonProperty("title")
    val title: Any? = null,

    @JsonProperty("guard")
    val guard: GuardGuard? = null,

    @JsonProperty("uhead_frame")
    val uheadFrame: Any? = null,

    @JsonProperty("guard_leader")
    val guardLeader: Any? = null
)
