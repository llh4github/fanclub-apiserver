/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.http

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilibili.constants.ApiConstants
import llh.fanclubvup.bilibili.dto.BiliBaseResponse
import llh.fanclubvup.bilibili.dto.DanmuInfoResponse
import llh.fanclubvup.bilibili.dto.WbiInfoResponse
import llh.fanclubvup.bilibili.exception.BiliApiException
import llh.fanclubvup.bilibili.exception.BiliException
import llh.fanclubvup.bilibili.props.BiliClientConfig
import llh.fanclubvup.bilibili.utils.JsonUtils
import llh.fanclubvup.bilibili.wbi.buildQueryString
import llh.fanclubvup.bilibili.wbi.wbiSign
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * B站 HTTP 客户端
 * 用于发送 HTTP 请求，获取 B站 API 数据
 * 
 * @param config 客户端配置，包含 cookies 等信息
 * @param enableLogging 是否启用日志
 */
class BiliHttpClient(config: BiliClientConfig? = null, enableLogging: Boolean = false) {
    private val logger = KotlinLogging.logger {}
    
    /**
     * OkHttp 客户端
     * 使用 lazy 初始化，避免在不需要时创建
     */
    private val client by lazy {
        val builder = OkHttpClient.Builder().callTimeout(10, TimeUnit.SECONDS)

        // 添加日志拦截器，方便调试
        if (enableLogging) {
            val loggingInterceptor = HttpLoggingInterceptor { message ->
                logger.info { "[OkHttp] $message" }
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        // 添加 cookie 拦截器，用于身份认证
        config?.let {
            val cookieHeader = it.cookies.joinToString(";") { cookie -> "${cookie.name}=${cookie.value}" }
            if (cookieHeader.isNotEmpty()) {
                builder.addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("Cookie", cookieHeader)
                        .build()
                    chain.proceed(request)
                }
            }
        }
        builder.build()
    }
    
    /**
     * JSON 映射器
     * 使用项目统一的 JsonUtils.mapper，确保配置一致
     */
    private val mapper = JsonUtils.mapper
    
    /**
     * WBI 签名缓存
     * 避免重复获取，提高性能
     */
    private var wbiSignCache: String? = null

    /**
     * 执行 GET 请求
     * 
     * @param url 请求 URL
     * @param clazz 响应类型
     * @return 响应结果
     */
    fun <T> executeGet(url: String, clazz: Class<T>): Result<T> {
        return try {
            // 创建请求，设置 User-Agent
            val request = Request.Builder().url(url).header("User-Agent", ApiConstants.USER_AGENT).get().build()
            client.newCall(request).execute().use { response ->
                // 检查 HTTP 响应状态
                if (!response.isSuccessful) return Result.failure(Exception("HTTP ${response.code}"))
                val body = response.body.string()
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

    /**
     * 获取 WBI 签名
     * 
     * WBI 签名是 B站 API 调用的必要参数，用于防止 API 滥用
     * 
     * @return WBI 签名
     */
    fun getWbiSign(): Result<String> {
        // 使用缓存，避免重复获取
        wbiSignCache?.let { return Result.success(it) }
        return executeGet(ApiConstants.WBI_INIT_URL, WbiInfoResponse::class.java).map { response ->
            // 获取 WBI 图片信息，用于生成签名
            val wbiImg = response.data?.wbiImg ?: throw BiliException("获取 WBI 图片信息失败")
            val sign = wbiSign(wbiImg)
            wbiSignCache = sign
            sign
        }
    }

    /**
     * 获取弹幕服务器信息
     * 
     * 弹幕服务器信息是建立 WebSocket 连接的前提
     * 
     * @param roomId 房间 ID
     * @return 弹幕服务器信息
     */
    fun fetchDanmuServerInfo(roomId: Long): Result<DanmuInfoResponse?> {
        // 获取 WBI 签名
        val sign = getWbiSign().getOrElse { return Result.failure(it) }
        // 构建请求参数
        val params = TreeMap<String, String>().apply { put("id", roomId.toString()); put("type", "0") }
        // 构建查询字符串
        val queryString = buildQueryString(params, sign)
        val url = "${ApiConstants.DANMAKU_SERVER_CONF_URL}?$queryString"
        return executeGet(url, DanmuInfoResponse::class.java)
    }
}
