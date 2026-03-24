/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.utils

import com.github.yitter.contract.IdGeneratorOptions
import com.github.yitter.idgen.YitIdHelper

object IdGenerator {

    private val CHARS by lazy {
        charArrayOf(
            '7', 'K', 'V', 'Z', '1', 'Q', '9', 'W', 'E', '4',
            'R', '0', 'T', 'Y', 'U', '2', 'I', 'O', '5', 'P',
            'A', 'S', 'D', 'F', 'G', 'H', 'J', '3', 'L', 'X',
            'C', '8', 'B', 'N', '6', 'M'
        ).apply {
            shuffle()
        }
    }

    init {
        YitIdHelper.setIdGenerator(IdGeneratorOptions(1))
    }

    fun nextId(): Long = YitIdHelper.nextId()

    fun nextIdStr(): String = nextId().toString()

    /**
     * 将 Long 型 ID 转换为 数字、字母混合的字符串
     * @return  `S0SYEVCPE`
     */
    fun nextShortId(): String {
        var num = nextId()
        if (num == 0L) return "0"
        val base = CHARS.size
        val buffer = CharArray(13)
        var index = 12

        while (num > 0) {
            buffer[index--] = CHARS[(num % base).toInt()]
            num /= base
        }

        return String(buffer, index + 1, 12 - index)
    }
}
