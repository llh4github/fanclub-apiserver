/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.dm

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilibili.dm.cmd.Command
import llh.fanclubvup.bilibili.utils.JsonUtils

/**
 * 命令处理器
 * 用于解析和处理弹幕命令
 */
object CommandProcessor {
    private val logger = KotlinLogging.logger {}

    /**
     * 应当忽略的命令类型
     */
    private val shouldIgnoreCommands = setOf(
        "ENTRY_EFFECT", // 进场通知
        "USER_TOAST_MSG", // 用户开通大航海，现在用v2版
        "WIDGET_BANNER", "WATCHED_CHANGE", "NOTICE_MSG",
        "RANK_CHANGED_V2", "DM_INTERACTION", "INTERACT_WORD_V2",
        "LIVE_PANEL_CHANGE_CONTENT", "LIKE_INFO_V3_CLICK",
        "LIKE_INFO_V3_UPDATE", "INTERACT_WORD", "ONLINE_RANK_V3",
        "TRADING_SCORE", "COMMON_NOTICE_DANMAKU", "STOP_LIVE_ROOM_LIST",
        "RANK_CHANGED", "POPULAR_RANK_CHANGED", "_HEARTBEAT"
    )

    /**
     * 命令类型映射，使用 Map 替代线性查找，O(1) 时间复杂度
     */
    private val commandTypeMap = CmdTypeMapEnums.commandTypeMap

    /**
     * 解析命令
     * 
     * @param json 命令 JSON 字符串
     * @return 解析后的命令对象
     */
    fun parseCommand(json: String): Command? {
        return try {
            val tree = JsonUtils.mapper.readTree(json)
            val cmd = tree.get("cmd")?.asString()?.split(":")?.get(0) ?: return null

            if (shouldIgnoreCommands.contains(cmd)) {
                logger.debug { "应当忽略的命令不进行数据解析: $cmd" }
                return null
            }

            val cmdType = commandTypeMap[cmd]
            if (cmdType == null) {
                logger.warn { "找不到对应的命令类型: $cmd,json:\n$json" }
                return null
            }

            return JsonUtils.mapper.treeToValue(tree, cmdType.clazz.java)
        } catch (e: Exception) {
            logger.error(e) { "解析命令失败: $json" }
            null
        }
    }
}
