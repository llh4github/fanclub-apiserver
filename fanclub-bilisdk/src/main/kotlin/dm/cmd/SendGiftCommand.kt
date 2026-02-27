/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SendGiftCommand(
    @field:JsonProperty("uid") val uid: Long,
    @field:JsonProperty("uname") val username: String,
    @field:JsonProperty("gift_name") val giftName: String,
    @field:JsonProperty("num") val num: Int,
    @field:JsonProperty("price") val price: Int,
    @JsonProperty("cmd") override val cmd: String = "SEND_GIFT",
    @field:JsonProperty("coin_type") val coinType: String? = null,
    @field:JsonProperty("gift_id") val giftId: Long? = null
) : Command()
