/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * 直播间准备中命令
 */
@JsonTypeName("PREPARING")
@JsonIgnoreProperties(ignoreUnknown = true)
data class PreparingCommand(
    @field:JsonProperty("msg_id")
    val msgId: String? = null,

    @field:JsonProperty("p_is_ack")
    val pIsAck: Boolean? = null,

    @field:JsonProperty("p_msg_type")
    val pMsgType: Int? = null,

    @field:JsonProperty("roomid")
    val roomId: Long? = null,

    /**
     * 发送时间 毫秒
     */
    @field:JsonProperty("send_time")
    val sendTime: Long? = null
) : Command()