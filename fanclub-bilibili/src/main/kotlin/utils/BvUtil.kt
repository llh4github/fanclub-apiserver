/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.utils

/**
 * BV 号提取
 */
object BvUtil {

    val pattern = Regex("BV[a-zA-Z0-9]{10}")

    fun extractBVFromString(input: String?): String? {
        if (input.isNullOrEmpty()) return null
        return pattern.find(input)?.value
    }
}
