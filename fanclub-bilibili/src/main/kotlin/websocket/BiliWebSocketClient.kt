/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilibili.constants.ApiConstants
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
 * @param hostList 弹幕服务器主机列表
 * @param roomId 房间 ID
 * @param token 认证令牌
 * @param uid 用户 ID
 * @param buvid 设备 ID
 * @param onMessage 消息回调函数
 * @param onConnectionFailed 连接失败回调函数
 */
class BiliWebSocketClient(
    private val hostList: List<DanmuHost>,
    private val roomId: Long,
    private val token: String,
    private val uid: BID = -1L,
    private val buvid: String = "",
    private val onMessage: (DanmuMessage) -> Unit = {},
    private val onConnectionFailed: () -> Unit = {}
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
            webSocket?.send(makePacket(byteArrayOf(), WsOperation.HEARTBEAT)) 
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
            onConnectionFailed()
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
                    parsePacket(bytes, roomId).forEach { msg ->
                        logger.debug { "解析到消息: ${msg.cmd}" }
                        onMessage(msg)
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
