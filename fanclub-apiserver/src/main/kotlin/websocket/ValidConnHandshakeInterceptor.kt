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
        attributes: Map<String, Any>
    ): Boolean {
        if (request is ServletServerHttpRequest) {
            val uri = request.servletRequest.requestURI
            val segments = uri.split("/")
            if (segments.size >= 4) {
                val uid = segments[3].toLongOrNull()
                if (!checkUID(uid)) {
                    logger.warn { "$uid 无效, 拒绝WS连接" }
                    // 设置 HTTP 状态码为
                    response.setStatusCode(HttpStatus.UNAUTHORIZED)
                    return false // 拒绝握手，主动断开连接
                }
            }
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
            val uri = request.servletRequest.requestURI
            logger.info { "WS连接成功: $uri" }
        }
        // 握手后处理，可以留空
    }
}
