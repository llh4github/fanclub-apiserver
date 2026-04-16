/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 验证码数据
 */
@Schema(title = "验证码数据")
data class CaptchaData(
    @Schema(title = "验证码 key")
    val key: String,
    @Schema(title = "验证码图片(base64)")
    val images: String,
    @Schema(title = "验证码长度")
    val len: Int = 5,
)
