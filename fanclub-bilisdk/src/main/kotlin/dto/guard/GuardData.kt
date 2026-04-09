/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.guard

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 响应数据
 */
data class GuardData(
    @JsonProperty("info")
    val info: GuardInfo? = null,

    @JsonProperty("list")
    val list: List<GuardListItem> = emptyList(),

    @JsonProperty("top3")
    val top3: List<GuardListItem> = emptyList(),

    @JsonProperty("my_follow_info")
    val myFollowInfo: GuardMyFollowInfo? = null,

    @JsonProperty("guard_warn")
    val guardWarn: GuardWarn? = null,

    @JsonProperty("exist_benefit")
    val existBenefit: Boolean = false,

    @JsonProperty("remind_benefit")
    val remindBenefit: String = "",

    @JsonProperty("ab")
    val ab: GuardAB? = null,

    @JsonProperty("remind_msg")
    val remindMsg: String = "",

    @JsonProperty("typ")
    val typ: Int = 0,

    @JsonProperty("extop")
    val extop: Any? = null,

    @JsonProperty("guard_leader")
    val guardLeader: Any? = null,

    @JsonProperty("main_text")
    val mainText: String = "",

    @JsonProperty("sub_text")
    val subText: String = "",

    @JsonProperty("btn_type")
    val btnType: Int = 0,

    @JsonProperty("prompt_text")
    val promptText: String = ""
)
