/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.statistics

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.consts.StatisticsCacheKey
import llh.fanclubvup.apiserver.consts.enums.GuardLevel
import llh.fanclubvup.apiserver.consts.enums.LiveRecordStatus
import llh.fanclubvup.apiserver.dto.viwer.DanmuWsMsg
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveRecordAddInput
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveRecordEndLiveInput
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerGuardBuyRecordAddInput
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerScBvAddInput
import llh.fanclubvup.apiserver.service.anchor.AnchorLiveRecordService
import llh.fanclubvup.apiserver.service.viewer.ViewerGuardBuyRecordService
import llh.fanclubvup.apiserver.service.viewer.ViewerScBvRecordService
import llh.fanclubvup.apiserver.utils.ValidationUtil
import llh.fanclubvup.apiserver.websocket.DanmuWebsocketHandler
import llh.fanclubvup.bilisdk.dm.cmd.*
import llh.fanclubvup.bilisdk.scraper.BiliWsMsgBizHandler
import llh.fanclubvup.bilisdk.utils.BvUtil
import llh.fanclubvup.common.utils.LocalDateTimeUtil
import llh.fanclubvup.common.utils.StrUtil
import llh.fanclubvup.ksp.generated.ViewerScBvRecordServiceCacheHelper
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.Executors

