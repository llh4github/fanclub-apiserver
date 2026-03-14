/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.userinfo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 用户基本信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserInfoData(
    /**
     * 用户ID
     */
    @JsonProperty("mid")
    val mid: Long? = null,
    /**
     * 用户名
     */
    @JsonProperty("name")
    val name: String? = null,
    /**
     * 性别，示例：女
     */
    @JsonProperty("sex")
    val sex: String? = null,
    /**
     * 头像URL
     */
    @JsonProperty("face")
    val face: String? = null,
    /**
     * NFT头像标识，0表示非NFT头像，示例：0
     */
    @JsonProperty("face_nft")
    val faceNft: Int? = null,
    /**
     * NFT头像类型，示例：0
     */
    @JsonProperty("face_nft_type")
    val faceNftType: Int? = null,
    /**
     * 个人签名
     */
    @JsonProperty("sign")
    val sign: String? = null,
    /**
     * 排名，示例：10000
     */
    @JsonProperty("rank")
    val rank: Int? = null,
    /**
     * 用户等级，示例：6
     */
    @JsonProperty("level")
    val level: Int? = null,
    /**
     * 加入时间，示例：0
     */
    @JsonProperty("jointime")
    val jointime: Int? = null,
    /**
     * 道德值，示例：0
     */
    @JsonProperty("moral")
    val moral: Int? = null,
    /**
     * 禁言状态，0表示未禁言，示例：0
     */
    @JsonProperty("silence")
    val silence: Int? = null,
    /**
     * 控制状态，示例：0
     */
    @JsonProperty("control")
    val control: Int? = null,
    /**
     * 硬币数，示例：0
     *
     * 看不到别人的数据
     */
    @JsonProperty("coins")
    val coins: Int? = null,
    /**
     * 是否有粉丝勋章，示例：true
     */
    @JsonProperty("fans_badge")
    val fansBadge: Boolean? = null,
    /**
     * 粉丝勋章信息
     */
    @JsonProperty("fans_medal")
    val fansMedal: FansMedal? = null,
    /**
     * 官方认证信息
     */
    @JsonProperty("official")
    val official: Official? = null,
    /**
     * 会员信息
     */
    @JsonProperty("vip")
    val vip: Vip? = null
)