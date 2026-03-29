/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.components

import llh.fanclubvup.apiserver.websocket.DanmuWebsocketHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val danmuWebsocketHandler: DanmuWebsocketHandler,
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(danmuWebsocketHandler, "/ws/danmu/{uid}")
            .setAllowedOriginPatterns("*") // 允许跨域，生产环境应指定具体域名
            .withSockJS() // 可选，启用 SockJS 降级支持
    }
}
