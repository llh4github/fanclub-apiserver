/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.wbi

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 用户钱包信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class WalletDto(
    @JsonProperty("mid")
    val mid: Long,
    
    @JsonProperty("bcoin_balance")
    val bcoinBalance: Int = 0,
    
    @JsonProperty("coupon_balance")
    val couponBalance: Int = 0,
    
    @JsonProperty("coupon_due_time")
    val couponDueTime: Long = 0
)
