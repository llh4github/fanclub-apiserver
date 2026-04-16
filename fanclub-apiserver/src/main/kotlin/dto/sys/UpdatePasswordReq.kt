package llh.fanclubvup.apiserver.dto.sys

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UpdatePasswordReq(

    @field:Schema(title = "用户ID", description = "用户ID", example = "123456")
    @field:Min(value = 0, message = "用户ID不能小于0")
    val id: Long,

    @field:Schema(title = "密码", description = "登录密码", example = "your_password")
    @field:NotBlank(message = "密码不能为空")
    @field:Size(min = 6, max = 100, message = "密码长度在 {min} 至 {max} 个字符之间")
    val password: String,

    @field:Schema(description = "会话ID，与初始化时保持一致", example = "user123session")
    @field:Size(min = 1, max = 30, message = "会话ID长度必须在1-30个字符之间")
    @field:Pattern(regexp = "^[a-zA-Z0-9]+$", message = "会话ID只能包含英文字母和数字")
    val cryptoSid: String,
)
