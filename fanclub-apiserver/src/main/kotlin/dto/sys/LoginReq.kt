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
    val password: String
)
