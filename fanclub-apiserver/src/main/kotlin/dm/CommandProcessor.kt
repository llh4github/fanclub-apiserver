/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.dm

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.dm.cmd.Command
import llh.fanclubvup.apiserver.dm.cmd.DanmakuCommand
import llh.fanclubvup.apiserver.dm.cmd.SendGiftCommand
import llh.fanclubvup.apiserver.dm.cmd.UnknownCommand

import tools.jackson.module.kotlin.jacksonObjectMapper

object CommandProcessor {
    private val mapper = jacksonObjectMapper()
    private val logger = KotlinLogging.logger {}

    fun parseCommand(json: String): Command? {
        return try {
            val tree = mapper.readTree(json)
            val cmd = tree.get("cmd")?.asString() ?: return null

            return when {
                cmd == "SEND_GIFT" -> mapper.readValue(json, SendGiftCommand::class.java)
                cmd.startsWith("DANMU_MSG") -> mapper.readValue(json, DanmakuCommand::class.java)
                else -> UnknownCommand(
                    cmd = cmd,
                    rawData = tree.properties().associate { it.key to it.value }
                )
            }
        } catch (e: Exception) {
            logger.error(e) { "解析命令失败: $json" }
            null
        }
    }

    // 使用安全的类型转换
    inline fun <reified T : Command> parseCommandAs(json: String): T? {
        return parseCommand(json) as? T
    }
}
