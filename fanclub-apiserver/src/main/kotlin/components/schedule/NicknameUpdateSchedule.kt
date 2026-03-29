/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.components.schedule

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.consts.StatisticsCacheKey
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerNicknameUpdateRequest
import llh.fanclubvup.apiserver.service.viewer.ViewerBasicInfoService
import org.springframework.data.redis.core.ScanOptions
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class NicknameUpdateSchedule(
    private val redisTemplate: StringRedisTemplate,
    private val service: ViewerBasicInfoService,
) {

    private val logger = KotlinLogging.logger {}

    /**
     * 每天凌晨 3:30 执行，批量处理观众昵称更新任务
     */
    @Scheduled(cron = "0 30 3 * * ?")
    fun batchUpdateViewerNicknames() {
        val key = StatisticsCacheKey.nicknameChange()
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
                            ViewerNicknameUpdateRequest(entry.key.toLong(), entry.value)
                        } catch (e: NumberFormatException) {
                            logger.warn(e) { "无效的 UID: ${entry.key}" }
                            null
                        }
                    }

                    if (list.isNotEmpty()) {
                        val affectedRows = service.saveListNoTx(list)
                        totalAffectedRows += affectedRows
                        totalProcessed += list.size
                        logger.info { "批量更新昵称，批次大小=${list.size}, 影响行数=$affectedRows" }
                    }
                }
        }

        logger.info { "批量更新昵称完成，总数=$totalProcessed, 总影响行数=$totalAffectedRows" }
        redisTemplate.delete(key)
    }
}
