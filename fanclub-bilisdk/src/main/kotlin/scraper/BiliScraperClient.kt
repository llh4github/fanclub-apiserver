package llh.fanclubvup.bilisdk.scraper

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.cache.BiliSignCacheManager
import llh.fanclubvup.bilisdk.consts.BiliApiUrls
import llh.fanclubvup.bilisdk.consts.BiliSdkCacheKey
import llh.fanclubvup.bilisdk.consts.ScraperConst
import llh.fanclubvup.bilisdk.dto.ScraperBaseResp
import llh.fanclubvup.bilisdk.dto.UserInfoResponse
import llh.fanclubvup.bilisdk.utils.WbiUtil
import llh.fanclubvup.common.excptions.AppRuntimeException
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.cache.CacheManager
import org.springframework.cache.get
import org.springframework.cache.set
import tools.jackson.module.kotlin.jacksonObjectMapper

class BiliScraperClient(
    private val cacheManager: BiliSignCacheManager
) {

    private val client = OkHttpClient()
    private val mapper = jacksonObjectMapper()
    private val logger = KotlinLogging.logger {}

    /**
     * 获取 WBI 签名
     */
    fun wbiSign(cookie: String): String? {
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
