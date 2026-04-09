/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.scraper

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.dm.cmd.Command

/**
 * 弹幕命令分发器
 * 根据命令类型分发到对应的处理器
 */
class DanmuCommandDispatcher(
    handlers: List<DanmuCommandHandler<*>>
) {
    private val logger = KotlinLogging.logger {}
    private val handlerMap = handlers.associateBy { it.supportedCommand() }

    /**
     * 分发命令到对应的处理器
     */
    @Suppress("UNCHECKED_CAST")
    fun dispatch(cmd: Command, roomId: Long) {
        val handler = handlerMap[cmd::class]
        if (handler != null) {
            (handler as DanmuCommandHandler<Command>).handle(cmd, roomId)
        } else {
            logger.warn { "未找到命令处理器: ${cmd::class.simpleName}" }
        }
    }
}
