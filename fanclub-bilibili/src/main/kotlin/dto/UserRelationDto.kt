/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 用户关系数据
 */
data class UserRelationData(
    /**
     * 关注数
     */
    @JsonProperty("following")
    val following: Int = 0,
    /**
     * 被关注数 即 粉丝数
     */
    @JsonProperty("follower")
    val follower: Int = 0,
)

/**
 * 用户关系响应
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserRelationResponse(
    /**
     * 用户关系数据
     */
    @JsonProperty("data")
    val data: UserRelationData? = null
) : BiliBaseResponse()
