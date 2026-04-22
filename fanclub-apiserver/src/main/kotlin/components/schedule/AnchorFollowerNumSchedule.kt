/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.components.schedule

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorFollowerNumInput
import llh.fanclubvup.apiserver.service.anchor.AnchorFollowerNumService
import llh.fanclubvup.apiserver.service.sys.ScraperCookieService
import llh.fanclubvup.apiserver.service.sys.ScraperFeatureService
import llh.fanclubvup.bilibili.http.BiliHttpClient
import llh.fanclubvup.common.BID
import llh.fanclubvup.common.consts.CacheKeyPrefix
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDate


@Component
class AnchorFollowerNumSchedule(
    private val scraperFeatureService: ScraperFeatureService,
    private val service: AnchorFollowerNumService,
    private val redisTemplate: StringRedisTemplate,
    private val scraperCookieService: ScraperCookieService,
) {

    private val logger = KotlinLogging.logger {}
    private val expire = Duration.ofHours(4)
    private val biliHttpClient by lazy {
        BiliHttpClient(scraperCookieService.fetchRandomCookies())
    }

    /**
     * 启动后等待 5 分钟开始执行，之后每 4 小时执行一次，更新主播粉丝数
     */
    @Scheduled(initialDelay = 5 * 60 * 1000, fixedRate = 4 * 60 * 60 * 1000)
    fun updateAnchorFollowerNum() {
        val now = LocalDate.now()
        val list = scraperFeatureService.queryFollowerEnabled()
        if (list.isEmpty()) {
            logger.warn { "需要更新主播粉丝数的主播列表为空" }
            return
        }
        logger.info { "开始执行更新主播粉丝数定时任务: ${list.size} 位主播" }
        try {
            list.forEach {
                findAndSaveFollowerNum(it.anchorInfo.biliId, now)
            }
        } catch (e: Throwable) {
            logger.error(e) { "执行更新主播粉丝数定时任务失败" }
        }
    }

    private fun findAndSaveFollowerNum(uId: BID, now: LocalDate) {
        biliHttpClient.fetchUserRelation(uId)
            .fold(
                onSuccess = { response ->
                    if (response == null || response.data == null) {
                        logger.warn { "获取主播 $uId 粉丝数失败或数据为空" }
                        return@fold
                    }
                    val data = response.data!!
                    val input = AnchorFollowerNumInput(uId, data.follower, now)
                    service.upsert(input, AnchorFollowerNumService.defaultUniqueKeys)
                    // 更新缓存
                    // see AnchorFollowerNumServiceImpl.queryNum
                    val key = CacheKeyPrefix.SERVICE_CACHE_KEY + "AnchorFollowerNumService:queryNum:" +
                            input.bid + ":" + now
                    redisTemplate.opsForValue().set(key, input.followerNum.toString(), expire)
                    logger.info { "已更新主播 $uId 粉丝数：${data.follower}" }
                },
                onFailure = {
                    logger.error(it) { "获取主播 $uId 粉丝数失败" }
                }
            )
    }
}
