/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.scraper

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.dm.cmd.Command

private val logger = KotlinLogging.logger {}

/**
 * B站 WebSocket 消息业务处理器接口
 * 使用策略模式分发命令到对应的处理器
 */
interface BiliWsMsgBizHandler {
    /**
     * 处理命令消息
     */
    fun handleMsg(cmd: Command, roomId: Long) {
        logger.warn { "未处理的命令类型: ${cmd::class.simpleName}, cmd=${cmd.cmd}" }
    }
}

/**
 * 默认的 B站 WebSocket 消息业务处理器实现
 * 使用 DanmuCommandDispatcher 进行命令分发
 */
class DefaultBiliWsMsgBizHandler(
    private val dispatcher: DanmuCommandDispatcher
) : BiliWsMsgBizHandler {
    override fun handleMsg(cmd: Command, roomId: Long) {
        dispatcher.dispatch(cmd, roomId)
    }
}
