/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.dm

import llh.fanclubvup.dm.cmd.Command
import llh.fanclubvup.dm.cmd.DanmakuCommand
import llh.fanclubvup.dm.cmd.SendGiftCommand
import llh.fanclubvup.dm.cmd.UnknownCommand
import tools.jackson.module.kotlin.jacksonObjectMapper

object CommandProcessor {
    private val mapper = jacksonObjectMapper()

    fun parseCommand(json: String): Command? {
        return try {
            val tree = mapper.readTree(json)
            val cmd = tree.get("cmd")?.asString() ?: return null

            return when {
                cmd == "SEND_GIFT" -> mapper.readValue(json, SendGiftCommand::class.java)
                cmd.startsWith("DANMU_MSG") -> mapper.readValue(json, DanmakuCommand::class.java)
                else -> UnknownCommand(
                    cmd = cmd,
//                    rawData = tree.fields().asSequence().associate { it.key to it.value }
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    // 使用安全的类型转换
    inline fun <reified T : Command> parseCommandAs(json: String): T? {
        return parseCommand(json) as? T
    }
}
