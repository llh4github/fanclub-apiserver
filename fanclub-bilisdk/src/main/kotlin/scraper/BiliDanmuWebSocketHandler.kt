/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.scraper

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.consts.ScraperConst
import llh.fanclubvup.bilisdk.dto.danmu.HostServer
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

abstract class BiliDanmuWebSocketHandler(
    private val client: OkHttpClient,
    private val hostList: List<HostServer>,
) : WebSocketListener() {

    private val logger = KotlinLogging.logger {}
    private val maxRetryCount = 5

    private var retry = 0
    private var webSocket: WebSocket? = null

    fun connect() {
        val host = hostList[retry % hostList.size]
        val url = "wss://$host:${host.wssPort}/sub"
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", ScraperConst.USER_AGENT)
            .build()
        webSocket = client.newWebSocket(request, this)
    }

    private fun reconnect() {
        retry++
        if (retry > maxRetryCount) {
            logger.error { "重连次数达到最大限制，停止重连" }
            return
        }
        logger.info { "正在尝试第 $retry 次重连..." }
        connect()
    }

    fun send(message: String) {
        webSocket?.send(message)
    }

    override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
        logger.info { "WebSocket 连接已打开 - ${response.request.url}" }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        logger.debug { "收到文本消息：$text" }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        logger.debug { "收到二进制消息：${bytes.size} 个字节" }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        logger.info { "WebSocket 正在关闭 - 代码：$code, 原因：$reason" }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        logger.info { "WebSocket 已关闭 - 代码：$code, 原因：$reason" }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
        logger.error(t) { "WebSocket 发生错误 - ${response?.body?.string() ?: "未知"},msg: ${t.message} " }
        reconnect()
    }
}
