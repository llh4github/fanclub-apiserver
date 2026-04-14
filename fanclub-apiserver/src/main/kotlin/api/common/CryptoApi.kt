package llh.fanclubvup.apiserver.api.common

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import llh.fanclubvup.apiserver.dto.JsonWrapper
import llh.fanclubvup.apiserver.dto.crypto.KeyExchangeCompleteRequest
import llh.fanclubvup.apiserver.dto.crypto.KeyExchangeRequest
import llh.fanclubvup.apiserver.dto.crypto.KeyExchangeResponse
import llh.fanclubvup.apiserver.service.common.CryptoService
import llh.fanclubvup.ksp.annon.PublicAccessUrl
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "加密服务", description = "前后端密钥交换和数据加密")
@RestController
@RequestMapping("/crypto")
class CryptoApi(
    private val cryptoService: CryptoService
) {
    @PublicAccessUrl
    @Operation(summary = "初始化密钥交换", description = "获取 RSA 公钥用于加密 AES 密钥(24小时内有效)")
    @PostMapping("/key-exchange/init")
    fun initKeyExchange(
        @RequestBody @Validated request: KeyExchangeRequest
    ): JsonWrapper<KeyExchangeResponse> {
        val publicKey = cryptoService.initiateKeyExchange(request.sessionId)
        return JsonWrapper.ok(
            KeyExchangeResponse(publicKey = publicKey)
        )
    }

    @PublicAccessUrl
    @Operation(summary = "完成密钥交换", description = "上传用 RSA 公钥加密的 AES 密钥")
    @PostMapping("/key-exchange/complete")
    fun completeKeyExchange(
        @RequestBody @Validated request: KeyExchangeCompleteRequest
    ): JsonWrapper<Boolean> {
        val success = cryptoService.completeKeyExchange(
            request.sessionId,
            request.encryptedAesKey
        )
        return JsonWrapper.ok(success)
    }
}
