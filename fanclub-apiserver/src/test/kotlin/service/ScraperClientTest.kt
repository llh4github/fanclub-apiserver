package llh.fanclubvup.apiserver.service

import jakarta.annotation.Resource
import llh.fanclubvup.bilisdk.scraper.BiliScraperClient
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.TimeUnit
import kotlin.test.Test

/**
 * 接口测试
 */
@DisabledOnOs(OS.LINUX) // 应该没人在linux系统下开发吧
@SpringBootTest
@ActiveProfiles("dev")
class ScraperClientTest {

    @Resource
    private lateinit var scraperClient: BiliScraperClient

    // LOL赛事房间
    private val roomId = 6L

    // LOL赛事官方账号
    private val uId = 50329118L

    @Test
    fun test_fetch_user_relation() {
        println(scraperClient.fetchUserRelation(uId))
    }

    @Test
    fun test() {
        println(scraperClient.fetchDanmuServerInfo(23771189))
    }

    @Test
    fun test_danmu() {
        scraperClient.creatDanmuWebsocket(23771189)
        TimeUnit.SECONDS.sleep(3)
    }

    @Test
    fun test_ss() {
        println(scraperClient.fetchRoomInfo(1713548468))
    }

    @Test
    fun test_a() {
        println(scraperClient.fetchGuardList(10071860, 1713548468))
    }
}
