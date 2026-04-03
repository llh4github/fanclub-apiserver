/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * 日语醒目留言消息命令
 */
@JsonTypeName("SUPER_CHAT_MESSAGE_JPN")
@JsonIgnoreProperties(ignoreUnknown = true)
data class SuperChatMessageJpnCommand(
    @field:JsonProperty("data")
    val data: SuperChatMessageJpnData? = null
) : Command() {

    /**
     * 日语醒目留言数据
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class SuperChatMessageJpnData(
        @field:JsonProperty("id")
        val id: Long? = null,

        @field:JsonProperty("uid")
        val uid: Long? = null,

        @field:JsonProperty("price")
        val price: Int? = null,

        @field:JsonProperty("rate")
        val rate: Int? = null,

        @field:JsonProperty("message")
        val message: String? = null,

        @field:JsonProperty("message_jpn")
        val messageJpn: String? = null,

        @field:JsonProperty("ts")
        val ts: Long? = null,

        @field:JsonProperty("token")
        val token: String? = null,

        @field:JsonProperty("medal_info")
        val medalInfo: MedalInfo? = null,

        @field:JsonProperty("time")
        val time: Int? = null,

        @field:JsonProperty("start_time")
        val startTime: Long? = null,

        @field:JsonProperty("end_time")
        val endTime: Long? = null
    )

    /**
     * 粉丝牌信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class MedalInfo(
        /**
         * 主播的uid
         */
        @field:JsonProperty("target_id")
        val targetId: Long? = null,

        @field:JsonProperty("anchor_roomid")
        val anchorRoomid: Long? = null,

        @field:JsonProperty("medal_level")
        val medalLevel: Int? = null,

        @field:JsonProperty("medal_name")
        val medalName: String? = null
    )
}
