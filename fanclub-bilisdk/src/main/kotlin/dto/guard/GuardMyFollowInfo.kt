/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.guard

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 我的关注信息
 */
data class GuardMyFollowInfo(
    @JsonProperty("accompany_days")
    val accompanyDays: Int = 0,

    @JsonProperty("auto_renew")
    val autoRenew: Int = 0,

    @JsonProperty("renew_remind")
    val renewRemind: GuardRenewRemind? = null,

    @JsonProperty("rank")
    val rank: Int = 0,

    @JsonProperty("ruid")
    val ruid: Long = 0L,

    @JsonProperty("uinfo")
    val uinfo: GuardMyFollowUserinfo? = null,

    @JsonProperty("expired_time")
    val expiredTime: String = ""
)
