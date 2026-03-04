/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.dto.sys

import io.swagger.v3.oas.annotations.media.Schema
import java.util.Date

@Schema(title = "登录响应", description = "登录成功后的响应数据")
data class LoginTokenResp(
    @field:Schema(
        title = "访问令牌",
        description = "用于访问受保护资源的 JWT 令牌",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    val accessToken: String,

    @field:Schema(
        title = "刷新令牌",
        description = "用于获取新的访问令牌",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    val refreshToken: String,

    @field:Schema(title = "过期时间", description = "访问令牌的有效期")
    val expiresIn: Date,

    @field:Schema(title = "用户 ID", description = "登录用户的唯一标识", example = "123456")
    val userId: Long,

    @field:Schema(title = "用户名", description = "登录用户的用户名", example = "Tom")
    val uname: String,
)
