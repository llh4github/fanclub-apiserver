package llh.fanclubvup.apiserver.components.schedule

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorFollowerNumInput
import llh.fanclubvup.apiserver.service.anchor.AnchorFollowerNumService
import llh.fanclubvup.bilisdk.scraper.BiliScraperClient
import llh.fanclubvup.common.excptions.AppRuntimeException
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate


@Component
class AnchorFollowerNumSchedule(
    private val scraperClient: BiliScraperClient,
    private val service: AnchorFollowerNumService,
) {

    private val logger = KotlinLogging.logger {}

    /**
     * 每 4 小时执行一次，更新主播粉丝数
     */
    @Scheduled(fixedRate = 4 * 60 * 60 * 1000)
    fun updateAnchorFollowerNum() {
        logger.info { "开始执行定时任务：更新主播粉丝数" }
        try {
            // TODO: 实现具体的业务逻辑
        } catch (e: Throwable) {
            logger.error(e) { "执行定时任务失败：更新主播粉丝数" }
        }
    }

    private fun findAndSaveFollowerNum(uId: Long) {
        val relation =
            scraperClient.fetchUserRelation(uId)?.data ?: throw AppRuntimeException("查找 $uId 用户粉丝数量出错")
        val input = AnchorFollowerNumInput(uId, relation.follower, LocalDate.now())
        service.save(input, saveMode = SaveMode.UPSERT)
    }
}
