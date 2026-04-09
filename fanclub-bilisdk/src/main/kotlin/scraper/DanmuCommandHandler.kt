/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.scraper

import llh.fanclubvup.bilisdk.dm.cmd.Command
import kotlin.reflect.KClass

/**
 * 弹幕命令处理器接口
 * 用于处理特定类型的弹幕命令
 */
interface DanmuCommandHandler<T : Command> {
    /**
     * 处理命令
     */
    fun handle(cmd: T, roomId: Long)

    /**
     * 支持的命令类型
     */
    fun supportedCommand(): KClass<T>
}
