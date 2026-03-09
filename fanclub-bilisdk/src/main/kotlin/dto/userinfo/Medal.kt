/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.userinfo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 勋章基本信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Medal(
    /**
     * 勋章等级，示例：31
     */
    @JsonProperty("level")
    val level: Int? = null,
    /**
     * 守护等级，示例：3
     */
    @JsonProperty("guard_level")
    val guardLevel: Int? = null,
    /**
     * 勋章颜色，示例：2951253
     */
    @JsonProperty("medal_color")
    val medalColor: Int? = null,
    /**
     * 勋章名称
     */
    @JsonProperty("medal_name")
    val medalName: String? = null,
    /**
     * 勋章边框颜色，示例：6809855
     */
    @JsonProperty("medal_color_border")
    val medalColorBorder: Int? = null,
    /**
     * 勋章渐变开始颜色，示例：2951253
     */
    @JsonProperty("medal_color_start")
    val medalColorStart: Int? = null,
    /**
     * 勋章渐变结束颜色，示例：10329087
     */
    @JsonProperty("medal_color_end")
    val medalColorEnd: Int? = null
)