/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * 大航海购买消息命令
 *
 * 这个要发送很多条，注意去重
 *
 * 感觉用 [UserToastMsgV2Cmd] 记录要好
 */
@JsonTypeName("GUARD_BUY")
@JsonIgnoreProperties(ignoreUnknown = true)
data class GuardBuyCommand(
    @field:JsonProperty("data")
    val data: GuardBuyData? = null
) : Command() {

    /**
     * 大航海购买数据
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GuardBuyData(
        @field:JsonProperty("uid")
        val uid: Long? = null,

        @field:JsonProperty("username")
        val username: String? = null,

        @field:JsonProperty("guard_level")
        val guardLevel: Int? = null,

        @field:JsonProperty("num")
        val num: Int? = null,

        @field:JsonProperty("price")
        val price: Long? = null,

        @field:JsonProperty("gift_id")
        val giftId: Long? = null,

        @field:JsonProperty("gift_name")
        val giftName: String? = null,

        @field:JsonProperty("start_time")
        val startTime: Long? = null,

        @field:JsonProperty("end_time")
        val endTime: Long? = null
    )
}
