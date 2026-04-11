/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.statistics.handlers

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.consts.enums.GuardLevel
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerGuardBuyRecordAddInput
import llh.fanclubvup.apiserver.service.viewer.ViewerGuardBuyRecordService
import llh.fanclubvup.apiserver.utils.ValidationUtil
import llh.fanclubvup.bilibili.dm.DanmuCommandHandler
import llh.fanclubvup.bilibili.dm.cmd.UserToastMsgV2Command
import llh.fanclubvup.common.utils.LocalDateTimeUtil
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

/**
 * 用户开通大航海 V2 消息处理器
 * 用于处理用户开通、续费舰长/提督/总督的事件
 */
@Component
class UserToastMsgV2CommandHandler(
    private val viewerGuardBuyRecordService: ViewerGuardBuyRecordService,
) : DanmuCommandHandler<UserToastMsgV2Command>, BaseMsgCommandHandler() {
    private val logger = KotlinLogging.logger {}

    override fun handle(cmd: UserToastMsgV2Command, roomId: Long) {
        val senderUid = cmd.data?.senderUinfo?.uid
        val num = cmd.data?.payInfo?.num
        val guardLevel = cmd.data?.guardInfo?.guardLevel
        val price = cmd.data?.payInfo?.price
        val startTime = cmd.data?.guardInfo?.startTime
        val payflowId = cmd.data?.payInfo?.payflowId

        // 获取接收者信息（主播）
        if (ValidationUtil.isAllNotNull(senderUid, guardLevel, num, price, startTime, payflowId)) {
            viewerGuardBuyRecordService.save(
                ViewerGuardBuyRecordAddInput(
                    senderBid = senderUid!!,
                    roomId = roomId,
                    guardType = GuardLevel.parse(guardLevel!!),
                    num = num!!,
                    price = price!!.toInt(),
                    startTime = LocalDateTimeUtil.toLocalDateTime(startTime!!),
                    payflowId = payflowId!!
                )
            )
        } else {
            logger.error { "用户开通大航海 V2 消息关键参数缺乏:\n$cmd" }
        }

    }

    override fun supportedCommand(): KClass<UserToastMsgV2Command> = UserToastMsgV2Command::class
}
