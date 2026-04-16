/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.common

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.dto.crypto.KeyExchangeResponse
import llh.fanclubvup.apiserver.utils.IdGenerator
import llh.fanclubvup.common.consts.CacheKeyPrefix
import llh.fanclubvup.common.excptions.AppRuntimeException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Service
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.time.Duration
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64

/**
 * AES-GCM 加解密服务
 * 提供安全的对称加密功能，GCM模式提供认证加密（AEAD）
 */
@Service
class CryptoService(
    private val redisTemplate: StringRedisTemplate
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
        private const val RSA_ALGORITHM = "RSA"
        private const val RSA_KEY_SIZE = 2048
        // 使用与前端Web Crypto API兼容的RSA算法
        private const val RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding"

        private const val SESSION_KEY_PREFIX = CacheKeyPrefix.CRYPTO_KEY + "session:"
        private const val RSA_PUBLIC_KEY_PREFIX = CacheKeyPrefix.CRYPTO_KEY + "rsa:public:"
        private const val RSA_PRIVATE_KEY_PREFIX = CacheKeyPrefix.CRYPTO_KEY + "rsa:private:"
        private val DEFAULT_TTL = Duration.ofHours(25)
    }

    @Autowired
    @Qualifier("deleteByPattern")
    private lateinit var deleteByPattern: DefaultRedisScript<Long>

    /**
     * 生成随机 AES 密钥
     * @return SecretKey AES密钥
     */
    fun generateAesKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
        keyGenerator.init(KEY_SIZE)
        return keyGenerator.generateKey()
    }

    /**
     * 从字节数组恢复 AES 密钥
     * @param keyBytes 密钥字节数组
     * @return SecretKey AES密钥
     */
    private fun restoreAesKey(keyBytes: ByteArray): SecretKey {
        return SecretKeySpec(keyBytes, ALGORITHM)
    }

    /**
     * AES-GCM 加密数据
     * @param plaintext 明文数据
     * @param key 加密密钥
     * @return 包含IV和密文的字节数组 (IV + ciphertext)
     */
    fun encrypt(plaintext: ByteArray, key: SecretKey): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)

        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)

        val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec)

        val ciphertext = cipher.doFinal(plaintext)

        return iv + ciphertext
    }

    /**
     * AES-GCM 加密字符串
     * @param plaintext 明文字符串
     * @param key 加密密钥
     * @return Base64 编码的加密数据
     */
    fun encryptString(plaintext: String, key: SecretKey): String {
        val encrypted = encrypt(plaintext.toByteArray(Charsets.UTF_8), key)
        return Base64.encode(encrypted)
    }

    /**
     * AES-GCM 解密数据
     * @param encryptedData 加密数据 (IV + ciphertext)
     * @param key 解密密钥
     * @return 明文数据
     */
    fun decrypt(encryptedData: ByteArray, key: SecretKey): ByteArray {
        if (encryptedData.size < GCM_IV_LENGTH) {
            throw IllegalArgumentException("加密数据长度不足，无法提取IV")
        }

        val iv = encryptedData.copyOfRange(0, GCM_IV_LENGTH)
        val ciphertext = encryptedData.copyOfRange(GCM_IV_LENGTH, encryptedData.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec)

        return cipher.doFinal(ciphertext)
    }

    /**
     * AES-GCM 解密 Base64 字符串
     * @param encryptedDataBase64 Base64 编码的加密数据
     * @param key 解密密钥
     * @return 明文字符串
     */
    fun decryptToString(encryptedDataBase64: String, key: SecretKey): String {
        val encryptedData = Base64.decode(encryptedDataBase64)
        return String(decrypt(encryptedData, key), Charsets.UTF_8)
    }

    /**
     * 将 AES 密钥转换为 Base64
     * @param key 密钥
     * @return Base64 字符串
     */
    fun aesKeyToBase64(key: SecretKey): String {
        return Base64.encode(key.encoded)
    }

    /**
     * 从 Base64 恢复 AES 密钥
     * @param base64Key Base64 编码的密钥
     * @return SecretKey
     */
    fun aesKeyFromBase64(base64Key: String): SecretKey {
        val keyBytes = Base64.decode(base64Key)
        return restoreAesKey(keyBytes)
    }

    // ==================== RSA 密钥管理 ====================

    /**
     * 生成 RSA 密钥对
     * @return Pair<PublicKey, PrivateKey>
     */
    fun generateRsaKeyPair(): Pair<PublicKey, PrivateKey> {
        val keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM)
        keyPairGenerator.initialize(RSA_KEY_SIZE)
        val keyPair = keyPairGenerator.generateKeyPair()
        return Pair(keyPair.public, keyPair.private)
    }

    /**
     * 为会话生成 RSA 密钥对并存储到 Redis
     * @param sessionId 会话ID
     * @param ttl 过期时间
     * @return Base64 编码的公钥
     */
    fun generateAndStoreRsaKeys(sessionId: String, ttl: Duration = DEFAULT_TTL): String {
        val (publicKey, privateKey) = generateRsaKeyPair()

        val publicKeyBase64 = Base64.encode(publicKey.encoded)
        val privateKeyBase64 = Base64.encode(privateKey.encoded)

        redisTemplate.opsForValue().set(
            "${RSA_PUBLIC_KEY_PREFIX}$sessionId",
            publicKeyBase64,
            ttl
        )
        redisTemplate.opsForValue().set(
            "${RSA_PRIVATE_KEY_PREFIX}$sessionId",
            privateKeyBase64,
            ttl
        )

        return publicKeyBase64
    }

    /**
     * 从 Redis 获取 RSA 私钥
     * @param sessionId 会话ID
     * @return PrivateKey 或 null
     */
    private fun getRsaPrivateKey(sessionId: String): PrivateKey? {
        val privateKeyBase64 = redisTemplate.opsForValue()
            .get("${RSA_PRIVATE_KEY_PREFIX}$sessionId") ?: return null

        val privateKeyBytes = Base64.decode(privateKeyBase64)
        val keyFactory = java.security.KeyFactory.getInstance(RSA_ALGORITHM)
        val keySpec = java.security.spec.PKCS8EncodedKeySpec(privateKeyBytes)
        return keyFactory.generatePrivate(keySpec)
    }

    /**
     * 用 RSA 私钥解密 AES 密钥
     * @param encryptedAesKeyBase64 Base64 编码的加密 AES 密钥
     * @param sessionId 会话ID
     * @return AES 密钥或 null
     */
    fun decryptAesKey(encryptedAesKeyBase64: String, sessionId: String): SecretKey? {
        val privateKey = getRsaPrivateKey(sessionId) ?: run {
            logger.warn { "未找到会话 [$sessionId] 的 RSA 私钥" }
            return null
        }

        return try {
            val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
            // 使用默认的 OAEP 参数，与前端 Web Crypto API 保持一致
            cipher.init(Cipher.DECRYPT_MODE, privateKey)

            val encryptedAesKey = Base64.decode(encryptedAesKeyBase64)
            logger.debug {
                "开始解密 AES 密钥 - 会话ID: $sessionId, " +
                        "加密数据长度: ${encryptedAesKey.size}字节, " +
                        "加密算法: $RSA_TRANSFORMATION"
            }

            val aesKeyBytes = cipher.doFinal(encryptedAesKey)
            logger.debug { "AES 密钥解密成功，密钥长度: ${aesKeyBytes.size}字节" }

            restoreAesKey(aesKeyBytes)
        } catch (e: Exception) {
            logger.error(e) {
                "RSA 解密失败 - 会话ID: $sessionId, " +
                        "可能原因: 1)前端使用了错误的公钥 2)加密算法不匹配 3)数据被篡改\n" +
                        "期望算法: $RSA_TRANSFORMATION"
            }
            null
        }
    }

    // ==================== 会话密钥管理 ====================

    /**
     * 保存会话 AES 密钥到 Redis
     * @param sessionId 会话ID
     * @param key AES 密钥
     * @param ttl 过期时间
     */
    fun saveSessionKey(sessionId: String, key: SecretKey, ttl: Duration = DEFAULT_TTL) {
        val base64Key = aesKeyToBase64(key)
        redisTemplate.opsForValue().set("${SESSION_KEY_PREFIX}$sessionId", base64Key, ttl)
    }

    /**
     * 从 Redis 获取会话 AES 密钥
     * @param sessionId 会话ID
     * @return AES 密钥或 null
     */
    fun getSessionKey(sessionId: String): SecretKey? {
        val base64Key = redisTemplate.opsForValue()
            .get("${SESSION_KEY_PREFIX}$sessionId") ?: return null
        return aesKeyFromBase64(base64Key)
    }

    /**
     * 删除会话密钥
     * @param sessionId 会话ID
     */
    fun deleteSessionKey(sessionId: String) {
        redisTemplate.execute(
            deleteByPattern,
            listOf(
                "${SESSION_KEY_PREFIX}$sessionId",
                "${RSA_PUBLIC_KEY_PREFIX}$sessionId",
                "${RSA_PRIVATE_KEY_PREFIX}$sessionId",
            ), ""
        )
    }

    /**
     * 完整的密钥交换流程：生成 RSA 密钥对并返回公钥
     * @param sessionId 会话ID
     * @return RSA 公钥（Base64）
     */
    fun initiateKeyExchange(sessionId: String): String {
        return generateAndStoreRsaKeys(sessionId)
    }

    fun initiateKeyExchange(): KeyExchangeResponse {
        val sessionId = IdGenerator.nextShortId()
        return KeyExchangeResponse(
            cryptoSid = sessionId,
            publicKey = initiateKeyExchange(sessionId)
        )
    }

    /**
     * 完成密钥交换：解密前端传来的 AES 密钥并保存
     * @param sessionId 会话ID
     * @param encryptedAesKeyBase64 加密的 AES 密钥（Base64）
     * @return 是否成功
     */
    fun completeKeyExchange(sessionId: String, encryptedAesKeyBase64: String): Boolean {
        val aesKey = decryptAesKey(encryptedAesKeyBase64, sessionId) ?: run {
            deleteSessionKey(sessionId)
            throw AppRuntimeException("AES 密钥解密失败")
        }
        saveSessionKey(sessionId, aesKey)
        return true
    }

    /**
     * 使用会话密钥加密数据
     * @param sessionId 会话ID
     * @param plaintext 明文
     * @return 加密后的 Base64 字符串
     */
    fun encryptWithSessionKey(sessionId: String, plaintext: String): String? {
        val key = getSessionKey(sessionId) ?: return null
        return encryptString(plaintext, key)
    }

    /**
     * 使用会话密钥解密数据
     * @param sessionId 会话ID
     * @param encryptedDataBase64 加密数据（Base64）
     * @return 明文或 null
     */
    fun decryptWithSessionKey(sessionId: String, encryptedDataBase64: String): String? {
        val key = getSessionKey(sessionId) ?: return null
        return decryptToString(encryptedDataBase64, key)
    }
}

