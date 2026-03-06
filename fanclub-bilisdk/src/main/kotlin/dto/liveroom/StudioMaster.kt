/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.liveroom

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 演播室主播
 */
data class StudioMaster(
    @JsonProperty("uid")
    val uid: Long,  // 主播 UID
    
    @JsonProperty("uname")
    val uname: String,  // 主播昵称
    
    @JsonProperty("face")
    val face: String  // 头像
)
