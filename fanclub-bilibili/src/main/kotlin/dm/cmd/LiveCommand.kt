/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.dm.cmd

/**
 * 直播开始命令
 * 用于解析 "LIVE" 类型的命令
 */
data class LiveCommand(
    /**
     * 命令类型
     */
    override val cmd: String = "LIVE",
    
    /**
     * 直播密钥
     */
    val liveKey: String? = null,
    
    /**
     * 语音背景
     */
    val voiceBackground: String? = null,
    
    /**
     * 子会话密钥
     */
    val subSessionKey: String? = null,
    
    /**
     * 直播平台
     */
    val livePlatform: String? = null,
    
    /**
     * 直播模式
     */
    val liveModel: Int? = null,
    
    /**
     * 房间 ID
     */
    val roomid: Long? = null,
    
    /**
     * 直播开始时间 (秒)
     */
    val liveTime: Long? = null,
    
    /**
     * 特殊类型
     */
    val specialTypes: List<String>? = null
) : Command
