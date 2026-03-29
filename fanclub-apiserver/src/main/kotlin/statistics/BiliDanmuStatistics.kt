/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.statistics

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.consts.StatisticsCacheKey
import llh.fanclubvup.apiserver.consts.enums.GuardLevel
import llh.fanclubvup.apiserver.dto.viwer.DanmuWsMsg
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveRecordAddInput
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveRecordEndLiveInput
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerGuardBuyRecordAddInput
import llh.fanclubvup.apiserver.service.anchor.AnchorLiveRecordService
import llh.fanclubvup.apiserver.service.viewer.ViewerGuardBuyRecordService
import llh.fanclubvup.apiserver.utils.ValidationUtil
import llh.fanclubvup.apiserver.websocket.DanmuWebsocketHandler
import llh.fanclubvup.bilisdk.dm.cmd.DanmuMsgCommand
import llh.fanclubvup.bilisdk.dm.cmd.LiveCommand
import llh.fanclubvup.bilisdk.dm.cmd.PreparingCommand
import llh.fanclubvup.bilisdk.dm.cmd.SendGiftCommand
import llh.fanclubvup.bilisdk.dm.cmd.SuperChatCommand
import llh.fanclubvup.bilisdk.dm.cmd.UserToastMsgV2Cmd
import llh.fanclubvup.bilisdk.scraper.BiliWsMsgBizHandler
import llh.fanclubvup.common.utils.LocalDateTimeUtil
import llh.fanclubvup.common.utils.StrUtil
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.Executors

@Service
class BiliDanmuStatistics(
    private val redisTemplate: StringRedisTemplate,
    private val anchorLiveRecordService: AnchorLiveRecordService,
    private val viewerGuardBuyRecordService: ViewerGuardBuyRecordService,
    private val danmuWebsocketHandler: DanmuWebsocketHandler
) : BiliWsMsgBizHandler {

    private val logger = KotlinLogging.logger {}

    @Autowired
    @Qualifier("statisticsDanmu")
    private lateinit var statisticsDanmu: DefaultRedisScript<Boolean>

    @Autowired
    @Qualifier("nicknameChange")
    private lateinit var nicknameChange: DefaultRedisScript<Boolean>

    private val executors = Executors.newVirtualThreadPerTaskExecutor()

    override fun handle(cmd: UserToastMsgV2Cmd) {
        val senderUid = cmd.data?.senderUinfo?.uid
        val reciverUid = cmd.data?.receiverInfo?.uid
        val num = cmd.data?.payInfo?.num
        val guardLevel = cmd.data?.guardInfo?.guardLevel
        val price = cmd.data?.payInfo?.price
        val startTime = cmd.data?.guardInfo?.startTime
        val payflowId = cmd.data?.payInfo?.payflowId

        if (ValidationUtil.isAllNotEmpty(senderUid, guardLevel, reciverUid, num, price, startTime, payflowId)) {
            viewerGuardBuyRecordService.save(
                ViewerGuardBuyRecordAddInput(
                    senderBid = senderUid!!,
                    receiverBid = reciverUid!!,
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

    override fun handle(cmd: SuperChatCommand) {
        logger.info {
            "醒目留言：" +
                    "用户=${cmd.data?.uinfo?.base?.name ?: cmd.data?.userInfo?.uname}, " +
                    "UID=${cmd.data?.uid ?: cmd.data?.uinfo?.uid}, " +
                    "金额=${cmd.data?.price} 元，" +
                    "倍数=${cmd.data?.rate}, " +
                    "内容=${cmd.data?.messageTrans ?: cmd.data?.message}"
        }
    }

    override fun handle(cmd: SendGiftCommand) {
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

    override fun handle(cmd: PreparingCommand) {
        val roomId = cmd.roomId
        val endTime = cmd.sendTime
        if (roomId == null || endTime == null) {
            logger.error { "直播准备中命令关键参数缺乏:\n$cmd" }
            return
        }

        val endLiveDateTime = Instant.ofEpochMilli(endTime)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val input = AnchorLiveRecordEndLiveInput(roomId, endLiveDateTime)
        val result = anchorLiveRecordService.updateEndLiveStatus(input)
        logger.info { "直播结束：直播间ID=$roomId, 结束时间=$endLiveDateTime, 更新数据库 $result 条数据" }
    }

    override fun handle(cmd: LiveCommand) {
        val liveKey = cmd.liveKey
        val roomId = cmd.roomId
        val liveTime = cmd.liveTime
        logger.info { "开播数据:\n$cmd" }
        if (liveKey == null || roomId == null) {
            logger.error { "直播开始命令关键参数缺乏:\n$cmd" }
            return
        }

        val liveDateTime =
            if (liveTime != null) LocalDateTimeUtil.toLocalDateTime(liveTime)
            else LocalDateTime.now()
        val input = AnchorLiveRecordAddInput(roomId, liveKey, liveDateTime)
        anchorLiveRecordService.save(input, SaveMode.UPSERT)
        logger.info { "保存开播记录" }
    }

    override fun handle(cmd: DanmuMsgCommand) {
        val now = LocalDate.now()
        val time = LocalTime.now()
        cmd.getUserInfo()?.let { userInfo ->
            logger.info {
                "弹幕消息：" +
                        "用户名=${userInfo.username}, " +
                        "UID=${userInfo.uid}, " +
                        "ts=${userInfo.timestamp}, " +
                        "内容=${cmd.getContent()}"
            }

            executors.execute {
                val key = StatisticsCacheKey.danmuCount(now)
                redisTemplate.execute(
                    statisticsDanmu,
                    listOf(key, "${time.hour}-${time.minute}"),
                    userInfo.uid.toString(), userInfo.timestamp.toString()
                )
            }
            executors.execute {
                val key = StatisticsCacheKey.nicknameChange()
                redisTemplate.execute(
                    nicknameChange,
                    listOf(key),
                    userInfo.username, userInfo.uid.toString()
                )
            }

            executors.execute {
                val senderInfo = cmd.extractSendInfo()
                val content = cmd.getContent()
                if (senderInfo == null) return@execute
                if (content == null) return@execute

                if (senderInfo.level <= 18) {
                    return@execute
                }
                val msg = DanmuWsMsg(
                    StrUtil.maskMiddle(senderInfo.name),
                    content,
                    senderInfo.ruid
                )
                danmuWebsocketHandler.sendDanmu(msg)
            }
        }
    }

}
