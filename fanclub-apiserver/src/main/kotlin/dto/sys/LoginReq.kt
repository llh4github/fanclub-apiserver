/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.dto.sys

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(title = "登录请求")
data class LoginReq(
    @field:Schema(title = "用户名", description = "登录用户名", example = "Tom")
    @field:NotBlank(message = "用户名不能为空")
    @field:Size(min = 3, max = 30, message = "用户名长度在 {min} 至 {max} 个字符之间")
    val username: String,

    @field:Schema(title = "密码", description = "登录密码", example = "your_password")
    @field:NotBlank(message = "密码不能为空")
    @field:Size(min = 6, max = 50, message = "密码长度在 {min} 至 {max} 个字符之间")
    val password: String,

    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 6, message = "验证码长度在 {min} 至 {max} 个字符之间")
    @field:Schema(title = "验证码", description = "登录验证码", example = "1234")
    val captcha: String,

    @NotBlank(message = "验证码 key 不能为空")
    @Size(min = 1, max = 50, message = "验证码 key 长度在 {min} 至 {max} 个字符之间")
    @field:Schema(title = "验证码 key", description = "登录验证码 key", example = "1234")
    val captchaKey: String
)
