/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.utils

object ValidationUtil {

    fun isAllNotEmpty(vararg args: Any?): Boolean {
        return args.all { it != null }
    }
}
