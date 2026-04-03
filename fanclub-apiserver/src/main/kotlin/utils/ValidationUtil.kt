/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.utils

object ValidationUtil {

    /**
     * 判断所有参数是否不为空
     */
    fun isAllNotNull(vararg args: Any?): Boolean {
        return args.all { it != null }
    }

    /**
     * 判断参数是否不为空
     *
     * 可检查容器、数组、字符串、Map
     * 自定义类型为true
     */
    fun isNotEmpty(value: Any?): Boolean {
        if (value == null) return false
        return when (value) {
            is String -> value.isNotEmpty()
            is Collection<*> -> value.isNotEmpty()
            is Map<*, *> -> value.isNotEmpty()
            is Array<*> -> value.isNotEmpty()
            else -> true
        }
    }
}
