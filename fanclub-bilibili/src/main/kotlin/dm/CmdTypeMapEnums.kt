/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.dm

import llh.fanclubvup.bilibili.dm.cmd.*
import kotlin.reflect.KClass

/**
 * 命令类型映射枚举
 * 用于将命令类型映射到具体的命令类
 */
enum class CmdTypeMapEnums(val cmd: String, val clazz: KClass<out Command>) {
    /**
     * 弹幕消息
     */
    DANMU_MSG("DANMU_MSG", DanmuMsgCommand::class),
    
    /**
     * 礼物消息
     */
    SEND_GIFT("SEND_GIFT", SendGiftCommand::class),
    
    /**
     * 超级留言
     */
    SUPER_CHAT_MESSAGE("SUPER_CHAT_MESSAGE", SuperChatCommand::class),
    
    /**
     * 舰长购买
     */
    GUARD_BUY("GUARD_BUY", GuardBuyCommand::class),
    
    /**
     * 直播开始
     */
    LIVE("LIVE", LiveCommand::class),
    
    /**
     * 直播准备
     */
    PREPARING("PREPARING", PreparingCommand::class)
    ;
    
    companion object {
        /**
         * 获取所有枚举值
         */
        fun getValues(): List<CmdTypeMapEnums> {
            return entries
        }
    }
}
