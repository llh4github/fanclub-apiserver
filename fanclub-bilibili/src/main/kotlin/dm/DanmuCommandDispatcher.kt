/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.dm

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilibili.dm.cmd.Command
import kotlin.reflect.KClass

/**
 * 弹幕命令分发器
 * 根据命令类型将已解析的 Command 对象分发到对应的处理器
 * 
 * 设计目的：
 * 1. 在 BiliWebSocketClient 层完成 JSON 解析后，直接分发给强类型处理器
 * 2. 避免在上层业务代码中重复解析 JSON
 * 3. 利用类型安全，编译期检查命令类型
 * 
 * 工作原理：
 * 1. 初始化时构建 KClass 到处理器的映射表
 * 2. 收到 Command 对象时，根据其运行时类型查找对应的处理器
 * 3. 如果找不到匹配的处理器，记录警告日志
 * 
 * 使用示例：
 * ```kotlin
 * val handlers = listOf(
 *     DanmuMsgHandler(),      // 处理 DanmuMsgCommand
 *     SendGiftHandler(),      // 处理 SendGiftCommand
 *     GuardBuyHandler()       // 处理 GuardBuyCommand
 * )
 * val dispatcher = DanmuCommandDispatcher(handlers)
 * 
 * // 在 WebSocket 消息回调中使用
 * val command = CommandProcessor.parseCommand(json)
 * if (command != null) {
 *     dispatcher.dispatch(command, roomId)
 * }
 * ```
 */
class DanmuCommandDispatcher(
    handlers: List<DanmuCommandHandler<*>>
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 命令类型到处理器的映射表
     * key: 命令的 KClass（如 DanmuMsgCommand::class）
     * value: 对应的处理器实例
     */
    private val handlerMap: Map<KClass<out Command>, DanmuCommandHandler<*>> by lazy {
        handlers.associateBy { it.supportedCommand() }
    }

    init {
        logger.info { "弹幕命令分发器初始化完成: ${handlerMap.size} 个命令处理器" }
        logger.debug { "已注册的命令类型: ${handlerMap.keys.joinToString(", ")}" }
    }

    /**
     * 分发命令到对应的处理器
     * 
     * @param cmd 已解析的命令对象
     * @param roomId 直播间 ID
     */
    @Suppress("UNCHECKED_CAST")
    fun dispatch(cmd: Command, roomId: Long) {
        val handler = handlerMap[cmd::class]

        if (handler != null) {
            try {
                // 类型安全的转换：因为 handlerMap 的 key 和 value 是对应的
                (handler as DanmuCommandHandler<Command>).handle(cmd, roomId)
            } catch (e: Exception) {
                logger.error(e) { "处理命令失败: cmd=${cmd.cmd}, roomId=$roomId" }
            }
        } else {
            logger.debug { "未找到命令处理器: ${cmd::class.simpleName} (cmd=${cmd.cmd})" }
        }
    }
}
