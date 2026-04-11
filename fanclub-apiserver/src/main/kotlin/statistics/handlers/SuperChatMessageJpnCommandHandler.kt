/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.statistics.handlers

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerScBvAddInput
import llh.fanclubvup.apiserver.service.viewer.ViewerScBvRecordService
import llh.fanclubvup.bilibili.dm.DanmuCommandHandler
import llh.fanclubvup.bilibili.dm.cmd.SuperChatMessageJpnCommand
import llh.fanclubvup.bilibili.utils.BvUtil
import llh.fanclubvup.common.utils.LocalDateTimeUtil
import llh.fanclubvup.ksp.generated.ViewerScBvRecordServiceCacheHelper
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

/**
 * 日语醒目留言命令处理器
 * 用于处理直播间日语醒目留言（Super Chat JPN）事件
 */
@Component
class SuperChatMessageJpnCommandHandler(
    private val viewerScBvRecordService: ViewerScBvRecordService,
) : DanmuCommandHandler<SuperChatMessageJpnCommand>, BaseMsgCommandHandler() {
    private val logger = KotlinLogging.logger {}


    override fun handle(cmd: SuperChatMessageJpnCommand, roomId: Long) {
        // 异步处理SC数据
        executors.execute {
            BvUtil.extractBVFromString(cmd.data?.message)?.let { bv ->
                val data = cmd.data
                val id = data?.id
                val sendTime = data?.ts
                val bid = data?.uid

                if (id != null && sendTime != null && bid != null) {
                    val input = ViewerScBvAddInput(
                        scId = id,
                        bv = bv,
                        roomId = roomId,
                        bid = bid,
                        sendTime = LocalDateTimeUtil.toLocalDateTime(sendTime)
                    )
                    val rs = viewerScBvRecordService.save(input)

                    // 清除BV计数缓存
                    val key = ViewerScBvRecordServiceCacheHelper.BV_COUNT_CACHE_PREFIX + ":${roomId}:${bv}"
                    redisTemplate.delete(key)

                    logger.info { "保存SC发送记录结果：${rs?.id}" }
                } else {
                    logger.error { "日语醒目留言关键参数缺乏，无法保存SC发送记录\n$cmd" }
                }
            }
        }
    }

    override fun supportedCommand(): KClass<SuperChatMessageJpnCommand> = SuperChatMessageJpnCommand::class
}
