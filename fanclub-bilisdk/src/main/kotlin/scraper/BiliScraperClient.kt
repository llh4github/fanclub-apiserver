/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.scraper

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.cache.BiliSignCacheManager
import llh.fanclubvup.bilisdk.cache.PersistentCookieJarManager
import llh.fanclubvup.bilisdk.consts.BiliApiUrls
import llh.fanclubvup.bilisdk.consts.BiliSdkCacheKey
import llh.fanclubvup.bilisdk.consts.ScraperConst
import llh.fanclubvup.bilisdk.dto.ScraperBaseResp
import llh.fanclubvup.bilisdk.dto.WbiUserInfoResponse
import llh.fanclubvup.bilisdk.dto.DanmuInfoResponse
import llh.fanclubvup.bilisdk.dto.GuardPageResponse
import llh.fanclubvup.bilisdk.dto.LiveRoomInfoResponse
import llh.fanclubvup.bilisdk.dto.UserInfoResponse
import llh.fanclubvup.bilisdk.dto.UserRelationResponse
import llh.fanclubvup.bilisdk.enums.WsOperation
import llh.fanclubvup.bilisdk.event.DanmuWsFailedEvent
import llh.fanclubvup.bilisdk.props.BiliScraperProp
import llh.fanclubvup.bilisdk.utils.WbiUtil
import llh.fanclubvup.bilisdk.utils.WsMsgUtil
import llh.fanclubvup.common.BID
import llh.fanclubvup.common.excptions.AppRuntimeException
import llh.fanclubvup.common.getOrNull
import llh.fanclubvup.common.utils.Md5Utils
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okio.ByteString.Companion.toByteString
import org.springframework.context.ApplicationEventPublisher
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.net.URLEncoder
import java.time.Duration
import java.time.Instant
import java.util.*

class BiliScraperClient(
    private val cacheManager: BiliSignCacheManager,
    private val persistentCookieJarManager: PersistentCookieJarManager,
    private val prop: BiliScraperProp,
    private val biliWsMsgBizHandler: BiliWsMsgBizHandler,
    private val eventPublisher: ApplicationEventPublisher,
) {

    private val client by lazy {
        OkHttpClient.Builder()
            .cookieJar(persistentCookieJarManager)
            .callTimeout(Duration.ofSeconds(3))
            .build()
    }
    private val mapper = jacksonObjectMapper()
    private val logger = KotlinLogging.logger {}

    /**
     * 获取 WBI 签名
     */
    fun wbiSign(): String? {

        val request = Request.Builder()
        request.url(BiliApiUrls.WBI_INIT_URL)

        return cacheManager.getOrFetch(BiliSdkCacheKey.WBI_SIGN) {
            wbiInfo().fold(
                onSuccess = { response ->
                    response.data?.wbiImg?.let { wbiImg ->
                        WbiUtil.wbiSign(wbiImg)
                    }
                },
                onFailure = { null }
            )
        }
    }

    fun fetchUserRelation(uId: BID): UserRelationResponse? {
        val url = BiliApiUrls.USER_RELATION_STAT_API
        val queryString = buildQueryString(TreeMap<String, String>().apply {
            this["vmid"] = uId.toString()
        })

        val request = requestBuilder("$url?$queryString").build()
        return execute(request, UserRelationResponse::class.java).getOrNull(logger)
    }

    /**
     * 获取用户信息
     *
     * @param uId B站用户UID
     */
    fun fetchUserInfo(uId: BID): UserInfoResponse? {
        val url = BiliApiUrls.USER_INFO_API
        val queryString = buildQueryString(TreeMap<String, String>().apply {
            this["mid"] = uId.toString()
        })

        val request = requestBuilder("$url?$queryString").build()
        return execute(request, UserInfoResponse::class.java).getOrNull(logger)
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
        val queryString = buildQueryString(TreeMap<String, String>().apply {
            this["ruid"] = uId.toString()
            this["roomid"] = roomId.toString()
            this["page"] = page.toString()
            this["page_size"] = pageSize.toString()
            this["typ"] = "5"
        })
        val request = requestBuilder("$url?$queryString").build()
        return execute(request, GuardPageResponse::class.java).getOrNull(logger)
    }

    /**
     * 获取直播间信息
     */
    fun fetchRoomInfo(roomId: Long): LiveRoomInfoResponse? {
        val url = BiliApiUrls.ROOM_INIT_URL + "?room_id=$roomId"
        val request = requestBuilder(url).build()
        return execute(request, LiveRoomInfoResponse::class.java).getOrNull(logger)
    }

    /**
     * 获取弹幕服务器信息
     */
    fun fetchDanmuServerInfo(roomId: Long): DanmuInfoResponse? {
        val params = TreeMap<String, String>().apply {
            put("id", roomId.toString())
            put("type", "0")
        }
        val queryString = buildQueryString(params) ?: return null
        val request = requestBuilder(BiliApiUrls.DANMAKU_SERVER_CONF_URL + "?" + queryString)
            .build()

        return execute(request, DanmuInfoResponse::class.java).getOrNull(logger)
    }


    private fun buildAuthWs(token: String, roomId: Long): Result<ByteArray> = runCatching {
        val buvid = persistentCookieJarManager.fetchCookies().firstOrNull { it.name == "buvid3" }
            ?: throw AppRuntimeException("buvid3 cookie not found")
        //FIXME 感觉不是稳定
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
        val token = info.token ?: return null
        val servers = info.hostList
        val packet = buildAuthWs(token, roomId).getOrNull(logger) ?: return null
        val handler = BiliDanmuWebSocketHandler(client, servers, biliWsMsgBizHandler) {
            eventPublisher.publishEvent(DanmuWsFailedEvent(roomId))
        }
        handler.connect()
        handler.send(packet.toByteString())
        return handler
    }

    private fun requestBuilder(url: String) = Request.Builder()
        .url(url)
        .addHeader("User-Agent", ScraperConst.USER_AGENT)
        .addHeader("Accept", "application/json")


    private fun buildQueryString(map: TreeMap<String, String>): String? {
        val sign = wbiSign() ?: return null
        map["wts"] = Instant.now().epochSecond.toString()
        val uri = map.entries
            .joinToString("&") { (k, v) ->
                "$k=${URLEncoder.encode(v, "utf-8")}"
            }
        val md5 = Md5Utils.encode(uri + sign).getOrNull(logger) ?: return null
        return "$uri&w_rid=${md5}"
    }

    /**
     * 获取 WBI 信息
     */
    fun wbiInfo(): Result<WbiUserInfoResponse> {
        val request = Request.Builder()
            .url(BiliApiUrls.WBI_INIT_URL)
            .build()
        return execute(request, WbiUserInfoResponse::class.java)
    }

    private fun <T : ScraperBaseResp> execute(request: Request, clazz: Class<T>): Result<T> =
        runCatching {
            client.newCall(request)
                .execute().use { response ->
                    if (!response.isSuccessful) {
                        logger.error { "${request.url} 响应结果：\n${response.body.string()}" }
                        throw AppRuntimeException("${request.url}请求失败")
                    }

                    val rs = mapper.readValue(response.body.string(), clazz)
                    return@runCatching rs
                }
        }
}
