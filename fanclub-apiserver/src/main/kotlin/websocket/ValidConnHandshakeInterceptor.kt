/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.service.sys.ScraperFeatureService
import org.springframework.http.HttpStatus
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Component
class ValidConnHandshakeInterceptor(
    private val scraperFeatureService: ScraperFeatureService,
) : HandshakeInterceptor {

    private val logger = KotlinLogging.logger {}

    fun checkUID(uid: Long?): Boolean {
        if (uid == null) return false
        return scraperFeatureService.queryMonitorEnabled().firstOrNull {
            it.anchorInfo.biliId == uid
        } != null
    }

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        if (request is ServletServerHttpRequest) {
            // 从查询参数中获取 uid
            val uid = request.servletRequest.getParameter("uid")?.toLongOrNull()
            if (uid == null || !checkUID(uid)) {
                logger.warn { "uid=$uid 无效，拒绝 WS 连接" }
                // 设置 HTTP 状态码为
                response.setStatusCode(HttpStatus.UNAUTHORIZED)
                return false // 拒绝握手，主动断开连接
            }
            // 将 uid 存储到 session 属性中
            attributes["uid"] = uid
            return true
        }
        return true
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {
        if (request is ServletServerHttpRequest) {
            val uid = request.servletRequest.getParameter("uid")
            logger.info { "WS 连接成功：uid=$uid" }
        }
        // 握手后处理，可以留空
    }
}
