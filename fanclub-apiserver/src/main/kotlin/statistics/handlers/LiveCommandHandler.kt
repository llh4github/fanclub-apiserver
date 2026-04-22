/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.statistics.handlers

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.consts.enums.LiveRecordStatus
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveRecordAddInput
import llh.fanclubvup.apiserver.service.anchor.AnchorLiveRecordService
import llh.fanclubvup.bilibili.dm.DanmuCommandHandler
import llh.fanclubvup.bilibili.dm.cmd.LiveCommand
import llh.fanclubvup.common.consts.CacheKeyPrefix
import llh.fanclubvup.common.utils.LocalDateTimeUtil
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import kotlin.reflect.KClass

/**
 * 直播开始命令处理器
 * 用于处理直播间开播事件
 */
@Component
class LiveCommandHandler(
    private val anchorLiveRecordService: AnchorLiveRecordService,
) : DanmuCommandHandler<LiveCommand>, BaseMsgCommandHandler() {
    private val logger = KotlinLogging.logger {}

    override fun handle(cmd: LiveCommand, roomId: Long) {
        logger.info { "直播间 $roomId 开始直播, liveKey: ${cmd.liveKey}, liveTime: ${cmd.liveTime}" }
        val liveKey = cmd.liveKey
        val liveTime = cmd.liveTime
        if (liveKey == null) {
            logger.error { "直播开始命令关键参数缺乏:\n$cmd" }
            return
        }
//        executors.execute {
//            fanclubSupportHttp.startLive(StartLiveReq(roomId, liveKey))
//        }

        val liveDateTime =
            if (liveTime != null) LocalDateTimeUtil.toLocalDateTime(liveTime)
            else LocalDateTime.now()
        val input = AnchorLiveRecordAddInput(roomId, liveKey, liveDateTime, LiveRecordStatus.LIVING)
        anchorLiveRecordService.save(input, SaveMode.UPSERT)
        redisTemplate.delete(CacheKeyPrefix.SERVICE_CACHE_KEY + "AnchorLiveRecordService:fetchLiveStatus:$roomId")
        logger.info { "保存开播记录" }
    }

    override fun supportedCommand(): KClass<LiveCommand> = LiveCommand::class
}
