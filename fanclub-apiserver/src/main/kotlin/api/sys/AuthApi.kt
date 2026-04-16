/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.api.sys

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import llh.fanclubvup.apiserver.dto.JsonWrapper
import llh.fanclubvup.apiserver.dto.sys.LoginReq
import llh.fanclubvup.apiserver.dto.sys.LoginTokenResp
import llh.fanclubvup.apiserver.service.common.CaptchaService
import llh.fanclubvup.apiserver.service.common.CryptoService
import llh.fanclubvup.apiserver.service.sys.UserService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "用户认证接口")
@RestController
@RequestMapping("/auth")
class AuthApi(
    private val userService: UserService,
    private val captchaService: CaptchaService,
    private val cryptoService: CryptoService,
) {
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    fun login(@RequestBody @Validated req: LoginReq): JsonWrapper<LoginTokenResp> {
        if (!captchaService.validCaptcha(req.captchaKey, req.captcha)) {
            return JsonWrapper.fail("9999", "验证码错误或已过期")
        }
        val password = cryptoService.decryptWithSessionKey(req.cryptoSid, req.password)
            ?: return JsonWrapper.fail("9999", "数据解密失败")
        val reqDecrypt = req.copy(password = password)

        return JsonWrapper.ok(userService.login(reqDecrypt))
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    fun logout(): JsonWrapper<Unit> =
        JsonWrapper.ok(userService.logout())
}
