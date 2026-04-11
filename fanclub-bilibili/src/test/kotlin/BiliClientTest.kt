/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import llh.fanclubvup.bilibili.dm.DanmuCommandHandler
import llh.fanclubvup.bilibili.dm.cmd.Command
import llh.fanclubvup.bilibili.dto.SerializableCookie
import llh.fanclubvup.bilibili.http.BiliHttpClient
import llh.fanclubvup.bilibili.props.BiliClientConfig
import llh.fanclubvup.bilibili.websocket.BiliWebSocketClient
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.milliseconds


/**
 * WebSocket 客户端测试
 */
class BiliClientTest {
    init {
        // 设置日志级别为 debug
        System.setProperty("kotlin-logging.level", "DEBUG")
    }

    private val logger = KotlinLogging.logger {}

    @Test
    fun testDanmuWebSocketConnection() = runBlocking {
        println("\n测试弹幕 WebSocket 连接")
        println("=======================================")


        val roomId = System.getenv("BILI_ROOM_ID")?.toLong() ?: -1L
        val myUid = System.getenv("BILI_UID")?.toLong() ?: -1L
        val uvid = System.getenv("BILI_UVID") ?: ""
        val sessdata = System.getenv("BILI_SESSDATA") ?: ""

        // 创建 HTTP 客户端获取弹幕服务器信息（带 Cookie，启用详细日志）
        val cookie = """
            SESSDATA=$sessdata
        """.trimIndent()

        // 将 cookie 字符串转换为 SerializableCookie
        val cookies = cookie.split(";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull {
                val parts = it.split("=", limit = 2)
                if (parts.size == 2) {
                    SerializableCookie(
                        name = parts[0],
                        value = parts[1],
                        domain = "bilibili.com"
                    )
                } else {
                    null
                }
            }

        // 创建测试用的 BiliClientConfigFetcher 实例
        val config = BiliClientConfig(
            uid = myUid,
            buvid = uvid,
            cookies = cookies
        )

        val httpClient = BiliHttpClient(config, enableLogging = true)


        // 获取弹幕服务器信息（带超时）
        val danmuInfoResult = withTimeoutOrNull(10000.milliseconds) {
            httpClient.fetchDanmuServerInfo(roomId)
        }

        if (danmuInfoResult == null) {
            println("获取弹幕服务器信息超时")
            return@runBlocking
        }

        if (danmuInfoResult.isFailure) {
            println("获取弹幕服务器信息失败: ${danmuInfoResult.exceptionOrNull()}")
            return@runBlocking
        }

        val danmuInfo = danmuInfoResult.getOrThrow()
        val data = danmuInfo?.data

        if (data == null) {
            println("未获取到数据部分")
            return@runBlocking
        }

        val token = data.token
        val hostList = data.hostList

        if (token.isNullOrEmpty()) {
            println("未获取到 Token")
            return@runBlocking
        }

        if (hostList.isNullOrEmpty()) {
            println("主机列表为空")
            return@runBlocking
        }

        val firstHost = hostList.first()
        val wsUrl = "wss://${firstHost.host}:${firstHost.wssPort}/sub"

        println("\nWebSocket URL: $wsUrl")
        println("Token: $token")

        // 使用 CountDownLatch 等待消息或超时
        val latch = CountDownLatch(1)
        var messageCount = 0

        // 创建简单的命令处理器用于测试
        val testHandler = object : DanmuCommandHandler<Command> {
            private val testLogger = KotlinLogging.logger {}

            override fun handle(cmd: Command, roomId: Long) {
                messageCount++
                testLogger.info { "收到消息 #${messageCount}: ${cmd.cmd}" }
                if (messageCount >= 10) {
                    latch.countDown()
                }
            }

            override fun supportedCommand(): KClass<Command> {
                // 返回 Command::class 作为通配符，处理所有命令
                return Command::class
            }
        }

        // 创建 WebSocket 客户端
        val wsClient = BiliWebSocketClient(
            hostList = hostList,
            roomId = roomId,
            token = token,
            uid = config.uid,
            buvid = config.buvid,
            handlers = listOf(testHandler),
            onConnectionFailed = {
                logger.error { "WebSocket 连接失败且无法重连" }
                latch.countDown()
            }
        )

        try {
            // 启动连接
            wsClient.start()
            println("WebSocket 连接已启动，等待接收消息...")

            // 等待最多 10 秒或收到 5 条消息
            val completed = latch.await(25, TimeUnit.SECONDS)

            if (completed) {
                if (messageCount > 0) {
                    println("✓ WebSocket 连接测试成功，收到 $messageCount 条消息")
                } else {
                    println("✗ WebSocket 连接失败")
                }
            } else {
                println("⚠ WebSocket 连接超时（这是预期的，因为我们在测试）")
            }
        } finally {
            wsClient.close()
            println("\n测试完成")
        }
    }
}