@Service
class BiliDanmuStatistics(
    private val redisTemplate: StringRedisTemplate,
    private val anchorLiveRecordService: AnchorLiveRecordService,
    private val viewerGuardBuyRecordService: ViewerGuardBuyRecordService,
    private val danmuWebsocketHandler: DanmuWebsocketHandler,
    private val viewerScBvRecordService: ViewerScBvRecordService,
) : BiliWsMsgBizHandler {

    private val logger = KotlinLogging.logger {}

    @Autowired
    @Qualifier("statisticsDanmu")
    private lateinit var statisticsDanmu: DefaultRedisScript<Boolean>

    @Autowired
    @Qualifier("nicknameChange")
    private lateinit var nicknameChange: DefaultRedisScript<Boolean>

    private val executors = Executors.newVirtualThreadPerTaskExecutor()

    override fun handleMsg(cmd: Command, roomId: Long) {
        when (cmd) {
            is UserToastMsgV2Cmd -> handleUserToastMsgV2Cmd(cmd, roomId)
            is SuperChatMessageJpnCommand -> handleSuperChatMessageJpnCommand(cmd, roomId)
            is SuperChatCommand -> handleSuperChatCommand(cmd, roomId)
            is SendGiftCommand -> handleSendGiftCommand(cmd, roomId)
            is PreparingCommand -> handlePreparingCommand(cmd, roomId)
            is LiveCommand -> handleLiveCommand(cmd, roomId)
            is DanmuMsgCommand -> handleDanmuMsgCommand(cmd, roomId)
            else -> logger.warn { "未处理的命令类型: ${cmd::class.simpleName}, cmd=${cmd.cmd}" }
        }
    }

    private fun handleUserToastMsgV2Cmd(cmd: UserToastMsgV2Cmd, roomId: Long) {
        val senderUid = cmd.data?.senderUinfo?.uid
        val num = cmd.data?.payInfo?.num
        val guardLevel = cmd.data?.guardInfo?.guardLevel
        val price = cmd.data?.payInfo?.price
        val startTime = cmd.data?.guardInfo?.startTime
        val payflowId = cmd.data?.payInfo?.payflowId

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

    private fun handleSuperChatMessageJpnCommand(cmd: SuperChatMessageJpnCommand, roomId: Long) {
        executors.execute {
            BvUtil.extractBVFromString(cmd.data?.message)?.let {
                val data = cmd.data
                val id = data?.id
                val sendTime = data?.ts
                val bid = data?.uid
                if (ValidationUtil.isAllNotNull(id, sendTime, bid)) {
                    val input = ViewerScBvAddInput(
                        scId = id!!,
                        bv = it,
                        roomId = roomId,
                        bid = bid!!,
                        sendTime = LocalDateTimeUtil.toLocalDateTime(sendTime!!)
                    )
                    viewerScBvRecordService.save(input)
                    val key = ViewerScBvRecordServiceCacheHelper.BV_COUNT_CACHE_PREFIX + ":${roomId}:${it}"
                    redisTemplate.delete(key)
                } else {
                    logger.error { "醒目留言关键参数缺乏，无法保SC发送记录\n$cmd" }
                }
            }

        }
    }

    private fun handleSuperChatCommand(cmd: SuperChatCommand, roomId: Long) {
        executors.execute {
            BvUtil.extractBVFromString(cmd.data?.message)?.let {
                val data = cmd.data
                val id = data?.id
                val sendTime = data?.sendTime
                val bid = data?.uid
                if (ValidationUtil.isAllNotNull(id, sendTime, bid)) {
                    val input = ViewerScBvAddInput(
                        scId = id!!,
                        bv = it,
                        roomId = roomId,
                        bid = bid!!,
                        sendTime = LocalDateTimeUtil.toLocalDateTimeEpochMilli(sendTime!!)
                    )
                    val rs = viewerScBvRecordService.save(input)
                    val key = ViewerScBvRecordServiceCacheHelper.BV_COUNT_CACHE_PREFIX + ":${roomId}:${it}"
                    redisTemplate.delete(key)
                    logger.info { "保存SC发送记录结果：${rs?.id}" }
                } else {
                    logger.error { "醒目留言关键参数缺乏，无法保SC发送记录\n$cmd" }
                }
            }
        }
        logger.info {
            "醒目留言：" +
                    "用户=${cmd.data?.uinfo?.base?.name ?: cmd.data?.userInfo?.uname}, " +
                    "UID=${cmd.data?.uid ?: cmd.data?.uinfo?.uid}, " +
                    "金额=${cmd.data?.price} 元，" +
                    "倍数=${cmd.data?.rate}, " +
                    "内容=${cmd.data?.message}"
        }
    }

    private fun handleSendGiftCommand(cmd: SendGiftCommand, roomId: Long) {
        logger.info {
            "赠送礼物：" +
                    "用户名=${cmd.data?.uname}, " +
                    "UID=${cmd.data?.uid}, " +
                    "礼物名=${cmd.data?.giftName}, " +
                    "数量=${cmd.data?.num}, " +
                    "单价=${cmd.data?.price}, " +
                    "总价=${cmd.data?.totalCoin}, " +
                    "动作=${cmd.data?.action}"
        }
    }

    private fun handlePreparingCommand(cmd: PreparingCommand, roomId: Long) {
        val endTime = cmd.sendTime
        if (endTime == null) {
            logger.error { "直播准备中命令关键参数缺乏:\n$cmd" }
            return
        }

        val endLiveDateTime = LocalDateTimeUtil.toLocalDateTimeEpochMilli(endTime)
        val input = AnchorLiveRecordEndLiveInput(roomId, endLiveDateTime)
        val result = anchorLiveRecordService.updateEndLiveStatus(input)
        logger.info { "直播结束：直播间ID=$roomId, 结束时间=$endLiveDateTime, 更新数据库 $result 条数据" }
    }

    private fun handleLiveCommand(cmd: LiveCommand, roomId: Long) {
        val liveKey = cmd.liveKey
        val liveTime = cmd.liveTime
        logger.info { "开播数据:\n$cmd" }
        if (liveKey == null) {
            logger.error { "直播开始命令关键参数缺乏:\n$cmd" }
            return
        }

        val liveDateTime =
            if (liveTime != null) LocalDateTimeUtil.toLocalDateTime(liveTime)
            else LocalDateTime.now()
        val input = AnchorLiveRecordAddInput(roomId, liveKey, liveDateTime, LiveRecordStatus.LIVING)
        anchorLiveRecordService.save(input, SaveMode.UPSERT)
        logger.info { "保存开播记录" }
    }

    private fun handleDanmuMsgCommand(cmd: DanmuMsgCommand, roomId: Long) {
        val now = LocalDate.now()
        val time = LocalTime.now()
        val content = cmd.getContent() ?: return
        cmd.extractSendInfo()?.let { sender ->
            if (sender.suid == -1L) {
                // FIXME 为啥有-1的？
                logger.warn { "发送者无效, 忽略\n$cmd" }
                return
            }
            // 网页同步一些弹幕
            executors.execute {
                // 屏蔽低等级的弹幕
                if (sender.level <= 15) {
                    return@execute
                }
                val msg = DanmuWsMsg(
                    StrUtil.maskMiddle(sender.name),
                    content,
                    sender.ruid,
                    sender.level
                )
                danmuWebsocketHandler.sendDanmu(msg)
            }
            // 统计弹幕发送量
            executors.execute {
                val key = StatisticsCacheKey.danmuCount(sender.ruid, now)
                redisTemplate.execute(
                    statisticsDanmu,
                    listOf(key, "${time.hour}-${time.minute}"),
                    sender.suid.toString(),
                    sender.ts.toString(),
                )
            }
        }

        cmd.getUserInfo()?.let { userInfo ->
            executors.execute {
                val key = StatisticsCacheKey.nicknameChange()
                redisTemplate.execute(
                    nicknameChange,
                    listOf(key),
                    userInfo.username, userInfo.uid.toString()
                )
            }
        }
    }

}
