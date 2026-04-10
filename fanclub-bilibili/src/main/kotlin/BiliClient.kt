/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilibili.dm.DanmuCommandHandler
import llh.fanclubvup.bilibili.http.BiliHttpClient
import llh.fanclubvup.bilibili.props.BiliClientConfig
import llh.fanclubvup.bilibili.websocket.BiliWebSocketClient

/**
 * B站客户端
 * 负责管理与 B站服务器的连接，包括获取弹幕服务器信息和建立 WebSocket 连接
 * 
 * 采用命令分发器模式处理弹幕消息，支持复杂业务逻辑的解耦和扩展。
 * 通过注册多个 DanmuCommandHandler，可以将不同类型的弹幕命令分发到不同的处理器。
 * 
 * 架构说明：
 * 1. BiliClient 负责高层连接管理（获取弹幕服务器信息、启动 WebSocket）
 * 2. BiliWebSocketClient 负责底层 WebSocket 通信和命令分发
 * 3. DanmuCommandHandler 处理已解析的强类型 Command 对象
 * 
 * @param roomId 房间 ID
 * @param config 客户端配置，包含 uid、buvid 和 cookies 等信息
 * @param handlers 弹幕命令处理器列表，用于处理不同类型的命令
 * @param httpClient HTTP 客户端，用于获取弹幕服务器信息
 * 
 * 使用示例：
 * ```kotlin
 * val handlers = listOf(
 *     object : DanmuCommandHandler<DanmuMsgCommand> {
 *         override fun handle(cmd: DanmuMsgCommand, roomId: Long) {
 *             // 直接访问 cmd.info 等字段，无需手动解析 JSON
 *             println("收到弹幕: ${cmd.info}")
 *         }
 *         override fun supportedCommand() = DanmuMsgCommand::class
 *     }
 * )
 * val client = BiliClient(roomId = 123456, config = config, handlers = handlers)
 * client.start()
 * ```
 */
class BiliClient(
    private val roomId: Long,
    private val config: BiliClientConfig,
    private val handlers: List<DanmuCommandHandler<*>>,
    private val httpClient: BiliHttpClient = BiliHttpClient(config)
) : AutoCloseable {
    private val logger = KotlinLogging.logger {}
    private var wsClient: BiliWebSocketClient? = null

    /**
     * 启动 B站客户端
     * 1. 获取弹幕服务器信息
     * 2. 检查信息完整性
     * 3. 建立 WebSocket 连接
     */
    fun start() {
        logger.info { "启动 B站客户端，房间ID: $roomId" }
        // 获取弹幕服务器信息，这是建立 WebSocket 连接的前提
        val danmuInfoResult = httpClient.fetchDanmuServerInfo(roomId)
        if (danmuInfoResult.isFailure) {
            logger.error(danmuInfoResult.exceptionOrNull()) { "获取弹幕服务器信息失败" }
            return
        }

        val danmuInfo = danmuInfoResult.getOrThrow()
        val hostList = danmuInfo?.data?.hostList
        val token = danmuInfo?.data?.token

        // 检查弹幕服务器信息是否完整，缺少主机列表或 token 都无法建立连接
        if (hostList.isNullOrEmpty() || token.isNullOrEmpty()) {
            logger.error { "弹幕服务器信息不完整" }
            return
        }

        // 创建 WebSocket 客户端并启动连接
        // handlers 会传递给 BiliWebSocketClient，在那里进行命令分发
        wsClient = BiliWebSocketClient(
            hostList = hostList,
            roomId = roomId,
            token = token,
            uid = config.uid,
            buvid = config.buvid,
            handlers = handlers
        )
        wsClient?.start()
    }

    fun isValid() = wsClient != null

    /**
     * 关闭 B站客户端
     * 关闭 WebSocket 连接，释放资源
     */
    override fun close() {
        wsClient?.close()
    }
}
