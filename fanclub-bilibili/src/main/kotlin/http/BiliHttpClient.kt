package llh.fanclubvup.bilibili.http

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilibili.constants.ApiConstants
import llh.fanclubvup.bilibili.dto.DanmuInfoResponse
import llh.fanclubvup.bilibili.dto.WbiInfoResponse
import llh.fanclubvup.bilibili.wbi.buildQueryString
import llh.fanclubvup.bilibili.wbi.wbiSign
import okhttp3.OkHttpClient
import okhttp3.Request
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.util.*
import java.util.concurrent.TimeUnit

class BiliHttpClient {
    private val logger = KotlinLogging.logger {}
    private val client by lazy { OkHttpClient.Builder().callTimeout(10, TimeUnit.SECONDS).build() }
    private val mapper = jacksonObjectMapper()
    private var wbiSignCache: String? = null

    fun <T> executeGet(url: String, clazz: Class<T>): Result<T> {
        return try {
            val request = Request.Builder().url(url).header("User-Agent", ApiConstants.USER_AGENT).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return Result.failure(Exception("HTTP ${response.code}"))
                val body = response.body.string() ?: ""
                Result.success(mapper.readValue(body, clazz))
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
