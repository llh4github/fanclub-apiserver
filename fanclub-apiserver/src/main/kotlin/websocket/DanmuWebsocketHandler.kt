/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.dto.viwer.DanmuWsMsg
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.util.concurrent.CopyOnWriteArraySet

@Component
class DanmuWebsocketHandler : TextWebSocketHandler() {
    private val sessions = CopyOnWriteArraySet<WebSocketSession>()
    private val mapper = jacksonObjectMapper()

    private val logger = KotlinLogging.logger {}
    override fun afterConnectionEstablished(session: WebSocketSession) {
        // 从 URI 模板变量中获取 uid
        val uid = session.attributes["uid"]?.toString()?.toLong()
        if (uid != null) {
            // 将 uid 存储到 session 属性中，方便后续使用
            session.attributes["uid"] = uid
            logger.info { "WebSocket 连接建立，uid=$uid, sessionId=${session.id}" }
        } else {
            logger.warn { "WebSocket 连接缺少 uid 参数，sessionId=${session.id}" }
        }
        sessions.add(session)
    }

    fun sendDanmu(msg: DanmuWsMsg) {
        val json = mapper.writeValueAsString(msg)
        val targetId = msg.targetUID
        val packet = TextMessage(json)
        sessions.forEach { session ->
            if (session.attributes["uid"] != targetId) {
                return@forEach
            }
            try {
                session.sendMessage(packet)
            } catch (e: Exception) {
                logger.error(e) { "发送弹幕消息失败，uid=${session.attributes["uid"]}, sessionId=${session.id}" }
            }
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val uid = session.attributes["uid"] as? Long
        logger.info { "WebSocket 连接关闭，uid=$uid, sessionId=${session.id}, status=$status" }
        sessions.remove(session)
    }

}
