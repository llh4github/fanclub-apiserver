/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.exception

/**
 * B站 API 业务错误异常
 * 
 * 当 B站 API 返回非零错误码时抛出此异常
 * 
 * @param code B站 API 返回的错误码
 * @param message B站 API 返回的错误信息
 */
class BiliApiException(
    val code: Int,
    message: String
) : BiliException("B站API错误 [$code]: $message")
