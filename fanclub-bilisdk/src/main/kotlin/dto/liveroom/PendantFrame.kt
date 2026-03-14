/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.liveroom

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 头像框
 */
data class PendantFrame(
    @JsonProperty("name")
    val name: String,
    
    @JsonProperty("value")
    val value: String,
    
    @JsonProperty("position")
    val position: Int,
    
    @JsonProperty("desc")
    val desc: String,
    
    @JsonProperty("area")
    val area: Int,
    
    @JsonProperty("area_old")
    val areaOld: Int,
    
    @JsonProperty("bg_color")
    val bgColor: String,
    
    @JsonProperty("bg_pic")
    val bgPic: String,
    
    @JsonProperty("use_old_area")
    val useOldArea: Boolean
)
