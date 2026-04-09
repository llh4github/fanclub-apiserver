package llh.fanclubvup.bilibili

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilibili.http.BiliHttpClient
import llh.fanclubvup.bilibili.websocket.BiliWebSocketClient
import llh.fanclubvup.bilibili.websocket.DanmuMessage

class BiliClient(private val roomId: Long, private val onDanmuMessage: (DanmuMessage) -> Unit = {}) : AutoCloseable {
    private val logger = KotlinLogging.logger {}
    private val httpClient = BiliHttpClient()
    private var wsClient: BiliWebSocketClient? = null

    fun start() {
        logger.info { "启动 B站客户端，房间ID: $roomId" }
        val danmuInfoResult = httpClient.fetchDanmuServerInfo(roomId)
        if (danmuInfoResult.isFailure) {
            logger.error(danmuInfoResult.exceptionOrNull()) { "获取弹幕服务器信息失败" }
            return
        }

        val danmuInfo = danmuInfoResult.getOrThrow()
        val hostList = danmuInfo?.data?.hostList
        val token = danmuInfo?.data?.token

        if (hostList.isNullOrEmpty() || token.isNullOrEmpty()) {
            logger.error { "弹幕服务器信息不完整" }
            return
        }

        wsClient = BiliWebSocketClient(hostList, roomId, token, onDanmuMessage)
        wsClient?.start()
    }

    override fun close() {
        wsClient?.close()
    }
}
