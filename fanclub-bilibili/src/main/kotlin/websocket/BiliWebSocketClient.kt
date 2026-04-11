/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilibili.constants.ApiConstants
import llh.fanclubvup.bilibili.dm.CommandProcessor
import llh.fanclubvup.bilibili.dm.DanmuCommandDispatcher
import llh.fanclubvup.bilibili.dm.DanmuCommandHandler
import llh.fanclubvup.bilibili.dto.DanmuHost
import llh.fanclubvup.bilibili.utils.JsonUtils
import llh.fanclubvup.common.BID
import okhttp3.*
import okio.ByteString
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * B站 WebSocket 客户端
 * 用于与 B站 弹幕服务器建立 WebSocket 连接，接收弹幕消息
 * 
 * 采用命令分发器模式处理弹幕消息：
 * 1. 接收原始 WebSocket 数据包
 * 2. 解析为 DanmuMessage
 * 3. 使用 CommandProcessor 解析为强类型的 Command 对象
 * 4. 通过 DanmuCommandDispatcher 分发到对应的处理器
 * 
 * @param hostList 弹幕服务器主机列表
 * @param roomId 房间 ID
 * @param token 认证令牌
 * @param uid 用户 ID
 * @param buvid 设备 ID
 * @param handlers 弹幕命令处理器列表，用于处理不同类型的命令
 * @param onConnectionFailed 连接失败回调函数
 */
class BiliWebSocketClient(
    private val hostList: List<DanmuHost>,
    private val roomId: Long,
    private val token: String,
    private val uid: BID = -1L,
    private val buvid: String = "",
    handlers: List<DanmuCommandHandler<*>>,
    private val onConnectionFailed: (roomId: Long) -> Unit = {}
) : AutoCloseable {
    private val logger = KotlinLogging.logger {}

    /**
     * OkHttp 客户端
     * 使用 lazy 初始化，避免在不需要时创建
     */
    private val client by lazy { OkHttpClient.Builder().build() }

    /**
     * 调度器
     * 用于执行心跳任务和重连任务
     */
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    /**
     * WebSocket 连接
     */
    private var webSocket: WebSocket? = null

    /**
     * 心跳任务
     */
    private var heartbeatTask: ScheduledFuture<*>? = null

    /**
     * 重连计数器
     * 用于实现重连逻辑
     */
    private val retryCounter = AtomicInteger(0)

    /**
     * 最大重连次数
     */
    private val maxRetryCount = 5

    /**
     * JSON 映射器
     * 使用项目统一的 JsonUtils.mapper，确保配置一致
     */
    private val mapper = JsonUtils.mapper

    /**
     * 弹幕命令分发器
     * 根据命令类型将已解析的 Command 对象分发到对应的处理器
     */
    private val dispatcher = DanmuCommandDispatcher(handlers)

    /**
     * 连接到 B站 弹幕服务器
     * 
     * 1. 选择服务器
     * 2. 创建 WebSocket 连接
     * 3. 发送认证信息
     * 4. 启动心跳
     */
    fun connect() {
        // 选择服务器，使用轮询方式
        val currentRetry = retryCounter.get()
        val server = hostList[currentRetry % hostList.size]
        val url = "wss://${server.host}:${server.wssPort}/sub"
        logger.info { "正在连接到 Bilibili WebSocket: $url" }

        // 创建请求，设置 User-Agent
        val request = Request.Builder().url(url).header("User-Agent", ApiConstants.USER_AGENT).build()
        webSocket = client.newWebSocket(request, createWebSocketListener())

        // 发送认证信息
        webSocket?.let { ws ->
            val auth = DanmuAuth.create(roomId, token, uid, buvid)
            val authJson = mapper.writeValueAsString(auth)
            logger.debug { "input-auth:\n$authJson" }
            ws.send(makePacket(authJson.toByteArray(Charsets.UTF_8), WsOperation.AUTH))
            startHeartbeat()
        }
    }

    /**
     * 启动心跳
     * 
     * 每 30 秒发送一次心跳，保持连接活跃
     */
    private fun startHeartbeat() {
        heartbeatTask = scheduler.scheduleAtFixedRate({
            webSocket?.send(makePacket("{}".encodeToByteArray(), WsOperation.HEARTBEAT))
        }, 0, 30, TimeUnit.SECONDS)
    }

    /**
     * 取消心跳
     */
    private fun cancelHeartbeat() {
        heartbeatTask?.cancel(true)
        heartbeatTask = null
    }

    /**
     * 重连
     * 
     * 当连接失败时，尝试重新连接
     */
    private fun reconnect() {
        val currentRetry = retryCounter.incrementAndGet()
        if (currentRetry > maxRetryCount) {
            // 重连次数超过限制，调用失败回调
            close()
            onConnectionFailed(roomId)
            return
        }
        // 延迟 2 秒后重连
        scheduler.schedule({ connect() }, 2, TimeUnit.SECONDS)
    }

    /**
     * 创建 WebSocket 监听器
     * 
     * 处理 WebSocket 事件
     */
    private fun createWebSocketListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                logger.info { "WebSocket 连接已打开" }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                try {
                    // 解析数据包，处理消息
                    val list = parsePacket(bytes, roomId)
                    list.forEach { msg ->
                        logger.debug { "解析到消息: ${msg.cmd}" }

                        // 检查消息是否在3秒内已处理过
                        if (!MessageDeduplicationCache.isDuplicate(msg.rawData)) {
                            // 使用 CommandProcessor 解析消息为强类型 Command 对象
                            val command = CommandProcessor.parseCommand(msg.rawData)
                            if (command != null) {
                                logger.debug { "解析到命令: ${command.cmd}" }
                                // 通过分发器将命令分发到对应的处理器
                                dispatcher.dispatch(command, roomId)
                            }
                        } else {
                            logger.debug { "消息已处理过，跳过: ${msg.cmd}" }
                        }
                    }
                } catch (e: Exception) {
                    logger.error(e) { "处理消息失败" }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                logger.error(t) { "WebSocket 连接失败" }
                cancelHeartbeat()
                reconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                logger.info { "WebSocket 连接关闭" }
                cancelHeartbeat()
            }
        }
    }

    /**
     * 启动 WebSocket 客户端
     */
    fun start() {
        connect()
    }

    /**
     * 关闭 WebSocket 客户端
     * 
     * 1. 取消心跳
     * 2. 关闭 WebSocket 连接
     * 3. 关闭调度器
     * 4. 关闭 OkHttp 客户端
     */
    override fun close() {
        cancelHeartbeat()
        webSocket?.close(1000, "Normal closure")
        scheduler.shutdown()
        client.dispatcher.executorService.shutdown()
    }
}
