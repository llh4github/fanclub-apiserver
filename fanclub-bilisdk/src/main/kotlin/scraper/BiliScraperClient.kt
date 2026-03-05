package llh.fanclubvup.bilisdk.scraper

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.cache.BiliSignCacheManager
import llh.fanclubvup.bilisdk.cache.PersistentCookieJarManager
import llh.fanclubvup.bilisdk.consts.BiliApiUrls
import llh.fanclubvup.bilisdk.consts.BiliSdkCacheKey
import llh.fanclubvup.bilisdk.consts.ScraperConst
import llh.fanclubvup.bilisdk.dto.ScraperBaseResp
import llh.fanclubvup.bilisdk.dto.UserInfoResponse
import llh.fanclubvup.bilisdk.dto.DanmuInfoResponse
import llh.fanclubvup.bilisdk.utils.WbiUtil
import llh.fanclubvup.common.excptions.AppRuntimeException
import llh.fanclubvup.common.utils.Md5Utils
import okhttp3.OkHttpClient
import okhttp3.Request
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.net.URLEncoder
import java.time.Instant
import java.util.*

class BiliScraperClient(
    private val cacheManager: BiliSignCacheManager,
    private val persistentCookieJarManager: PersistentCookieJarManager
) {

    private val client by lazy {
        OkHttpClient.Builder()
            .cookieJar(persistentCookieJarManager)
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
    fun wbiInfo(): Result<UserInfoResponse> {
        val request = Request.Builder()
            .url(BiliApiUrls.WBI_INIT_URL)
            .build()
        return execute(request, UserInfoResponse::class.java)
    }

    private fun <T : ScraperBaseResp> execute(request: Request, clazz: Class<T>): Result<T> =
        runCatching {
            client.newCall(request)
                .execute().use { response ->
                    if (!response.isSuccessful) {
                        logger.error { "${request.url} 响应结果： $response" }
                        throw AppRuntimeException("${request.url}请求失败")
                    }

                    val rs = mapper.readValue(response.body.string(), clazz)
                    return@runCatching rs
                }
        }
}
