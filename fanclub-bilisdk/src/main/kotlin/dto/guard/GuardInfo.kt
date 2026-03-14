/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.guard

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 大航海信息
 */
data class GuardInfo(
    @JsonProperty("num")
    val num: Int = 0,

    @JsonProperty("page")
    val page: Int = 0,

    @JsonProperty("now")
    val now: Int = 0,

    @JsonProperty("achievement_level")
    val achievementLevel: Int = 0,

    @JsonProperty("anchor_guard_achieve_level")
    val anchorGuardAchieveLevel: Int = 0,

    @JsonProperty("achievement_icon_src")
    val achievementIconSrc: String = "",

    @JsonProperty("buy_guard_icon_src")
    val buyGuardIconSrc: String = "",

    @JsonProperty("rule_doc_src")
    val ruleDocSrc: String = "",

    @JsonProperty("ex_background_src")
    val exBackgroundSrc: String = "",

    @JsonProperty("color_start")
    val colorStart: String = "",

    @JsonProperty("color_end")
    val colorEnd: String = "",

    @JsonProperty("tab_color")
    val tabColor: List<String>? = null,

    @JsonProperty("title_color")
    val titleColor: List<String>? = null
)
