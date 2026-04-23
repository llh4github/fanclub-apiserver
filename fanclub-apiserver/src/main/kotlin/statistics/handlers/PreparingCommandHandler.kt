/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.statistics.handlers

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveRecordEndLiveInput
import llh.fanclubvup.apiserver.service.anchor.AnchorLiveRecordService
import llh.fanclubvup.bilibili.dm.DanmuCommandHandler
import llh.fanclubvup.bilibili.dm.cmd.PreparingCommand
import llh.fanclubvup.common.utils.LocalDateTimeUtil
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

/**
 * 直播准备命令处理器
 * 用于处理直播间下播事件（进入准备状态）
 */
@Component
class PreparingCommandHandler(
    private val anchorLiveRecordService: AnchorLiveRecordService,
) : DanmuCommandHandler<PreparingCommand>, BaseMsgCommandHandler() {
    private val logger = KotlinLogging.logger {}

    override fun handle(cmd: PreparingCommand, roomId: Long) {
        logger.info { "直播间 $roomId 进入准备状态（下播）, sendTime: ${cmd.sendTime}" }

        val endTime = cmd.sendTime
        if (endTime == null) {
            logger.error { "直播准备中命令关键参数缺乏:\n$cmd" }
            return
        }
//        executors.execute {
//            fanclubSupportHttp.stopLive(
//                StopLiveReq(roomId)
//            )
//        }

        val endLiveDateTime = LocalDateTimeUtil.toLocalDateTimeEpochMilli(endTime)
        val input = AnchorLiveRecordEndLiveInput(roomId, endLiveDateTime)
        val result = anchorLiveRecordService.updateEndLiveStatus(input)
        logger.info { "更新直播结束状态：直播间ID=$roomId, 结束时间=$endLiveDateTime, 更新数据库 $result 条数据" }
    }

    override fun supportedCommand(): KClass<PreparingCommand> = PreparingCommand::class
}
