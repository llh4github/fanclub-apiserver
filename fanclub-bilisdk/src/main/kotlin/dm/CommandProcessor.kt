/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dm

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.dm.cmd.Command
import llh.fanclubvup.bilisdk.dm.cmd.GuardBuyCommand
import llh.fanclubvup.bilisdk.scraper.BiliWsMsgBizHandler
import tools.jackson.module.kotlin.jacksonObjectMapper

object CommandProcessor {
    private val mapper = jacksonObjectMapper()

    private val logger = KotlinLogging.logger {}

    private val shouldIgnoreCommands = listOf(
        "WIDGET_BANNER", // 忽略横幅
        "WATCHED_CHANGE", // 忽略观看人数改变
        "NOTICE_MSG", // 忽略礼物公告
        "RANK_CHANGED_V2", // 忽略排行榜
        "DM_INTERACTION", // 忽略互动(xx人点赞)
        "INTERACT_WORD_V2",
        "LIVE_PANEL_CHANGE_CONTENT",
        "LIKE_INFO_V3_CLICK", // 点赞数据
        "LIKE_INFO_V3_UPDATE",
        "INTERACT_WORD",
        "ONLINE_RANK_V3",
        "COMMON_NOTICE_DANMAKU",
        "STOP_LIVE_ROOM_LIST",
        "RANK_CHANGED",
        "POPULAR_RANK_CHANGED",
    )

    fun parseCommand(json: String): Command? {
        return try {
            val tree = mapper.readTree(json)
            val cmd = tree.get("cmd")?.asString()?.split(":")[0] ?: return null
            if (shouldIgnoreCommands.contains(cmd)) {
                logger.debug { "应当忽略的命令不进行数据解析" }
                return null
            }

            val cmdType = CmdTypeMapEnums.getValues().firstOrNull {
                it.cmd == cmd
            }
            if (cmdType == null) {
                logger.warn { "找不到对应的命令类型: \n$json" }
                return null
            }
            if (cmdType.clazz == GuardBuyCommand::class) {
                // FIXME 调试后删除
                logger.info { "舰长购买消息的原始json: \n$json" }
            }

            return mapper.treeToValue(tree, cmdType.clazz.java)
        } catch (e: Exception) {
            logger.error(e) { "解析命令失败: $json" }
            null
        }
    }

}
