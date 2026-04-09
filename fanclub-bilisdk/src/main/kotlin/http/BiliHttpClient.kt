/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.http

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.cache.PersistentCookieJarManager
import llh.fanclubvup.bilisdk.consts.ScraperConst
import llh.fanclubvup.bilisdk.dto.ScraperBaseResp
import llh.fanclubvup.common.excptions.AppRuntimeException
import okhttp3.OkHttpClient
import okhttp3.Request
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.Duration

/**
 * B站 HTTP 客户端
 * 负责执行 HTTP 请求和响应解析
 */
class BiliHttpClient(
    val cookieJarManager: PersistentCookieJarManager
) {
    private val logger = KotlinLogging.logger {}
    private val mapper = jacksonObjectMapper()

    private val client by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJarManager)
            .callTimeout(Duration.ofSeconds(3))
            .build()
    }

    /**
     * 执行 HTTP 请求并解析响应
     */
    fun <T : ScraperBaseResp> execute(request: Request, clazz: Class<T>): Result<T> = runCatching {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                logger.error { "${request.url} 响应失败:\n${response.body.string()}" }
                throw AppRuntimeException("${request.url}请求失败")
            }
            mapper.readValue(response.body.string(), clazz)
        }
    }

    /**
     * 获取 OkHttpClient 实例（用于 WebSocket 连接）
     */
    val okHttpClient: OkHttpClient get() = client

    /**
     * 构建请求构建器（带默认请求头）
     */
    fun requestBuilder(url: String): Request.Builder = Request.Builder()
        .url(url)
        .addHeader("User-Agent", ScraperConst.USER_AGENT)
        .addHeader("Accept", "application/json")
}
