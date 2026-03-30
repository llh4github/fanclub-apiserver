/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.components.schedule

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.consts.StatisticsCacheKey
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerDanmuCountAddInput
import llh.fanclubvup.apiserver.service.sys.ScraperFeatureService
import llh.fanclubvup.apiserver.service.viewer.ViewerDanmuCountService
import llh.fanclubvup.common.BID
import org.springframework.data.redis.core.ScanOptions
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class DanmuCountSchedule(
    private val service: ViewerDanmuCountService,
    private val redisTemplate: StringRedisTemplate,
    private val scraperFeatureService: ScraperFeatureService,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 定时同步 Redis 中的弹幕计数到数据库
     * 每天凌晨 1:10 执行，统计前一天的弹幕发送量
     */
    @Scheduled(cron = "0 10 1 * * ?")
    fun syncDanmuCountToDatabase() {
        val list = scraperFeatureService.queryMonitorEnabled()
        if (list.isEmpty()) {
            logger.info { "没有开启数据监控的主播， 不统计弹幕发送量数据" }
            return
        }

        val targetDate = LocalDate.now().minusDays(1L)
        list.forEach {
            saveDanmuCnt(it.anchorInfo.biliId, targetDate)
        }
    }

    private fun saveDanmuCnt(rbid: BID, targetDate: LocalDate) {
        val key = StatisticsCacheKey.danmuCount(rbid, targetDate)

        val opt = ScanOptions.scanOptions().count(1000).build()
        val cursor = redisTemplate.opsForHash<String, String>().scan(key, opt)

        val batchSize = 500 // 每批处理 500 条
        var totalProcessed = 0
        var totalAffectedRows = 0

        cursor.use { c ->
            c.asSequence()
                .chunked(batchSize) // 分批处理
                .forEach { batch ->
                    val list = batch.mapNotNull { entry ->
                        try {
                            ViewerDanmuCountAddInput(entry.key.toLong(), rbid, entry.value.toInt(), targetDate)
                        } catch (e: NumberFormatException) {
                            logger.warn(e) { "无效的 UID: ${entry.key}" }
                            null
                        }
                    }

                    if (list.isNotEmpty()) {
                        val affectedRows = service.saveListNoTx(list)
                        totalAffectedRows += affectedRows
                        totalProcessed += list.size
                        logger.info { "批量保存弹幕发送量数据，批次大小=${list.size}, 影响行数=$affectedRows" }
                    }
                }
        }

        logger.info { "$rbid 主播保存弹幕发送量数据完成，总数=$totalProcessed, 总影响行数=$totalAffectedRows" }
        redisTemplate.delete(key)
    }

}
