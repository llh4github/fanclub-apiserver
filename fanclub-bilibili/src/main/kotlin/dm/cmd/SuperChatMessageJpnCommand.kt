/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 日语醒目留言消息命令
 * 用于解析 "SUPER_CHAT_MESSAGE_JPN" 类型的命令
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SuperChatMessageJpnCommand(
    /**
     * 命令类型
     */
    override val cmd: String = "SUPER_CHAT_MESSAGE_JPN",

    /**
     * 日语醒目留言数据
     */
    @JsonProperty("data")
    val data: SuperChatMessageJpnData? = null
) : Command {

    /**
     * 日语醒目留言数据
     */
    data class SuperChatMessageJpnData(
        /**
         * 消息 ID
         */
        @JsonProperty("id")
        val id: Long? = null,

        /**
         * 用户 ID
         */
        @JsonProperty("uid")
        val uid: Long? = null,

        /**
         * 消息价格（元）
         */
        @JsonProperty("price")
        val price: Int? = null,

        /**
         * 汇率
         */
        @JsonProperty("rate")
        val rate: Int? = null,

        /**
         * 消息内容
         */
        @JsonProperty("message")
        val message: String? = null,

        /**
         * 日语消息内容
         */
        @JsonProperty("message_jpn")
        val messageJpn: String? = null,

        /**
         * 时间戳（秒）
         */
        @JsonProperty("ts")
        val ts: Long? = null,

        /**
         * Token
         */
        @JsonProperty("token")
        val token: String? = null,

        /**
         * 粉丝牌信息
         */
        @JsonProperty("medal_info")
        val medalInfo: MedalInfo? = null,

        /**
         * 持续时间（秒）
         */
        @JsonProperty("time")
        val time: Int? = null,

        /**
         * 开始时间
         */
        @JsonProperty("start_time")
        val startTime: Long? = null,

        /**
         * 结束时间
         */
        @JsonProperty("end_time")
        val endTime: Long? = null
    )

    /**
     * 粉丝牌信息
     */
    data class MedalInfo(
        /**
         * 主播的 UID
         */
        @JsonProperty("target_id")
        val targetId: Long? = null,

        /**
         * 主播房间号
         */
        @JsonProperty("anchor_roomid")
        val anchorRoomid: Long? = null,

        /**
         * 粉丝牌等级
         */
        @JsonProperty("medal_level")
        val medalLevel: Int? = null,

        /**
         * 粉丝牌名称
         */
        @JsonProperty("medal_name")
        val medalName: String? = null
    )
}
