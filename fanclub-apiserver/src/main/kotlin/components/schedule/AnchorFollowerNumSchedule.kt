package llh.fanclubvup.apiserver.components.schedule

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorFollowerNumInput
import llh.fanclubvup.apiserver.service.anchor.AnchorFollowerNumService
import llh.fanclubvup.apiserver.service.sys.ScraperFeatureService
import llh.fanclubvup.bilisdk.scraper.BiliScraperClient
import llh.fanclubvup.common.BID
import llh.fanclubvup.common.excptions.AppRuntimeException
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate


@Component
class AnchorFollowerNumSchedule(
    private val scraperFeatureService: ScraperFeatureService,
    private val scraperClient: BiliScraperClient,
    private val service: AnchorFollowerNumService,
) {

    private val logger = KotlinLogging.logger {}

    /**
     * 启动后等待 5 分钟开始执行，之后每 4 小时执行一次，更新主播粉丝数
     */
    @Scheduled(initialDelay = 5 * 60 * 1000, fixedRate = 4 * 60 * 60 * 1000)
    fun updateAnchorFollowerNum() {
        val now = LocalDate.now()
        logger.info { "开始执行定时任务：更新主播粉丝数" }
        try {
            scraperFeatureService.queryFollowerEnabled().forEach {
                findAndSaveFollowerNum(it.anchorInfo.biliId, now)
            }
        } catch (e: Throwable) {
            logger.error(e) { "执行更新主播粉丝数定时任务失败" }
        }
    }

    private fun findAndSaveFollowerNum(uId: BID, now: LocalDate) {
        val result = scraperClient.fetchUserRelation(uId)
        if (result == null || result.data == null) {
            logger.warn { "获取主播 $uId 粉丝数失败或数据为空" }
            return
        }
        val data = result.data!!
        val input = AnchorFollowerNumInput(uId, data.follower, now)
        service.save(input, saveMode = SaveMode.UPSERT)
        logger.info { "已更新主播 $uId 粉丝数：${data.follower}" }
    }
}
