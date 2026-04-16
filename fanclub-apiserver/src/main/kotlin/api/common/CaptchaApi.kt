/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.api.common

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import llh.fanclubvup.apiserver.dto.JsonWrapper
import llh.fanclubvup.apiserver.service.common.CaptchaService
import llh.fanclubvup.ksp.annon.PublicAccessUrl
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/captcha")
@Tag(name = "验证码")
class CaptchaApi(
    private val captchaService: CaptchaService
) {
    @PublicAccessUrl
    @Operation(summary = "生成验证码")
    @GetMapping("/generate")
    fun generateCaptcha() =
        JsonWrapper.ok(
            captchaService.generateCaptcha(len = 4)
        )
}
