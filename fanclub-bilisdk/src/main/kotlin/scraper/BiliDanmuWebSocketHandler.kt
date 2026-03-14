/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.scraper

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.consts.ScraperConst
import llh.fanclubvup.bilisdk.dto.danmu.HostServer
import llh.fanclubvup.bilisdk.utils.WsMsgUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class BiliDanmuWebSocketHandler(
    private val client: OkHttpClient,
    private val hostList: List<HostServer>,
) : WebSocketListener() {

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
        webSocket = client.newWebSocket(request, InnerWebSocketListener(::reconnect))
    }

    fun reconnect() {
        retry++
        if (retry > maxRetryCount) {
            logger.error { "重连次数达到最大限制，停止重连" }
            return
        }
        logger.info { "正在尝试第 $retry 次重连..." }
        connect()
    }

    fun send(bytes: ByteString) {
        webSocket?.send(bytes)
    }

    class InnerWebSocketListener(val reconnect: () -> Unit) : WebSocketListener() {
        private val logger = KotlinLogging.logger {}
        override fun onOpen(webSocket: WebSocket, response: Response) {
            logger.info { "WebSocket 连接已打开 - ${response.request.url}" }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            logger.debug { "收到文本消息：$text" }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            WsMsgUtil.parsePacket(bytes ,webSocket)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            logger.error(t) { "WebSocket 连接失败 - ${response?.request?.url}" }
            reconnect()
        }
    }

}
