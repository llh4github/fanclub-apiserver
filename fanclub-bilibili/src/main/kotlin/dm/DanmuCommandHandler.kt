/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.dm

import llh.fanclubvup.bilibili.dm.cmd.Command
import kotlin.reflect.KClass

/**
 * 弹幕命令处理器接口
 * 用于处理特定类型的弹幕命令（已经解析为强类型对象）
 * 
 * 设计目的：
 * 1. 将复杂的弹幕处理逻辑拆分为多个独立的处理器
 * 2. 每个处理器专注于特定类型的命令（如 DanmuMsgCommand、SendGiftCommand）
 * 3. 利用 Kotlin 的类型系统，避免手动解析 JSON
 * 4. 提高代码的可维护性和可扩展性
 * 
 * 与 DanmuMessageHandler 的区别：
 * - DanmuMessageHandler: 处理原始的 DanmuMessage（包含 cmd 和 rawData）
 * - DanmuCommandHandler: 处理已解析的 Command 对象（强类型，可直接访问字段）
 * 
 * 使用示例：
 * ```kotlin
 * class DanmuMsgHandler : DanmuCommandHandler<DanmuMsgCommand> {
 *     override fun handle(cmd: DanmuMsgCommand, roomId: Long) {
 *         // 直接访问 cmd.info 等字段，无需手动解析 JSON
 *         println("收到弹幕: ${cmd.info?.get(1)}")
 *     }
 *     
 *     override fun supportedCommand() = DanmuMsgCommand::class
 * }
 * ```
 */
interface DanmuCommandHandler<T : Command> {
    /**
     * 处理命令
     * 
     * @param cmd 已解析的命令对象，可以直接访问其字段
     * @param roomId 直播间 ID，用于区分不同房间的消息
     */
    fun handle(cmd: T, roomId: Long)

    /**
     * 支持的命令类型
     * 
     * 返回此处理器能够处理的命令类型的 KClass
     * 分发器会根据此值将命令路由到对应的处理器
     * 
     * @return 命令类型的 KClass
     */
    fun supportedCommand(): KClass<T>
}
