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
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * 弹幕 WebSocket 处理类
 * @param connectionFailed 连接失败回调
 * @param biliWsMsgBizHandler 弹幕消息业务处理类
 */
class BiliDanmuWebSocketHandler(
    private val client: OkHttpClient,
    private val hostList: List<HostServer>,
    private val biliWsMsgBizHandler: BiliWsMsgBizHandler,
    private val connectionFailed: () -> Unit = {},
) {
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    private val logger = KotlinLogging.logger {}
    private val maxRetryCount = 5

    private var retry = 0
    private var webSocket: WebSocket? = null

    fun connect() {
        val server = hostList[retry % hostList.size]
        val url = "wss://${server.host}:${server.wssPort}/sub"
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", ScraperConst.USER_AGENT)
            .build()
        webSocket = client.newWebSocket(
            request,
            InnerWebSocketListener(::reconnect, biliWsMsgBizHandler)
        )
        webSocket?.let {
            scheduler.scheduleAtFixedRate({
                val reply = makePacket("{}", WsOperation.HEARTBEAT)
                send(reply.toByteString())
            }, 0, 30, TimeUnit.SECONDS)
        }
    }

    fun isValid(): Boolean {
        return webSocket != null
    }

    fun reconnect() {
        retry++
        if (retry > maxRetryCount) {
            logger.error { "重连次数达到最大限制，停止重连" }
            webSocket = null
            connectionFailed()
            return
        }
        logger.info { "正在尝试第 $retry 次重连..." }
        connect()
    }

    fun send(bytes: ByteString) {
        webSocket?.send(bytes)
    }

    class InnerWebSocketListener(
        private val reconnect: () -> Unit,
        private val biliWsMsgBizHandler: BiliWsMsgBizHandler,
    ) : WebSocketListener() {
        private val logger = KotlinLogging.logger {}
        override fun onOpen(webSocket: WebSocket, response: Response) {
            logger.info { "WebSocket 连接已打开 - ${response.request.url}" }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            logger.debug { "收到文本消息：$text" }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            WsMsgUtil.parsePacket(bytes, webSocket, biliWsMsgBizHandler)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            logger.error(t) { "WebSocket 连接失败 - ${response?.request?.url}" }
            reconnect()
        }
    }

}
