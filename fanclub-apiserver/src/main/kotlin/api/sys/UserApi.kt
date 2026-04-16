package llh.fanclubvup.apiserver.api.sys

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import llh.fanclubvup.apiserver.dto.JsonWrapper
import llh.fanclubvup.apiserver.dto.sys.UpdatePasswordReq
import llh.fanclubvup.apiserver.service.common.CryptoService
import llh.fanclubvup.apiserver.service.sys.UserService
import llh.fanclubvup.apiserver.utils.SecurityContextUtil
import llh.fanclubvup.common.excptions.AppRuntimeException
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "用户接口")
@RestController
@RequestMapping("/sys/user")
class UserApi(
    private val service: UserService,
    private val cryptoService: CryptoService,
) {

    @PutMapping("/updatePassword")
    @Operation(summary = "更新密码")
    fun updatePassword(
        @RequestBody @Validated req: UpdatePasswordReq
    ): JsonWrapper<Boolean> {
        if (SecurityContextUtil.isAnchor()
            && req.id != SecurityContextUtil.currentUserId()
        ) {
            throw AppRuntimeException("无权修改其他人的密码")
        }
        val password = cryptoService.decryptWithSessionKey(req.password, req.cryptoSid)
            ?: return JsonWrapper.fail("9999", "密码不合法")
        val reqDecrypt = req.copy(password = password)
        return JsonWrapper.ok(
            service.updatePassword(reqDecrypt)
        )
    }
}
