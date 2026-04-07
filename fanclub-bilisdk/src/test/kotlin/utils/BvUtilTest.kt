package llh.fanclubvup.bilisdk.utils

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

class BvUtilTest {

    @ParameterizedTest
    @CsvSource(
        "分享视频：BV1J4411p7qQ BV1J4412p7qQ很不错, BV1J4411p7qQ", // 多个只取第一个
        "https://b23.tv/BV1G4411d7fH, BV1G4411d7fH",
        "https://b23.tv/BV1G4411d7fH?p=1, BV1G4411d7fH",
        "视频链接：BV1xx411c7mD 很好玩, BV1xx411c7mD",
        "abc,",
        "无效的：BV123,"
    )
    fun test_extractBVFromString(input: String, want: String?) {
        val bv = BvUtil.extractBVFromString(input)
        assertEquals(want, bv)
    }
}
