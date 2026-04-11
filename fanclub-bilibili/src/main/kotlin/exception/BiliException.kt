/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.exception

/**
 * B站客户端模块的根异常类
 * 
 * 所有本模块抛出的业务异常都应继承此类
 */
open class BiliException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : RuntimeException(message, cause)
