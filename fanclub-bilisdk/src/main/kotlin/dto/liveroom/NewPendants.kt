/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.liveroom

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 新挂件信息
 */
data class NewPendants(
    @JsonProperty("frame")
    val frame: PendantFrame? = null,  // 头像框
    
    @JsonProperty("badge")
    val badge: PendantBadge? = null,  // 徽章
    
    @JsonProperty("mobile_frame")
    val mobileFrame: PendantFrame? = null,  // 移动端头像框
    
    @JsonProperty("mobile_badge")
    val mobileBadge: PendantBadge? = null  // 移动端徽章
)
