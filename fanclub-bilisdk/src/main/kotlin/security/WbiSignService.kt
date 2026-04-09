/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.security

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.cache.BiliSignCacheManager
import llh.fanclubvup.bilisdk.consts.BiliApiUrls
import llh.fanclubvup.bilisdk.consts.BiliSdkCacheKey
import llh.fanclubvup.bilisdk.dto.WbiUserInfoResponse
import llh.fanclubvup.bilisdk.http.BiliHttpClient
import llh.fanclubvup.bilisdk.utils.WbiUtil
import llh.fanclubvup.common.getOrNull
import llh.fanclubvup.common.utils.Md5Utils
import okhttp3.Request
import java.net.URLEncoder
import java.time.Instant
import java.util.*

/**
 * WBI 签名服务
 * 负责获取和管理 WBI 签名，构建带签名的查询字符串
 */
class WbiSignService(
    private val cacheManager: BiliSignCacheManager,
    private val httpClient: BiliHttpClient
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 获取 WBI 签名（带缓存）
     */
    fun getSignature(): String? {
        return cacheManager.getOrFetch(BiliSdkCacheKey.WBI_SIGN) {
            fetchWbiInfo().fold(
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
     * 构建带 WBI 签名的查询字符串
     */
    fun buildQueryString(params: TreeMap<String, String>): String? {
        val sign = getSignature() ?: return null

        params["wts"] = Instant.now().epochSecond.toString()

        val uri = params.entries.joinToString("&") { (k, v) ->
            "$k=${URLEncoder.encode(v, "utf-8")}"
        }

        val md5 = Md5Utils.encode(uri + sign).getOrNull(logger) ?: return null
        return "$uri&w_rid=$md5"
    }

    /**
     * 获取 WBI 信息
     */
    fun fetchWbiInfo(): Result<WbiUserInfoResponse> {
        val request = Request.Builder()
            .url(BiliApiUrls.WBI_INIT_URL)
            .build()
        return httpClient.execute(request, WbiUserInfoResponse::class.java)
    }
}
