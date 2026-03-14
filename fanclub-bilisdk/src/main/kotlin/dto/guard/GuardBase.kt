/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.guard

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 用户基础信息
 */
data class GuardBase(
    @JsonProperty("name")
    val name: String = "",

    @JsonProperty("face")
    val face: String = "",

    @JsonProperty("name_color")
    val nameColor: Int = 0,

    @JsonProperty("is_mystery")
    val isMystery: Boolean = false,

    @JsonProperty("risk_ctrl_info")
    val riskCtrlInfo: Any? = null,

    @JsonProperty("origin_info")
    val originInfo: GuardOriginInfo? = null,

    @JsonProperty("official_info")
    val officialInfo: GuardOfficialInfo? = null,

    @JsonProperty("name_color_str")
    val nameColorStr: String = ""
)
