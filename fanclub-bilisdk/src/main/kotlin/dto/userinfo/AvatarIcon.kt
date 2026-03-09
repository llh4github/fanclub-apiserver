/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.userinfo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 头像图标信息
 * 示例数据：
 * {
 *   "icon_type": 1,
 *   "icon_resource": {}
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AvatarIcon(
    /**
     * 图标类型，示例：1
     */
    @JsonProperty("icon_type")
    val iconType: Int? = null,
    /**
     * 图标资源，示例：{}
     */
    @JsonProperty("icon_resource")
    val iconResource: Map<String, Any>? = null
)