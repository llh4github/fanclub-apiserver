/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.scraper

import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.WebSocketListener

class NoOpWebSocketListener : WebSocketListener() {

    private val logger = KotlinLogging.logger {}

    override fun onOpen(webSocket: okhttp3.WebSocket, response: okhttp3.Response) {
        logger.info { "WebSocket 连接已打开 - ${response.request.url}" }
    }

    override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
        logger.debug { "收到文本消息：$text" }
    }

    override fun onClosing(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
        logger.info { "WebSocket 正在关闭 - 代码：$code, 原因：$reason" }
    }

    override fun onClosed(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
        logger.info { "WebSocket 已关闭 - 代码：$code, 原因：$reason" }
    }

    override fun onFailure(webSocket: okhttp3.WebSocket, t: Throwable, response: okhttp3.Response?) {
        logger.error(t) { "WebSocket 发生错误 - ${response?.body?.string() ?: "未知"},msg: ${t.message} " }
    }
}
