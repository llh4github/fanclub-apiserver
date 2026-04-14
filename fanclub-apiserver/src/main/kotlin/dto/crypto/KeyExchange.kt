package llh.fanclubvup.apiserver.dto.crypto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 密钥交换请求
 * @param sessionId 会话ID，用于标识当前加密会话
 */
@Schema(description = "密钥交换请求")
data class KeyExchangeRequest(
    @field:Schema(description = "会话ID，用于标识当前加密会话", example = "user123session")
    @field:Size(min = 1, max = 30, message = "会话ID长度必须在1-30个字符之间")
    @field:Pattern(regexp = "^[a-zA-Z0-9]+$", message = "会话ID只能包含英文字母和数字")
    val sessionId: String
)

/**
 * 密钥交换响应
 * @param publicKey Base64 编码的 RSA 公钥，前端用它加密 AES 密钥
 */
@Schema(description = "密钥交换响应")
data class KeyExchangeResponse(
    @field:Schema(description = "Base64 编码的 RSA 公钥", example = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...")
    val publicKey: String
)

/**
 * 完成密钥交换请求
 * @param sessionId 会话ID，与初始化时保持一致
 * @param encryptedAesKey 用 RSA 公钥加密后的 AES 密钥（Base64 编码）
 */
@Schema(description = "完成密钥交换请求")
data class KeyExchangeCompleteRequest(
    @field:Schema(description = "会话ID，与初始化时保持一致", example = "user123session")
    @field:Size(min = 1, max = 30, message = "会话ID长度必须在1-30个字符之间")
    @field:Pattern(regexp = "^[a-zA-Z0-9]+$", message = "会话ID只能包含英文字母和数字")
    val sessionId: String,
    @field:Schema(description = "用 RSA 公钥加密后的 AES 密钥（Base64 编码）", example = "abc123xyz...")
    val encryptedAesKey: String
)
