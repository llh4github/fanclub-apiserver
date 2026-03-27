/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * 直播开始命令
 */
@JsonTypeName("LIVE")
@JsonIgnoreProperties(ignoreUnknown = true)
data class LiveCommand(
    @field:JsonProperty("live_key")
    val liveKey: String? = null,

    @field:JsonProperty("voice_background")
    val voiceBackground: String? = null,

    @field:JsonProperty("sub_session_key")
    val subSessionKey: String? = null,

    @field:JsonProperty("live_platform")
    val livePlatform: String? = null,

    @field:JsonProperty("live_model")
    val liveModel: Int? = null,

    @field:JsonProperty("roomid")
    val roomId: Long? = null,

    @field:JsonProperty("live_time")
    val liveTime: Long? = null,

    @field:JsonProperty("special_types")
    val specialTypes: List<String>? = null
) : Command()
