/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.http

import llh.fanclubvup.utils.HmacSHAUtils
import org.springframework.web.client.RestClient
import java.util.*
import kotlin.time.Clock

data class BiliLiveOpenApiHeaderBuilder(
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


    fun fillHeader(builder: RestClient.RequestBodySpec): Boolean {
        HmacSHAUtils.generateHmacSha256Signature(signatureRaw(), signatureKey)
            .onSuccess {
                builder.header("Authorization", it)
            }.onFailure {
                return false
            }
        builder
            .header("x-bili-accesskeyid", accessKeyId)
            .header("x-bili-content-md5", contentMd5)
            .header("x-bili-signature-method", signatureMethod)
            .header("x-bili-signature-nonce", signatureNonce)
            .header("x-bili-signature-version", signatureVersion)
            .header("x-bili-timestamp", timestamp.toString())
        return true
    }
}
