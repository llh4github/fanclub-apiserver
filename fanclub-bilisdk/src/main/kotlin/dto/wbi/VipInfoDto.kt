/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.wbi

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * VIP 详细信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class VipInfoDto(
    @JsonProperty("type")
    val type: Int = 0,
    
    @JsonProperty("status")
    val status: Int = 0,
    
    @JsonProperty("due_date")
    val dueDate: Long = 0,
    
    @JsonProperty("vip_pay_type")
    val vipPayType: Int = 0,
    
    @JsonProperty("theme_type")
    val themeType: Int = 0,
    
    @JsonProperty("label")
    val label: VipLabelDto? = null,
    
    @JsonProperty("avatar_subscript")
    val avatarSubscript: Int = 0,
    
    @JsonProperty("nickname_color")
    val nicknameColor: String = "",
    
    @JsonProperty("role")
    val role: Int = 0,
    
    @JsonProperty("avatar_subscript_url")
    val avatarSubscriptUrl: String = "",
    
    @JsonProperty("tv_vip_status")
    val tvVipStatus: Int = 0,
    
    @JsonProperty("tv_vip_pay_type")
    val tvVipPayType: Int = 0,
    
    @JsonProperty("tv_due_date")
    val tvDueDate: Long = 0,
    
    @JsonProperty("avatar_icon")
    val avatarIcon: AvatarIconDto? = null,
    
    @JsonProperty("ott_info")
    val ottInfo: OttInfoDto? = null,
    
    @JsonProperty("super_vip")
    val superVip: SuperVipDto? = null
)

/**
 * 头像图标信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AvatarIconDto(
    @JsonProperty("icon_type")
    val iconType: Int = 0,
    
    @JsonProperty("icon_resource")
    val iconResource: Map<String, Any>? = null
)

/**
 * OTT 信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OttInfoDto(
    @JsonProperty("vip_type")
    val vipType: Int = 0,
    
    @JsonProperty("pay_type")
    val payType: Int = 0,
    
    @JsonProperty("pay_channel_id")
    val payChannelId: String = "",
    
    @JsonProperty("status")
    val status: Int = 0,
    
    @JsonProperty("overdue_time")
    val overdueTime: Long = 0
)

/**
 * 超级 VIP 信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SuperVipDto(
    @JsonProperty("is_super_vip")
    val isSuperVip: Boolean = false
)
