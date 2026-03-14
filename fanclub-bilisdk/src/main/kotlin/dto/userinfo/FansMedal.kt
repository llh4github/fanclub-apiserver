/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.userinfo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 粉丝勋章信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class FansMedal(
    /**
     * 是否显示勋章，示例：true
     */
    @JsonProperty("show")
    val show: Boolean? = null,
    /**
     * 是否佩戴勋章，示例：true
     */
    @JsonProperty("wear")
    val wear: Boolean? = null,
    /**
     * 勋章基本信息
     */
    @JsonProperty("medal")
    val medal: Medal? = null,
    /**
     * 勋章详细信息
     */
    @JsonProperty("detail")
    val detail: Detail? = null
)