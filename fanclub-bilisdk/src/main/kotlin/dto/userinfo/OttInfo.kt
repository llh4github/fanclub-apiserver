/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.userinfo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * OTT会员信息
 * 示例数据：
 * {
 *   "vip_type": 1,
 *   "pay_type": 0,
 *   "pay_channel_id": ...
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OttInfo(
    /**
     * OTT会员类型，示例：1
     */
    @JsonProperty("vip_type")
    val vipType: Int? = null,
    /**
     * OTT会员支付类型，示例：0
     */
    @JsonProperty("pay_type")
    val payType: Int? = null,
    /**
     * 支付渠道ID，示例：...
     */
    @JsonProperty("pay_channel_id")
    val payChannelId: Int? = null
)