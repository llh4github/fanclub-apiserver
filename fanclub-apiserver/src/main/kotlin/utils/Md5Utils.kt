/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.charset.Charset
import java.security.MessageDigest

/**
 * MD5 哈希编码工具类
 * 
 * 提供 MD5 哈希算法的相关功能，包括字符串 MD5 编码、文件 MD5 校验等
 * 
 * 注意：MD5 算法已被认为不够安全，不建议用于密码存储等安全敏感场景，
 * 建议使用 bcrypt、scrypt 或 Argon2 等更安全的哈希算法
 */
object Md5Utils {
    private const val MD5_ALGORITHM = "MD5"
    private val logger = KotlinLogging.logger {}

    /**
     * 对字符串进行 MD5 编码
     * 
     * @param input 需要编码的字符串
     * @param charset 字符集，默认为 UTF-8
     * @return String 32位小写十六进制格式的 MD5 哈希值
     */
    fun encode(input: String, charset: Charset = Charsets.UTF_8): Result<String> = runCatching {
        try {
            val md = MessageDigest.getInstance(MD5_ALGORITHM)
            val digest = md.digest(input.toByteArray(charset))
            bytesToHex(digest)
        } catch (e: Exception) {
            logger.error(e) { "MD5 编码失败: $input" }
            throw RuntimeException("MD5 编码失败", e)
        }
    }

    /**
     * 对字节数组进行 MD5 编码
     *
     * @param input 需要编码的字节数组
     * @return String 32位小写十六进制格式的 MD5 哈希值
     */
    fun encodeBytes(input: ByteArray): Result<String> = runCatching {
        try {
            val md = MessageDigest.getInstance(MD5_ALGORITHM)
            val digest = md.digest(input)
            bytesToHex(digest)
        } catch (e: Exception) {
            logger.error(e) { "MD5 字节数组编码失败" }
            throw RuntimeException("MD5 字节数组编码失败", e)
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 输入的字节数组
     * @return String 十六进制字符串（小写）
     */
    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

}