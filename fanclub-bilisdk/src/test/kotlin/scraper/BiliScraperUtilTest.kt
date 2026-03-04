package llh.fanclubvup.bilisdk.scraper

import llh.fanclubvup.bilisdk.dto.wbi.WbiImgDto
import llh.fanclubvup.bilisdk.utils.WbiUtil
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class BiliScraperUtilTest {

    @Test
    fun test_wbiSign() {
        val a = WbiUtil.wbiSign(
            WbiImgDto(
                imgUrl = "https://i0.hdslb.com/bfs/wbi/7cd084941338484aae1ad9425b84077c.png",
                subUrl = "https://i0.hdslb.com/bfs/wbi/4932caff0ff746eab6f01bf08b70ac45.png"
            )
        )
        assertEquals("ea1db124af3c7062474693fa704f4ff8", a)

    }
}