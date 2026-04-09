package llh.fanclubvup.bilibili.wbi

import llh.fanclubvup.bilibili.dto.WbiImgDto
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.util.*

private val WBI_KEY_INDEX_TABLE = intArrayOf(
    46,
    47,
    18,
    2,
    53,
    8,
    23,
    32,
    15,
    50,
    10,
    31,
    58,
    3,
    45,
    35,
    27,
    43,
    5,
    49,
    33,
    9,
    42,
    19,
    29,
    28,
    14,
    39,
    12,
    38,
    41,
    13
)

fun extractWbiKey(url: String): String {
    val filename = url.substringAfterLast('/')
    return filename.substringBeforeLast('.')
}

fun wbiSign(dto: WbiImgDto): String {
    val shuffled = extractWbiKey(dto.imgUrl) + extractWbiKey(dto.subUrl)
    return WBI_KEY_INDEX_TABLE.filter { it < shuffled.length }.mapNotNull { shuffled.getOrNull(it) }.joinToString("")
}

fun buildQueryString(params: TreeMap<String, String>, wbiSign: String): String {
    params["wts"] = Instant.now().epochSecond.toString()
    val uri = params.entries.joinToString("&") { (k, v) -> "$k=${URLEncoder.encode(v, StandardCharsets.UTF_8)}" }
    val md5 = md5("$uri$wbiSign")
    return "$uri&w_rid=$md5"
}

private fun md5(input: String): String {
    val digest = MessageDigest.getInstance("MD5")
    return digest.digest(input.toByteArray(StandardCharsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
