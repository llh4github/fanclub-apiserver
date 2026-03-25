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

        val list = cursor.use { c ->
            c.asSequence()
                .mapNotNull { entry ->
                    try {
                        ViewerNicknameUpdateRequest(entry.key.toLong(), entry.value)
                    } catch (e: NumberFormatException) {
                        logger.warn(e) { "无效的 UID: ${entry.key}" }
                        null
                    }
                }
                .toList()
        }

        if (list.isNotEmpty()) {
            val affectedRows = service.saveListNoTx(list)
            logger.info { "批量更新昵称完成，总数=${list.size}, 影响行数=$affectedRows" }
        } else {
            logger.info { "没有需要更新的昵称数据" }
        }
        redisTemplate.delete(key)
    }
}
