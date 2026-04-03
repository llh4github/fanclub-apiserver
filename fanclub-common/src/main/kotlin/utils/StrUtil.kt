/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.common.utils

object StrUtil {

    fun maskMiddle(text: String, maskChar: Char = '*'): String {
        return if (text.length <= 2) {
            // 长度小于等于2，直接返回原字符串
            text
        } else {
            // 取首字符 + 中间星号 + 尾字符
            val first = text.first()
            val last = text.last()
            val middle = maskChar.toString().repeat(text.length - 2)
            "$first$middle$last"
        }
    }
}