package llh.fanclubvup.bilibili.http

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilibili.constants.ApiConstants
import llh.fanclubvup.bilibili.dto.BiliBaseResponse
import llh.fanclubvup.bilibili.dto.DanmuInfoResponse
import llh.fanclubvup.bilibili.dto.WbiInfoResponse
import llh.fanclubvup.bilibili.exception.BiliApiException
import llh.fanclubvup.bilibili.utils.JsonUtils
import llh.fanclubvup.bilibili.wbi.buildQueryString
import llh.fanclubvup.bilibili.wbi.wbiSign
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.util.*
import java.util.concurrent.TimeUnit

class BiliHttpClient(cookie: String? = null, enableLogging: Boolean = true) {
    private val logger = KotlinLogging.logger {}
    private val client by lazy {
        val builder = OkHttpClient.Builder().callTimeout(10, TimeUnit.SECONDS)

        // 添加日志拦截器
        if (enableLogging) {
            val loggingInterceptor = HttpLoggingInterceptor { message ->
                logger.info { "[OkHttp] $message" }
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        cookie?.let {
            builder.addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Cookie", it)
                    .build()
                chain.proceed(request)
            }
        }
        builder.build()
    }
    private val mapper = JsonUtils.mapper
    private var wbiSignCache: String? = null

    fun <T> executeGet(url: String, clazz: Class<T>): Result<T> {
        return try {
            val request = Request.Builder().url(url).header("User-Agent", ApiConstants.USER_AGENT).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return Result.failure(Exception("HTTP ${response.code}"))
                val body = response.body.string() ?: ""
                val result = mapper.readValue(body, clazz)

                // 检查 B站 API 响应码
                if (result is BiliBaseResponse && result.code != 0) {
                    return Result.failure(BiliApiException(result.code, result.message))
                }

                Result.success(result)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getWbiSign(): Result<String> {
        wbiSignCache?.let { return Result.success(it) }
        return executeGet(ApiConstants.WBI_INIT_URL, WbiInfoResponse::class.java).map { response ->
            val wbiImg = response.data?.wbiImg ?: throw IllegalStateException("获取 WBI 图片信息失败")
            val sign = wbiSign(wbiImg)
            wbiSignCache = sign
            sign
        }
    }

    fun fetchDanmuServerInfo(roomId: Long): Result<DanmuInfoResponse?> {
        val sign = getWbiSign().getOrElse { return Result.failure(it) }
        val params = TreeMap<String, String>().apply { put("id", roomId.toString()); put("type", "0") }
        val queryString = buildQueryString(params, sign)
        val url = "${ApiConstants.DANMAKU_SERVER_CONF_URL}?$queryString"
        return executeGet(url, DanmuInfoResponse::class.java)
    }
}
