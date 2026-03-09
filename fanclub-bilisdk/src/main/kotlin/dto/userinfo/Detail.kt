/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.userinfo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 勋章详细信息
 * 示例数据：
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Detail(
    /**
     * 用户ID
     */
    @JsonProperty("uid")
    val uid: Long? = null,
    /**
     * 勋章渐变结束颜色（十六进制），示例：#FFE2E9FE
     */
    @JsonProperty("medal_color_end")
    val medalColorEnd: String? = null,
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
     * 一级图标URL
     */
    @JsonProperty("first_icon")
    val firstIcon: String? = null,
    /**
     * 二级图标URL，示例：
     */
    @JsonProperty("second_icon")
    val secondIcon: String? = null,
    /**
     * 勋章等级颜色（十六进制），示例：#FF93A3E8
     */
    @JsonProperty("medal_color_level")
    val medalColorLevel: String? = null,
    /**
     * 勋章名称颜色（十六进制），示例：#FF5978C6
     */
    @JsonProperty("medal_color_name")
    val medalColorName: String? = null,
    /**
     * 勋章等级背景颜色，示例：0
     */
    @JsonProperty("medal_level_bg_color")
    val medalLevelBgColor: Int? = null,
    /**
     * 勋章名称，示例：养熊人
     */
    @JsonProperty("medal_name")
    val medalName: String? = null,
    /**
     * 勋章ID，示例：405013
     */
    @JsonProperty("medal_id")
    val medalId: Long? = null,
    /**
     * 勋章颜色（十六进制），示例：#FFE2E9FE
     */
    @JsonProperty("medal_color")
    val medalColor: String? = null,
    /**
     * 勋章边框颜色（十六进制），示例：#FFD0DCFF
     */
    @JsonProperty("medal_color_border")
    val medalColorBorder: String? = null
)