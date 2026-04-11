/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * 弹幕命令接口
 * 所有具体的弹幕命令都实现此接口
 */
@JsonIgnoreProperties(ignoreUnknown = true)
interface Command {
    /**
     * 命令类型
     */
    val cmd: String
}
