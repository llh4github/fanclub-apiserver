package llh.fanclubvup.apiserver.service

import llh.fanclubvup.apiserver.service.common.CryptoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import kotlin.io.encoding.Base64
import kotlin.test.Test


@SpringBootTest
@ActiveProfiles("docker")
class CryptoServiceTest {
    @Autowired
    private lateinit var service: CryptoService

    /**
     * 模拟前端：用 RSA 公钥加密数据
     */
    fun encryptWithRsaPublicKey(data: ByteArray, publicKeyBase64: String): String {
        val publicKeyBytes = Base64.decode(publicKeyBase64)
        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = X509EncodedKeySpec(publicKeyBytes)
        val publicKey = keyFactory.generatePublic(keySpec)

        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encrypted = cipher.doFinal(data)

        return Base64.encode(encrypted)
    }

    @Test
    fun test_key_exchange() {
        val sessionId = "test12345"

        // 1. 后端生成 RSA 密钥对，返回公钥
        val publicKeyBase64 = service.initiateKeyExchange(sessionId)

        // 2. 前端：生成 AES 密钥并用 RSA 公钥加密（模拟前端行为）
        val aesKey = service.generateAesKey()
        val encryptedAesKey = encryptWithRsaPublicKey(aesKey.encoded, publicKeyBase64)

        // 3. 前端将加密的 AES 密钥发送给后端
        val success = service.completeKeyExchange(sessionId, encryptedAesKey)
        assert(success) { "密钥交换失败" }

        // 4. 验证：使用会话密钥加密和解密数据
        val originalText = "Hello, World! 你好世界！"
        val encrypted = service.encryptWithSessionKey(sessionId, originalText)
        assert(encrypted != null) { "加密失败" }

        val decrypted = service.decryptWithSessionKey(sessionId, encrypted!!)
        assert(decrypted == originalText) {
            "解密结果不匹配: expected='$originalText', actual='$decrypted'"
        }

        println("✅ 密钥交换和加解密测试通过")
        println("原始文本: $originalText")
        println("加密后: $encrypted")
        println("解密后: $decrypted")

        // 5. 验证加密数据长度关系
        testEncryptionLength()
    }

    /**
     * 测试 AES-GCM 加密数据长度关系
     * 公式: 加密后长度 = IV(12) + 密文(=明文长度) + GCM标签(16) = 明文长度 + 28
     */
    @Test
    fun testEncryptionLength() {
        val sessionId = "test-length"
        service.initiateKeyExchange(sessionId)

        // 生成并设置 AES 密钥
        val aesKey = service.generateAesKey()
        val encryptedAesKey = encryptWithRsaPublicKey(
            aesKey.encoded,
            service.initiateKeyExchange(sessionId)
        )
        service.completeKeyExchange(sessionId, encryptedAesKey)

        // 测试不同长度的明文
        val testCases = listOf(
            "" to 0,
            "A" to 1,
            "Hello" to 5,
            "Hello, World!" to 13,
            "你好世界" to 12, // UTF-8: 每个中文3字节
            "A".repeat(100) to 100
        )

        println("\n📏 AES-GCM 加密长度测试:")
        println("公式: 加密后长度 = 明文长度 + 28 (IV:12 + GCM标签:16)")
        println("-".repeat(60))

        for ((text, expectedLength) in testCases) {
            val plaintextBytes = text.toByteArray(Charsets.UTF_8)
            val actualPlaintextLength = plaintextBytes.size

            // 直接调用 encrypt 方法获取原始字节数组
            val encryptedBytes = service.encrypt(plaintextBytes, aesKey)
            val encryptedLength = encryptedBytes.size

            // Base64 编码后的长度（实际传输时的长度）
            val base64Length = kotlin.io.encoding.Base64.encode(encryptedBytes).length

            val overhead = encryptedLength - actualPlaintextLength

            println(
                "明文: ${actualPlaintextLength}字节 | " +
                        "加密后: ${encryptedLength}字节 | " +
                        "开销: ${overhead}字节 | " +
                        "Base64: ${base64Length}字节 | " +
                        "内容: '${text.take(20)}${if (text.length > 20) "..." else ""}'"
            )

            assert(overhead == 28) {
                "加密开销应该是28字节，实际是: $overhead"
            }
        }

        println("-".repeat(60))
        println("✅ 所有长度测试通过！")

    }
}