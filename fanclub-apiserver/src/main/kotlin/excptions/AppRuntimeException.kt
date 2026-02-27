/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.excptions

import kotlin.Throwable


/**
 * 本项目的根异常
 */
class AppRuntimeException(override val message: String? = null, override val cause: Throwable? = null) :
    Throwable(message, cause) {

}