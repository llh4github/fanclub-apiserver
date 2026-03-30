/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.components

import llh.fanclubvup.apiserver.components.properties.WebsocketProperty
import llh.fanclubvup.apiserver.websocket.DanmuWebsocketHandler
import llh.fanclubvup.apiserver.websocket.ValidConnHandshakeInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.server.HandshakeInterceptor

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val danmuWebsocketHandler: DanmuWebsocketHandler,
    private val property: WebsocketProperty,
    private val interceptor: ValidConnHandshakeInterceptor,
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(danmuWebsocketHandler, "/ws/danmu/{uid}")
            .setAllowedOriginPatterns(property.domain) // 允许跨域，生产环境应指定具体域名
            .addInterceptors(interceptor)
    }
}
