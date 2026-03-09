/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.userinfo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 会员信息
 * 示例数据：
 * {
 *   "type": 2,
 *   "status": 1,
 *   "due_date": 1906646400000,
 *   "vip_pay_type": 0,
 *   "theme_type": 0,
 *   "nickname_color": "#FB7299",
 *   "role": 3
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Vip(
    /**
     * 会员类型，示例：2
     */
    @JsonProperty("type")
    val type: Int? = null,
    /**
     * 会员状态，1表示有效，示例：1
     */
    @JsonProperty("status")
    val status: Int? = null,
    /**
     * 到期时间戳，示例：1906646400000
     */
    @JsonProperty("due_date")
    val dueDate: Long? = null,
    /**
     * 会员支付类型，示例：0
     */
    @JsonProperty("vip_pay_type")
    val vipPayType: Int? = null,
    /**
     * 主题类型，示例：0
     */
    @JsonProperty("theme_type")
    val themeType: Int? = null,
    /**
     * 会员标签信息
     */
    @JsonProperty("label")
    val label: Label? = null,
    /**
     * 头像下标，示例：1
     */
    @JsonProperty("avatar_subscript")
    val avatarSubscript: Int? = null,
    /**
     * 昵称颜色（十六进制），示例：#FB7299
     */
    @JsonProperty("nickname_color")
    val nicknameColor: String? = null,
    /**
     * 角色，示例：3
     */
    @JsonProperty("role")
    val role: Int? = null,
    /**
     * 头像下标URL，示例：
     */
    @JsonProperty("avatar_subscript_url")
    val avatarSubscriptUrl: String? = null,
    /**
     * TV会员状态，示例：0
     */
    @JsonProperty("tv_vip_status")
    val tvVipStatus: Int? = null,
    /**
     * TV会员支付类型，示例：0
     */
    @JsonProperty("tv_vip_pay_type")
    val tvVipPayType: Int? = null,
    /**
     * TV会员到期时间戳，示例：1744646400
     */
    @JsonProperty("tv_due_date")
    val tvDueDate: Long? = null,
    /**
     * 头像图标信息
     */
    @JsonProperty("avatar_icon")
    val avatarIcon: AvatarIcon? = null,
    /**
     * OTT会员信息
     */
    @JsonProperty("ott_info")
    val ottInfo: OttInfo? = null
)