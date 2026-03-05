package llh.fanclubvup.bilisdk.scraper

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.cache.BiliSignCacheManager
import llh.fanclubvup.bilisdk.cache.PersistentCookieJar
import llh.fanclubvup.bilisdk.consts.BiliApiUrls
import llh.fanclubvup.bilisdk.consts.BiliSdkCacheKey
import llh.fanclubvup.bilisdk.consts.ScraperConst
import llh.fanclubvup.bilisdk.dto.ScraperBaseResp
import llh.fanclubvup.bilisdk.dto.UserInfoResponse
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
    private val cacheManager: BiliSignCacheManager
) {

    private val client by lazy {
        OkHttpClient.Builder()
            .cookieJar(PersistentCookieJar)
            .build()
    }
    private val mapper = jacksonObjectMapper()
    private val logger = KotlinLogging.logger {}

    /**
     * 获取 WBI 签名
     */
    fun wbiSign(cookie: String): String? {

        val request = Request.Builder()
        request.url(BiliApiUrls.WBI_INIT_URL)

        return cacheManager.get(BiliSdkCacheKey.WBI_SIGN) {
            wbiInfo(cookie).fold(
                onSuccess = { response ->
                    response.data?.wbiImg?.let { wbiImg ->
                        WbiUtil.wbiSign(wbiImg)
                    }
                },
                onFailure = { null }
            )
        }
    }

    fun fetchDanmuInfo() {
        val cookie = ""
        val params = TreeMap<String, String>().apply {
            put("id", "6")
            put("type", "0")
        }
        val queryString = buildQueryString(cookie, params) ?: return
        val request = Request.Builder()
            .url(BiliApiUrls.DANMAKU_SERVER_CONF_URL + "?" + queryString)
            .addHeader("User-Agent", ScraperConst.USER_AGENT)
            .addHeader("SESSDATA", cookie)
            .build()

        client
            .newCall(request)
            .execute().use { response ->
                logger.debug { "${request.url} 响应结果： $response" }
            }
    }

    private fun buildQueryString(cookie: String, map: TreeMap<String, String>): String? {
        val sign = wbiSign(cookie) ?: return null
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
    fun wbiInfo(cookie: String): Result<UserInfoResponse> {
        val request = Request.Builder()
            .url(BiliApiUrls.WBI_INIT_URL)
            .addHeader("User-Agent", ScraperConst.USER_AGENT)
            .addHeader("SESSDATA", cookie)
            .build()
        return execute(request, UserInfoResponse::class.java)
    }

    private fun <T : ScraperBaseResp> execute(request: Request, clazz: Class<T>): Result<T> =
        runCatching {
            client.newCall(request).execute().use { response ->
                logger.debug { "${request.url} 响应结果： $response" }
                if (!response.isSuccessful) {
                    logger.error { "${request.url} 响应结果： $response" }
                    throw AppRuntimeException("${request.url}请求失败")
                }

                val rs = mapper.readValue(response.body.string(), clazz)
                return@runCatching rs
            }
        }
}
