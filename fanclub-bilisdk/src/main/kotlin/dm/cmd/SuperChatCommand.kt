/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * 醒目留言消息命令
 */
@JsonTypeName("SUPER_CHAT_MESSAGE")
@JsonIgnoreProperties(ignoreUnknown = true)
data class SuperChatCommand(
    @field:JsonProperty("data")
    val data: SuperChatData? = null,

    @field:JsonProperty("send_time")
    val sendTime: Long? = null
) : Command() {

    /**
     * 醒目留言数据
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class SuperChatData(
        @field:JsonProperty("id")
        val id: Long? = null,
        @field:JsonProperty("message")
        val message: String? = null,

//        @field:JsonProperty("message_trans")
//        val messageTrans: String? = null,

        @field:JsonProperty("price")
        val price: Int? = null,

        @field:JsonProperty("rate")
        val rate: Int? = null,

        @field:JsonProperty("ts")
        val ts: Long? = null,

        @field:JsonProperty("uid")
        val uid: Long? = null,

        @field:JsonProperty("uinfo")
        val uinfo: UserInfo? = null,

        @field:JsonProperty("user_info")
        val userInfo: UserSimpleInfo? = null,

        /** 毫秒 */
        @field:JsonProperty("send_time")
        val sendTime: Long?
    )

    /**
     * 用户详细信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class UserInfo(
        @field:JsonProperty("base")
        val base: UserBase? = null,

        @field:JsonProperty("guard")
        val guard: GuardInfo? = null,

        @field:JsonProperty("uid")
        val uid: Long? = null,

        @field:JsonProperty("wealth")
        val wealth: Any? = null
    )

    /**
     * 用户基础信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class UserBase(
        @field:JsonProperty("face")
        val face: String? = null,

        @field:JsonProperty("name")
        val name: String? = null,

        @field:JsonProperty("name_color")
        val nameColor: Int? = null
    )

    /**
     * 大航海信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GuardInfo(
        @field:JsonProperty("expired_str")
        val expiredStr: String? = null,

        @field:JsonProperty("level")
        val level: Int? = null
    )

    /**
     * 用户简单信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class UserSimpleInfo(
        @field:JsonProperty("guard_level")
        val guardLevel: Int? = null,

        @field:JsonProperty("title")
        val title: String? = null,

        @field:JsonProperty("uname")
        val uname: String? = null,

        @field:JsonProperty("user_level")
        val userLevel: Int? = null
    )
}
