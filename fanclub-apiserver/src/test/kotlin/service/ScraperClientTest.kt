/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service

import jakarta.annotation.Resource
import llh.fanclubvup.bilisdk.scraper.BiliScraperClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.TimeUnit
import kotlin.io.encoding.Base64
import kotlin.test.Test

/**
 * 接口测试
 */
@SpringBootTest
@ActiveProfiles("docker")
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
        val info = scraperClient.fetchDanmuServerInfo(roomId)?.data ?: throw Exception("未获取到弹幕服务器信息")
        println(info)
        val token = info.token ?: throw Exception("未获取到弹幕服务器信息")
        println("token\n$token")
        val packet = scraperClient.buildAuthWs(token, roomId).getOrNull() ?: throw Exception("未获取到弹幕服务器信息")
        val encode = Base64.encode(packet)
        println(encode)
    }

    @Test
    fun test_danmu() {
        scraperClient.creatDanmuWebsocket(roomId)
        TimeUnit.SECONDS.sleep(5)
    }

    @Test
    fun test_ss() {
        println(scraperClient.fetchRoomInfo(roomId))
    }

    @Test
    fun test_a() {
        println(scraperClient.fetchGuardList(uId, roomId))
    }
}
