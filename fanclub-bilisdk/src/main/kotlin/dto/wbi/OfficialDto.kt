/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.wbi

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 官方认证信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OfficialDto(
    @JsonProperty("role")
    val role: Int = 0,
    
    @JsonProperty("title")
    val title: String = "",
    
    @JsonProperty("desc")
    val desc: String = "",
    
    @JsonProperty("type")
    val type: Int = -1
)

/**
 * 官方验证信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OfficialVerifyDto(
    @JsonProperty("type")
    val type: Int = -1,
    
    @JsonProperty("desc")
    val desc: String = ""
)
