/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.dm.cmd

/**
 * 直播准备命令
 * 用于解析 "PREPARING" 类型的命令
 */
data class PreparingCommand(
    /**
     * 命令类型
     */
    override val cmd: String = "PREPARING",
    
    /**
     * 消息 ID
     */
    val msgId: String? = null,
    
    /**
     * 是否需要确认
     */
    val pIsAck: Boolean? = null,
    
    /**
     * 消息类型
     */
    val pMsgType: Int? = null,
    
    /**
     * 房间 ID
     */
    val roomid: Long? = null,
    
    /**
     * 发送时间 毫秒
     */
    val sendTime: Long? = null
) : Command
