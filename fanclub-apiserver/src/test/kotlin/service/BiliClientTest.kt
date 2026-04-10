/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service

import llh.fanclubvup.apiserver.service.sys.ScraperCookieService
import llh.fanclubvup.bilibili.BiliClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.TimeUnit
import kotlin.test.Test

@SpringBootTest
@ActiveProfiles("docker")
class BiliClientTest {
    @Autowired
    private lateinit var service: ScraperCookieService

    @Test
    fun testClientStart() {
        val roomId = System.getenv("BILI_ROOM_ID")?.toLong() ?: -1L
        if (roomId <= 0) throw Exception("房间ID错误")
        val cookies = service.fetchRandomCookies() ?: throw Exception("没有可用的Cookie")
        val client = BiliClient(roomId, cookies, emptyList())
        client.start()
        TimeUnit.SECONDS.sleep(6)
    }
}
