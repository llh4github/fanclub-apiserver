/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.guard

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 勋章信息
 */
data class GuardMedal(
    @JsonProperty("name")
    val name: String = "",

    @JsonProperty("level")
    val level: Int = 0,

    @JsonProperty("color_start")
    val colorStart: Int = 0,

    @JsonProperty("color_end")
    val colorEnd: Int = 0,

    @JsonProperty("color_border")
    val colorBorder: Int = 0,

    @JsonProperty("color")
    val color: Int = 0,

    @JsonProperty("id")
    val id: Int = 0,

    @JsonProperty("typ")
    val typ: Int = 0,

    @JsonProperty("is_light")
    val isLight: Int = 0,

    @JsonProperty("ruid")
    val ruid: Long = 0L,

    @JsonProperty("guard_level")
    val guardLevel: Int = 0,

    @JsonProperty("score")
    val score: Int = 0,

    @JsonProperty("guard_icon")
    val guardIcon: String = "",

    @JsonProperty("honor_icon")
    val honorIcon: String = "",

    @JsonProperty("v2_medal_color_start")
    val v2MedalColorStart: String = "",

    @JsonProperty("v2_medal_color_end")
    val v2MedalColorEnd: String = "",

    @JsonProperty("v2_medal_color_border")
    val v2MedalColorBorder: String = "",

    @JsonProperty("v2_medal_color_text")
    val v2MedalColorText: String = "",

    @JsonProperty("v2_medal_color_level")
    val v2MedalColorLevel: String = "",

    @JsonProperty("user_receive_count")
    val userReceiveCount: Int = 0
)
