package llh.fanclubvup.bilisdk.utils

/**
 * BV 号提取
 */
object BvUtil {
    fun extractBVFromString(input: String): String? {
        val pattern = Regex("BV[a-zA-Z0-9]{10}")
        return pattern.find(input)?.value
    }
}
