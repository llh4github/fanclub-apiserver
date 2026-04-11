/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 直播开始命令
 * 用于解析 "LIVE" 类型的命令
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class LiveCommand(
    /**
     * 命令类型
     */
    override val cmd: String = "LIVE",
    
    /**
     * 直播密钥
     */
    @JsonProperty("live_key")
    val liveKey: String? = null,
    
    /**
     * 语音背景
     */
    @JsonProperty("voice_background")
    val voiceBackground: String? = null,
    
    /**
     * 子会话密钥
     */
    @JsonProperty("sub_session_key")
    val subSessionKey: String? = null,
    
    /**
     * 直播平台
     */
    @JsonProperty("live_platform")
    val livePlatform: String? = null,
    
    /**
     * 直播模式
     */
    @JsonProperty("live_model")
    val liveModel: Int? = null,
    
    /**
     * 房间 ID
     */
    val roomid: Long? = null,
    
    /**
     * 直播开始时间 (秒)
     */
    @JsonProperty("live_time")
    val liveTime: Long? = null,
    
    /**
     * 特殊类型
     */
    @JsonProperty("special_types")
    val specialTypes: List<String>? = null
) : Command
