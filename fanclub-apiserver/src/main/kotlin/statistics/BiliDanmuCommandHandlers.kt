/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.statistics

import llh.fanclubvup.bilisdk.dm.cmd.*
import llh.fanclubvup.bilisdk.scraper.DanmuCommandHandler
import org.springframework.stereotype.Component

/**
 * 大航海 V2 命令处理器
 */
@Component
class UserToastMsgV2Handler(
    private val statistics: BiliDanmuStatistics
) : DanmuCommandHandler<UserToastMsgV2Cmd> {
    override fun handle(cmd: UserToastMsgV2Cmd, roomId: Long) {
        statistics.handleMsg(cmd, roomId)
    }

    override fun supportedCommand() = UserToastMsgV2Cmd::class
}

/**
 * 醒目留言（日语）命令处理器
 */
@Component
class SuperChatMessageJpnHandler(
    private val statistics: BiliDanmuStatistics
) : DanmuCommandHandler<SuperChatMessageJpnCommand> {
    override fun handle(cmd: SuperChatMessageJpnCommand, roomId: Long) {
        statistics.handleMsg(cmd, roomId)
    }

    override fun supportedCommand() = SuperChatMessageJpnCommand::class
}

/**
 * 醒目留言命令处理器
 */
@Component
class SuperChatHandler(
    private val statistics: BiliDanmuStatistics
) : DanmuCommandHandler<SuperChatCommand> {
    override fun handle(cmd: SuperChatCommand, roomId: Long) {
        statistics.handleMsg(cmd, roomId)
    }

    override fun supportedCommand() = SuperChatCommand::class
}

/**
 * 送礼命令处理器
 */
@Component
class SendGiftHandler(
    private val statistics: BiliDanmuStatistics
) : DanmuCommandHandler<SendGiftCommand> {
    override fun handle(cmd: SendGiftCommand, roomId: Long) {
        statistics.handleMsg(cmd, roomId)
    }

    override fun supportedCommand() = SendGiftCommand::class
}

/**
 * 直播准备中命令处理器
 */
@Component
class PreparingHandler(
    private val statistics: BiliDanmuStatistics
) : DanmuCommandHandler<PreparingCommand> {
    override fun handle(cmd: PreparingCommand, roomId: Long) {
        statistics.handleMsg(cmd, roomId)
    }

    override fun supportedCommand() = PreparingCommand::class
}

/**
 * 直播开始命令处理器
 */
@Component
class LiveHandler(
    private val statistics: BiliDanmuStatistics
) : DanmuCommandHandler<LiveCommand> {
    override fun handle(cmd: LiveCommand, roomId: Long) {
        statistics.handleMsg(cmd, roomId)
    }

    override fun supportedCommand() = LiveCommand::class
}

/**
 * 弹幕消息命令处理器
 */
@Component
class DanmuMsgHandler(
    private val statistics: BiliDanmuStatistics
) : DanmuCommandHandler<DanmuMsgCommand> {
    override fun handle(cmd: DanmuMsgCommand, roomId: Long) {
        statistics.handleMsg(cmd, roomId)
    }

    override fun supportedCommand() = DanmuMsgCommand::class
}
