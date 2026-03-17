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

    @EventListener
    fun startup(event: ApplicationStartedEvent) {
        queryEnabled().forEach { info ->
            scraperClient.creatDanmuWebsocket(info.anchorInfo.roomId)?.let {
                map[info.anchorInfo.roomId] = it
            }
        }
    }

    @EventListener
    fun danmuWsFailedEvent(event: DanmuWsFailedEvent) {
        queryEnabled(event.roomId).forEach { info ->
            scraperClient.creatDanmuWebsocket(info.anchorInfo.roomId)?.let {
                map[info.anchorInfo.roomId] = it
            }
        }
    }

    private fun queryEnabled(roomId: Long? = null): List<ScraperMonitorFeatureEnabledView> {
        val querySpec = if (roomId != null)
            ScraperMonitorFeatureSpec(true, ScraperMonitorFeatureSpec.TargetOf_anchorInfo(roomId))
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
