/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.dm.handlers

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilibili.dm.DanmuCommandHandler
import llh.fanclubvup.bilibili.dm.cmd.DanmuMsgCommand
import llh.fanclubvup.bilibili.dm.cmd.GuardBuyCommand
import llh.fanclubvup.bilibili.dm.cmd.SendGiftCommand
import kotlin.reflect.KClass

/**
 * 弹幕消息处理器示例
 * 处理 DanmuMsgCommand 类型的弹幕消息
 * 
 * 这是一个示例实现，展示如何实现 DanmuCommandHandler 接口
 * 与处理原始 DanmuMessage 不同，这里直接操作强类型的 Command 对象
 */
class DanmuMsgHandler : DanmuCommandHandler<DanmuMsgCommand> {
    private val logger = KotlinLogging.logger {}

    override fun handle(cmd: DanmuMsgCommand, roomId: Long) {
        // 直接访问 cmd 对象的字段，无需手动解析 JSON
        val info = cmd.info
        if (info != null && info.size > 1) {
            val userName = (info[1] as? List<*>)?.getOrNull(1)?.toString() ?: "未知用户"
            val content = info.getOrNull(1)?.toString() ?: ""
            logger.info { "[$roomId] 收到弹幕 - $userName: $content" }
        }
        // 在这里添加具体的业务逻辑
        // 例如：存储到数据库、触发事件、统计分析等
    }

    override fun supportedCommand(): KClass<DanmuMsgCommand> {
        return DanmuMsgCommand::class
    }
}

/**
 * 礼物消息处理器示例
 * 处理 SendGiftCommand 类型的礼物消息
 */
class SendGiftHandler : DanmuCommandHandler<SendGiftCommand> {
    private val logger = KotlinLogging.logger {}

    override fun handle(cmd: SendGiftCommand, roomId: Long) {
        val data = cmd.data
        if (data != null) {
            logger.info { "[$roomId] 收到礼物 - ${data.uname}: ${data.giftName} x ${data.num}" }
            // 在这里添加礼物处理逻辑
            // 例如：统计礼物数量、更新排行榜、发送感谢消息等
        }
    }

    override fun supportedCommand(): KClass<SendGiftCommand> {
        return SendGiftCommand::class
    }
}

/**
 * 舰长消息处理器示例
 * 处理 GuardBuyCommand 类型的舰长购买消息
 */
class GuardBuyHandler : DanmuCommandHandler<GuardBuyCommand> {
    private val logger = KotlinLogging.logger {}

    override fun handle(cmd: GuardBuyCommand, roomId: Long) {
        val data = cmd.data
        if (data != null) {
            logger.info { "[$roomId] 有人上舰 - ${data.username}: ${data.guardLevel} 级舰长" }
            // 在这里添加舰长处理逻辑
            // 例如：记录舰长信息、发送欢迎消息、更新舰长列表等
        }
    }

    override fun supportedCommand(): KClass<GuardBuyCommand> {
        return GuardBuyCommand::class
    }
}
