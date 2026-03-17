package llh.fanclubvup.apiserver.components.schedule

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.entity.sys.dto.ScraperMonitorFeatureEnabledView
import llh.fanclubvup.apiserver.entity.sys.dto.ScraperMonitorFeatureSpec
import llh.fanclubvup.apiserver.service.sys.ScraperFeatureService
import llh.fanclubvup.bilisdk.event.DanmuWsFailedEvent
import llh.fanclubvup.bilisdk.scraper.BiliDanmuWebSocketHandler
import llh.fanclubvup.bilisdk.scraper.BiliScraperClient
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * 直播数据监控计划
 */
@Component
class LiveDataMonitorSchedule(
    private val scraperFeatureService: ScraperFeatureService,
    private val scraperClient: BiliScraperClient,
) {
    private val logger = KotlinLogging.logger {}
    private val map = ConcurrentHashMap<Long, BiliDanmuWebSocketHandler>()
    private val countMap = ConcurrentHashMap<Long, Int>()
    private val maxRetry = 5

    @EventListener
    fun startup(event: ApplicationStartedEvent) {
        queryEnabled().forEach { info ->
            scraperClient.creatDanmuWebsocket(info.anchorInfo.roomId)?.let {
                map[info.anchorInfo.roomId] = it
            }
        }
        logger.info { "主播弹幕数据监控计划启动成功" }
    }

    @EventListener
    fun danmuWsFailedEvent(event: DanmuWsFailedEvent) {
        retryWsConnection(listOf(event.roomId))
    }

    @Scheduled(cron = "0 0 4 * * ?")
    fun retryWsConnectionEveryDay() {
        countMap.clear()
        logger.info { "清除计数表" }
        val invalidRoomIds = map.filter { !it.value.isValid() }.keys
        retryWsConnection(invalidRoomIds.toList())
    }


    private fun retryWsConnection(roomIds: List<Long> = emptyList()) {
        queryEnabled(roomIds).forEach { info ->
            val cnt = countMap.getOrPut(info.anchorInfo.roomId) { 0 }
            if (cnt >= maxRetry) {
                logger.error { "${info.anchorInfo.roomId} 达最大重试次数，取消重试" }
            } else {
                scraperClient.creatDanmuWebsocket(info.anchorInfo.roomId)?.let {
                    map[info.anchorInfo.roomId] = it
                    countMap[info.anchorInfo.roomId] = cnt + 1
                }
            }
        }
    }

    private fun queryEnabled(roomIds: List<Long> = emptyList()): List<ScraperMonitorFeatureEnabledView> {
        val querySpec = if (roomIds.isEmpty())
            ScraperMonitorFeatureSpec(
                true,
                ScraperMonitorFeatureSpec.TargetOf_anchorInfo(roomIds)
            )
        else ScraperMonitorFeatureSpec(true)

        val list = scraperFeatureService.listQuery(
            staticType = ScraperMonitorFeatureEnabledView::class,
            querySpec = querySpec,
        )
        if (list.isEmpty()) {
            logger.warn { "没有开启直播数据监控的主播" }
        }
        return list
    }
}
