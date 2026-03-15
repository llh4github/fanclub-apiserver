/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.common

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

inline fun <reified T> Result<T>.getOrNull(logger: KLogger = KotlinLogging.logger {}): T? {
    if (this.exceptionOrNull() != null) {
        logger.error(this.exceptionOrNull()) { "获取结果失败" }
    }

    return this.getOrNull()
}
