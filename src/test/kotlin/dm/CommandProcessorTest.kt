package llh.fanclubvup.dm

import llh.fanclubvup.dm.cmd.DanmakuCommand
import llh.fanclubvup.dm.cmd.SendGiftCommand
import llh.fanclubvup.dm.cmd.UnknownCommand
import llh.fanclubvup.dm.cmd.content
import llh.fanclubvup.dm.cmd.formattedSender
import llh.fanclubvup.dm.cmd.sender
import llh.fanclubvup.dm.cmd.totalValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class CommandProcessorTest {

    @ParameterizedTest
    @ValueSource(
        strings = [
            """
            {
            "cmd": "SEND_GIFT",
            "uid": 123456789,
            "uname": "张三",
            "gift_name": "小花花",
            "num": 10,
            "price": 1
        }
            """,
        ]
    )
    fun test(jsonStr: String) {
        val rs = CommandProcessor.parseCommand(jsonStr.trim())
        println(rs)
    }

    @Test
    fun a() {
        val giftJson = """
        {
            "cmd": "SEND_GIFT",
            "uid": 123456789,
            "uname": "张三",
            "gift_name": "小花花",
            "num": 10,
            "price": 1
        }
    """.trimIndent()

        val danmakuJson = """
        {
            "cmd": "DANMU_MSG:2:0:0:0",
            "info": [[123, "李四", 30, 1], ["这是一条弹幕", 0, 1234567890]]
        }
    """.trimIndent()

        val unknownJson = """
        {
            "cmd": "NEW_FEATURE_CMD:1",
            "data": {},
            "extra": "test"
        }
    """.trimIndent()

        // 1. 基础解析
        val giftCommand = CommandProcessor.parseCommandAs<SendGiftCommand>(giftJson)
        val danmakuCommand = CommandProcessor.parseCommandAs<DanmakuCommand>(danmakuJson)
        val unknownCommand = CommandProcessor.parseCommand(unknownJson)

        // 2. 优雅的模式匹配
        when (val command = CommandProcessor.parseCommand(giftJson)) {
            is SendGiftCommand -> {
                println("礼物: ${command.giftName}")
                println("赠送者: ${command.formattedSender}")
                println("数量: ${command.num}, 总价值: ${command.totalValue}")
            }

            is DanmakuCommand -> {
                println("弹幕: ${command.content}")
                println("发送者: ${command.sender}")
            }

            is UnknownCommand -> {
                println("未知命令: ${command.cmd}")
                println("原始数据: ${command.rawData}")
            }

            null -> println("解析失败")
        }
        // 3. 链式调用处理
        giftCommand?.let { gift ->
            println("收到礼物: ${gift.giftName}")
        } ?: println("不是礼物命令")

        // 4. 使用安全调用和Elvis操作符
        val content = danmakuCommand?.content ?: "无弹幕内容"
        println("弹幕内容: $content")
    }
}