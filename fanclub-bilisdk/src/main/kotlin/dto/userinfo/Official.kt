/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.userinfo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 官方认证信息
 * 示例数据：
 * {
 *   "role": 7,
 *   "title": "bilibili 直播高能主播",
 *   "desc": "",
 *   "type": 0
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Official(
    /**
     * 角色类型，示例：7
     */
    @JsonProperty("role")
    val role: Int? = null,
    /**
     * 认证标题，示例：bilibili 直播高能主播
     */
    @JsonProperty("title")
    val title: String? = null,
    /**
     * 认证描述，示例：
     */
    @JsonProperty("desc")
    val desc: String? = null,
    /**
     * 认证类型，示例：0
     */
    @JsonProperty("type")
    val type: Int? = null
)