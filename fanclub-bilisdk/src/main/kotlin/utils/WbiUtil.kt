/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.utils

import llh.fanclubvup.bilisdk.dto.wbi.WbiImgDto

object WbiUtil {
    private val WBI_KEY_INDEX_TABLE = listOf(
        46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35,
        27, 43, 5, 49, 33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13
    )

    fun wbiSign(dto: WbiImgDto): String {
        val shuffled = extractWbiImgKey(dto.imgUrl) + extractWbiImgKey(dto.subUrl)
        return WBI_KEY_INDEX_TABLE
            .filter { it < shuffled.length }
            .map { shuffled[it] }
            .joinToString("")
    }

    /**
     * 提取img_key（用于WBI签名计算）
     *
     * 特化方法，不通用。
     */
    private fun extractWbiImgKey(url: String): String {
        val fileName = url.substringAfterLast("/").substringBefore(".png")
        return fileName  // 例如: 7cd084941338484aae1ad9425b84077c
    }
}