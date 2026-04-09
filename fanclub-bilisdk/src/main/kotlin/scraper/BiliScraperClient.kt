/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.scraper

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.consts.BiliApiUrls
import llh.fanclubvup.bilisdk.dto.*
import llh.fanclubvup.bilisdk.enums.WsOperation
import llh.fanclubvup.bilisdk.event.DanmuWsFailedEvent
import llh.fanclubvup.bilisdk.http.BiliHttpClient
import llh.fanclubvup.bilisdk.props.BiliScraperProp
import llh.fanclubvup.bilisdk.security.WbiSignService
import llh.fanclubvup.bilisdk.utils.WsMsgUtil
import llh.fanclubvup.common.BID
import llh.fanclubvup.common.getOrNull
import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString
import org.springframework.context.ApplicationEventPublisher
import java.util.*

/**
 * B站爬虫客户端
 * 协调 HTTP 请求和 WebSocket 连接
 */
class BiliScraperClient(
    private val httpClient: BiliHttpClient,
    private val wbiSignService: WbiSignService,
    private val prop: BiliScraperProp,
    private val biliWsMsgBizHandler: BiliWsMsgBizHandler,
    private val eventPublisher: ApplicationEventPublisher,
    private val authFetcher: BiliWsAuthFetcher,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 获取用户关系数据：关注的数量，粉丝数量
     */
    fun fetchUserRelation(uId: BID): UserRelationResponse? {
        val url = BiliApiUrls.USER_RELATION_STAT_API
        val queryString = wbiSignService.buildQueryString(TreeMap<String, String>().apply {
            this["vmid"] = uId.toString()
        }) ?: return null

        val request = httpClient.requestBuilder("$url?$queryString").build()
        return httpClient.execute(request, UserRelationResponse::class.java).getOrNull(logger)
    }

    /**
     * 获取用户信息
     *
     * @param uId B站用户UID
     */
    fun fetchUserInfo(uId: BID): UserInfoResponse? {
        val url = BiliApiUrls.USER_INFO_API
        val queryString = wbiSignService.buildQueryString(TreeMap<String, String>().apply {
            this["mid"] = uId.toString()
        }) ?: return null

        val request = httpClient.requestBuilder("$url?$queryString").build()
        return httpClient.execute(request, UserInfoResponse::class.java).getOrNull(logger)
    }

    /**
     * 获取舰长列表
     *
     * @param uId B站用户UID
     * @param roomId B站直播间ID
     * @param page 页码
     */
    fun fetchGuardList(
        uId: BID,
        roomId: Long,
        page: Int = 1,
        pageSize: Int = 30
    ): GuardPageResponse? {
        val url = BiliApiUrls.GUARD_LIST_API
        val queryString = wbiSignService.buildQueryString(TreeMap<String, String>().apply {
            this["ruid"] = uId.toString()
            this["roomid"] = roomId.toString()
            this["page"] = page.toString()
            this["page_size"] = pageSize.toString()
            this["typ"] = "5"
        }) ?: return null

        val request = httpClient.requestBuilder("$url?$queryString").build()
        return httpClient.execute(request, GuardPageResponse::class.java).getOrNull(logger)
    }

    /**
     * 获取直播间信息
     */
    fun fetchRoomInfo(roomId: Long): LiveRoomInfoResponse? {
        val url = BiliApiUrls.ROOM_INIT_URL + "?room_id=$roomId"
        val request = httpClient.requestBuilder(url).build()
        return httpClient.execute(request, LiveRoomInfoResponse::class.java).getOrNull(logger)
    }

    /**
     * 获取弹幕服务器信息
     */
    fun fetchDanmuServerInfo(roomId: Long): DanmuInfoResponse? {
        val params = TreeMap<String, String>().apply {
            put("id", roomId.toString())
            put("type", "0")
        }
        val queryString = wbiSignService.buildQueryString(params) ?: return null
        val request = httpClient.requestBuilder(BiliApiUrls.DANMAKU_SERVER_CONF_URL + "?" + queryString).build()

        return httpClient.execute(request, DanmuInfoResponse::class.java).getOrNull(logger)
    }

    /**
     * 构建 WebSocket 认证包
     */
    fun buildAuthWs(token: String, roomId: Long): Result<ByteArray> = runCatching {
        val cookies = httpClient.cookieJarManager.fetchCookies()
        val buvid = cookies.firstOrNull { it.name == "buvid3" }
            ?: throw IllegalStateException("buvid3 cookie not found")
        val text = """
             {"uid":${prop.currentBid},"roomid":$roomId,"protover":3,"buvid":"${buvid.value}","support_ack":true,"scene":"room","platform":"web","type":2,"key":"$token"}
        """.trimIndent()
        WsMsgUtil.makePacket(text, WsOperation.AUTH)
    }

    /**
     * 创建弹幕 WebSocket
     */
    fun creatDanmuWebsocket(roomId: Long): BiliDanmuWebSocketHandler? {
        val info = fetchDanmuServerInfo(roomId)?.data ?: return null
        val servers = info.hostList
        val handler = BiliDanmuWebSocketHandler(httpClient.okHttpClient, servers, biliWsMsgBizHandler, roomId) {
            eventPublisher.publishEvent(DanmuWsFailedEvent(roomId))
        }
        handler.connect { retry ->
            if (retry % 2 == 0) {
                val token = info.token ?: return@connect null
                val packet = buildAuthWs(token, roomId).getOrNull(logger) ?: return@connect null
                logger.debug { "使用接口组装授权字符串" }
                packet.toByteString()
            } else {
                logger.debug { "使用数据库中的授权字符串" }
                authFetcher.fetch(roomId)?.encodeUtf8()
            }
        }
        return handler
    }
}
