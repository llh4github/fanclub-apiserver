/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 直播准备命令
 * 用于解析 "PREPARING" 类型的命令
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PreparingCommand(
    /**
     * 命令类型
     */
    override val cmd: String = "PREPARING",

    /**
     * 消息 ID
     */
    @field:JsonProperty("msg_id")
    val msgId: String? = null,

    /**
     * 是否需要确认
     */
    @field:JsonProperty("p_is_ack")
    val pIsAck: Boolean? = null,

    /**
     * 消息类型
     */
    @field:JsonProperty("p_msg_type")
    val pMsgType: Int? = null,

    /**
     * 房间 ID
     */
    @field:JsonProperty("roomid")
    val roomId: Long? = null,

    /**
     * 发送时间 毫秒
     */
    @field:JsonProperty("send_time")
    val sendTime: Long? = null
) : Command
