/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.liveroom

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 徽章
 */
data class PendantBadge(
    @JsonProperty("name")
    val name: String,
    
    @JsonProperty("position")
    val position: Int,
    
    @JsonProperty("value")
    val value: String,
    
    @JsonProperty("desc")
    val desc: String
)
