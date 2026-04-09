/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.scraper

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.consts.ScraperConst
import llh.fanclubvup.bilisdk.dto.danmu.HostServer
import llh.fanclubvup.bilisdk.enums.WsOperation
import llh.fanclubvup.bilisdk.utils.WsMsgUtil
import llh.fanclubvup.bilisdk.utils.WsMsgUtil.makePacket
import okhttp3.*
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.encoding.Base64

/**
 * 弹幕 WebSocket 处理类
 * @param connectionFailed 连接失败回调
 * @param biliWsMsgBizHandler 弹幕消息业务处理类
 */
class BiliDanmuWebSocketHandler(
    private val client: OkHttpClient,
    private val hostList: List<HostServer>,
    private val biliWsMsgBizHandler: BiliWsMsgBizHandler,
    private val roomId: Long,
    private val connectionFailed: () -> Unit = {},
) : AutoCloseable {
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    private val logger = KotlinLogging.logger {}
    private val maxRetryCount = 5

    private val retryCounter = AtomicInteger(0)
    private var webSocket: WebSocket? = null
    private var heartbeatTask: ScheduledFuture<*>? = null

    fun cancelHeartbeat() {
        heartbeatTask?.cancel(true)
        logger.info { "取消心跳任务" }
        heartbeatTask = null
    }

    fun startHeartbeat() {
        heartbeatTask = scheduler.scheduleAtFixedRate({
            val reply = makePacket("{}", WsOperation.HEARTBEAT)
            send(reply.toByteString())
        }, 0, 30, TimeUnit.SECONDS)
    }

    fun connect(fetchAuth: (retry: Int) -> ByteString?) {
        val currentRetry = retryCounter.get()
        val server = hostList[currentRetry % hostList.size]
        val url = "wss://${server.host}:${server.wssPort}/sub"
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", ScraperConst.USER_AGENT)
            .build()
        webSocket = client.newWebSocket(
            request,
            InnerWebSocketListener(roomId, biliWsMsgBizHandler) {
                reconnect(fetchAuth)
                cancelHeartbeat()
            }
        )
        webSocket?.let {
            fetchAuth(currentRetry)?.let { auth ->
                it.send(auth)
                startHeartbeat()
            }
        }
    }

    fun isValid(): Boolean {
        return webSocket != null
    }

    fun reconnect(fetchAuth: (retry: Int) -> ByteString?) {
        val currentRetry = retryCounter.incrementAndGet()
        if (currentRetry > maxRetryCount) {
            logger.error { "重连次数达到最大限制，停止重连" }
            webSocket = null
            connectionFailed()
            return
        }

        scheduler.schedule({
            logger.info { "正在尝试第 $currentRetry 次重连..." }
            connect(fetchAuth)
        }, 2, TimeUnit.SECONDS)
    }

    fun send(bytes: ByteString) {
        logger.debug { "发送数据：\n${Base64.encode(bytes.toByteArray())}" }
        webSocket?.send(bytes)
    }

    override fun close() {
        cancelHeartbeat()
        webSocket?.close(1000, "Normal closure")
        scheduler.shutdown()
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow()
            }
        } catch (e: InterruptedException) {
            scheduler.shutdownNow()
        }
    }

    class InnerWebSocketListener(
        private val roomId: Long,
        private val biliWsMsgBizHandler: BiliWsMsgBizHandler,
        private val reconnect: () -> Unit,
    ) : WebSocketListener() {
        private val logger = KotlinLogging.logger {}
        override fun onOpen(webSocket: WebSocket, response: Response) {
            logger.info { "WebSocket 连接已打开 - ${response.request.url}" }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            logger.debug { "收到文本消息：$text" }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            WsMsgUtil.parsePacket(bytes, webSocket, biliWsMsgBizHandler, roomId)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            logger.error(t) { "WebSocket 连接失败 - ${response?.request?.url}" }
            reconnect()
        }
    }

}
