/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * HMAC-SHA256 签名工具类
 */
object HmacSHAUtils {
    private const val HMAC_SHA256_ALGORITHM = "HmacSHA256"
    private val logger = KotlinLogging.logger {}

    /**
     * 使用 HMAC-SHA256 算法生成数字签名
     * 
     * @param raw 需要签名的原始数据
     * @param secretKey 用于签名的密钥
     * @return Result<String> 包含 Base64 编码的签名结果，如果失败则包含异常信息
     * 
     */
    fun generateHmacSha256Signature(raw: String, secretKey: String): Result<String> = runCatching {
        try {
            val mac = Mac.getInstance(HMAC_SHA256_ALGORITHM)
            val secretKeySpec = SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), HMAC_SHA256_ALGORITHM)
            mac.init(secretKeySpec)
            val rawHmac = mac.doFinal(raw.toByteArray(Charsets.UTF_8))
            Base64.getEncoder().encodeToString(rawHmac)
        } catch (e: Exception) {
            logger.error(e) { "生成 HMAC-SHA256 签名失败: $raw" }
            throw RuntimeException("生成 HMAC-SHA256 签名失败", e)
        }
    }

    /**
     * 验证 HMAC-SHA256 数字签名的有效性
     * 
     * @param raw 原始数据
     * @param signature 待验证的签名（Base64 编码格式）
     * @param secretKey 用于验证的密钥（必须与签名时使用的密钥相同）
     * @return Boolean 签名是否有效
     *         - true: 签名有效，数据未被篡改
     *         - false: 签名无效，数据可能被篡改或密钥不匹配
     */
    fun verifyHmacSha256Signature(raw: String, signature: String, secretKey: String): Boolean {
        return try {
            val expectedSignature = generateHmacSha256Signature(raw, secretKey)
            if (expectedSignature.isFailure) {
                return false
            }
            MessageDigest.isEqual(
                Base64.getDecoder().decode(signature),
                Base64.getDecoder().decode(expectedSignature.getOrElse { "" })
            )
        } catch (e: Exception) {
            logger.error(e) { "验证 HMAC-SHA256 签名失败: $raw" }
            false
        }
    }
}
