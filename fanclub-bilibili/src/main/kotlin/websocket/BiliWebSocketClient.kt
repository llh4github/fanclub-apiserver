package llh.fanclubvup.bilibili.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilibili.constants.ApiConstants
import llh.fanclubvup.bilibili.dto.DanmuHost
import okhttp3.*
import okio.ByteString
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class BiliWebSocketClient(
    private val hostList: List<DanmuHost>,
    private val roomId: Long,
    private val token: String,
    private val onMessage: (DanmuMessage) -> Unit = {},
    private val onConnectionFailed: () -> Unit = {}
) : AutoCloseable {
    private val logger = KotlinLogging.logger {}
    private val client by lazy { OkHttpClient.Builder().build() }
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var webSocket: WebSocket? = null
    private var heartbeatTask: ScheduledFuture<*>? = null
    private val retryCounter = AtomicInteger(0)
    private val maxRetryCount = 5
    private val mapper = jacksonObjectMapper()

    fun connect() {
        val currentRetry = retryCounter.get()
        val server = hostList[currentRetry % hostList.size]
        val url = "wss://${server.host}:${server.wssPort}/sub"
        logger.info { "正在连接到 Bilibili WebSocket: $url" }

        val request = Request.Builder().url(url).header("User-Agent", ApiConstants.USER_AGENT).build()
        webSocket = client.newWebSocket(request, createWebSocketListener())

        webSocket?.let { ws ->
            val auth = DanmuAuth.create(roomId, token)
            val authJson = mapper.writeValueAsString(auth)
            ws.send(makePacket(authJson.toByteArray(Charsets.UTF_8), WsOperation.AUTH))
            logger.info { "已发送认证信息" }
            startHeartbeat()
        }
    }

    private fun startHeartbeat() {
        heartbeatTask = scheduler.scheduleAtFixedRate({
            webSocket?.send(makePacket(byteArrayOf(), WsOperation.HEARTBEAT))
        }, 0, 30, TimeUnit.SECONDS)
    }

    private fun cancelHeartbeat() {
        heartbeatTask?.cancel(true)
        heartbeatTask = null
    }

    private fun reconnect() {
        val currentRetry = retryCounter.incrementAndGet()
        if (currentRetry > maxRetryCount) {
            close()
            onConnectionFailed()
            return
        }
        scheduler.schedule({ connect() }, 2, TimeUnit.SECONDS)
    }

    private fun createWebSocketListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                logger.info { "WebSocket 连接已打开" }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                try {
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

    fun start() {
        connect()
    }

    override fun close() {
        cancelHeartbeat()
        webSocket?.close(1000, "Normal closure")
        scheduler.shutdown()
        client.dispatcher.executorService.shutdown()
    }
}
