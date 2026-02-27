/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.http

import llh.fanclubvup.common.utils.HmacSHAUtils
import okhttp3.Request
import java.util.UUID
import kotlin.time.Clock

/**
 * B 站直播开放 API 客户端
 *
 *  [接口文档](https://open-live.bilibili.com/document/74eec767-e594-7ddd-6aba-257e8317c05d#h1-u5FEBu901Fu5F00u59CB)
 */
data class BiliLiveApiHeaderBuilder(
    val accessKeyId: String,
    val contentMd5: String,
    val signatureKey: String,
    val signatureNonce: String = UUID.randomUUID().toString(),
    val signatureVersion: String = "1.0",
    val signatureMethod: String = "HMAC-SHA256",
    val timestamp: Long = Clock.System.now().epochSeconds
) {

    private fun signatureRaw() =
        """x-bili-accesskeyid:$accessKeyId
            x-bili-content-md5:$contentMd5
            x-bili-signature-method:$signatureMethod
            x-bili-signature-nonce:$signatureNonce
            x-bili-signature-version:$signatureVersion
            x-bili-timestamp:$timestamp""".trimIndent()

    /**
     * 填充请求头
     * @return 是否填充成功
     */
    fun fillHeader(request: Request.Builder) =
        HmacSHAUtils.generateHmacSha256Signature(signatureRaw(), signatureKey)
            .fold(
                onSuccess = {
                    request
                        .addHeader("x-bili-accesskeyid", accessKeyId)
                        .addHeader("x-bili-content-md5", contentMd5)
                        .addHeader("x-bili-signature-method", signatureMethod)
                        .addHeader("x-bili-signature-nonce", signatureNonce)
                        .addHeader("x-bili-signature-version", signatureVersion)
                        .addHeader("x-bili-timestamp", timestamp.toString())
                        .addHeader("Authorization", it)
                    true
                },
                onFailure = {
                    false
                })

}
