/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilibili.http.BiliHttpClient
import llh.fanclubvup.bilibili.props.BiliClientConfig
import llh.fanclubvup.bilibili.websocket.BiliWebSocketClient
import llh.fanclubvup.bilibili.websocket.DanmuMessage

/**
 * B站客户端
 * 负责管理与 B站服务器的连接，包括获取弹幕服务器信息和建立 WebSocket 连接
 * 
 * @param roomId 房间 ID
 * @param config 客户端配置，包含 uid、buvid 和 cookies 等信息
 * @param httpClient HTTP 客户端，用于获取弹幕服务器信息
 * @param onDanmuMessage 弹幕消息回调函数
 */
class BiliClient(
    private val roomId: Long,
    private val config: BiliClientConfig,
    private val httpClient: BiliHttpClient = BiliHttpClient(config),
    private val onDanmuMessage: (roomId: Long, msg: DanmuMessage) -> Unit
) : AutoCloseable {
    private val logger = KotlinLogging.logger {}
    private var wsClient: BiliWebSocketClient? = null

    /**
     * 启动 B站客户端
     * 1. 获取弹幕服务器信息
     * 2. 检查信息完整性
     * 3. 建立 WebSocket 连接
     */
    fun start(): Boolean {
        logger.info { "启动 B站客户端，房间ID: $roomId" }
        // 获取弹幕服务器信息，这是建立 WebSocket 连接的前提
        val danmuInfoResult = httpClient.fetchDanmuServerInfo(roomId)
        if (danmuInfoResult.isFailure) {
            logger.error(danmuInfoResult.exceptionOrNull()) { "获取弹幕服务器信息失败" }
            return false
        }

        val danmuInfo = danmuInfoResult.getOrThrow()
        val hostList = danmuInfo?.data?.hostList
        val token = danmuInfo?.data?.token

        // 检查弹幕服务器信息是否完整，缺少主机列表或 token 都无法建立连接
        if (hostList.isNullOrEmpty() || token.isNullOrEmpty()) {
            logger.error { "弹幕服务器信息不完整" }
            return false
        }

        // 创建 WebSocket 客户端并启动连接
        // 使用从配置中获取的 uid 和 buvid，确保认证信息正确
        wsClient = BiliWebSocketClient(
            hostList,
            roomId,
            token,
            config.uid,
            config.buvid,
            onDanmuMessage
        )
        wsClient?.start()
        return true
    }

    /**
     * 有效性检查
     */
    fun isValid() = wsClient != null

    /**
     * 关闭 B站客户端
     * 关闭 WebSocket 连接，释放资源
     */
    override fun close() {
        wsClient?.close()
    }
}
