package llh.fanclubvup.bilisdk.scraper

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.consts.BiliApiUrls
import llh.fanclubvup.bilisdk.consts.ScraperConst
import llh.fanclubvup.bilisdk.dto.ScraperBaseResp
import llh.fanclubvup.common.excptions.AppRuntimeException
import okhttp3.OkHttpClient
import okhttp3.Request
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.util.concurrent.TimeUnit

class BiliScraperClient {

    private val client = OkHttpClient()
    private val mapper = jacksonObjectMapper()
    private val logger = KotlinLogging.logger {}

    fun wbi(): Result<ScraperBaseResp> {
        val request = Request.Builder()
            .url(BiliApiUrls.WBI_INIT_URL)
            .addHeader("User-Agent", ScraperConst.USER_AGENT)
            .addHeader("Cookie", "SESSDATA=your_sessdata")
            .build()
        return execute(request, ScraperBaseResp::class.java)
    }

    fun <T : ScraperBaseResp> execute(request: Request, clazz: Class<T>): Result<T> =
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
