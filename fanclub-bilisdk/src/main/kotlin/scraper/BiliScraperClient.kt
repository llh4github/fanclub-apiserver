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
import llh.fanclubvup.bilisdk.utils.WbiUtil
import llh.fanclubvup.bilisdk.utils.WsMsgUtil
import llh.fanclubvup.common.excptions.AppRuntimeException
import llh.fanclubvup.common.utils.Md5Utils
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.net.URLEncoder
import java.nio.charset.Charset
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.io.encoding.Base64

class BiliScraperClient(
    private val cacheManager: BiliSignCacheManager,
    private val persistentCookieJarManager: PersistentCookieJarManager
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

        return cacheManager.get(BiliSdkCacheKey.WBI_SIGN) {
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

    fun fetchUserRelation(uId: Long): UserRelationResponse? {
        val url = BiliApiUrls.USER_RELATION_STAT_API
        val queryString = buildQueryString(TreeMap<String, String>().apply {
            this["vmid"] = uId.toString()
        })

        val request = requestBuilder("$url?$queryString").build()
        return execute(request, UserRelationResponse::class.java).getOrNull()
    }

    /**
     * 获取用户信息
     *
     * @param uId B站用户UID
     */
    fun fetchUserInfo(uId: Long): UserInfoResponse? {
        val url = BiliApiUrls.USER_INFO_API
        val queryString = buildQueryString(TreeMap<String, String>().apply {
            this["mid"] = uId.toString()
        })

        val request = requestBuilder("$url?$queryString").build()
        return execute(request, UserInfoResponse::class.java).getOrNull()
    }

    /**
     * 获取舰长列表
     *
     * @param uId B站用户UID
     * @param roomId B站直播间ID
     * @param page 页码
     */
    fun fetchGuardList(
        uId: Long,
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
        return execute(request, GuardPageResponse::class.java).getOrNull()
    }

    /**
     * 获取直播间信息
     */
    fun fetchRoomInfo(roomId: Long): LiveRoomInfoResponse? {
        val url = BiliApiUrls.ROOM_INIT_URL + "?room_id=$roomId"
        val request = requestBuilder(url).build()
        return execute(request, LiveRoomInfoResponse::class.java).getOrNull()
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

        return execute(request, DanmuInfoResponse::class.java).getOrNull()
    }


    private fun buildAuthWs(token: String, uid: Long, roomId: Long): ByteArray {

        //FIXME 传入的TOKEN不对
        val text = mapOf(
            "uid" to uid,
            "roomid" to roomId,
            "protover" to 3,
            "buvid" to "E424426C-C93F-1335-B8B3-E54CCBC2CDC791306infoc",
            "support_ack" to true,
            "scene" to "room",
            "platform" to "web",
            "type" to 2,
            "key" to token
        )
        return WsMsgUtil.makePacket(text, WsOperation.AUTH)
    }

    /**
     * 创建弹幕 WebSocket
     *
     * @param bid: 当前登录的用户ID
     */
    fun creatDanmuWebsocket(bid: Long, roomId: Long, handler: WebSocketListener = NoOpWebSocketListener()): WebSocket? {
        val info = fetchDanmuServerInfo(roomId)?.data ?: return null
        val token = info.token ?: return null
        var retry = 0
        val servers = info.hostList
        val packet = buildAuthWs(token, bid, roomId)
        while (retry < 10) {
            val info = servers[retry % servers.size]
            val host = info.host
            val url = "wss://$host:${info.wssPort}/sub"
            logger.info { "url: $url" }

            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", ScraperConst.USER_AGENT)
                .build()
            try {
                val a = packet.toByteString()
                logger.info { "packet info \n${Base64.encode(a.toByteArray())}" }
                val ws = client.newWebSocket(request, handler)
                ws.send(packet.toByteString())
                retry = 0
                return null
            } catch (e: Exception) {
                logger.warn(e) { "连接弹幕服务器失败，重试${retry}次" }
            }
            retry += 1
        }
        logger.error { "重试次数过多，放弃连接弹幕服务器" }
        return null
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
        val md5 = Md5Utils.encode(uri + sign).getOrNull() ?: return null
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
