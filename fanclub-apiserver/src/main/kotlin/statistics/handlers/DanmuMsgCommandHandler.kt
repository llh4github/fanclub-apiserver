/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.statistics.handlers

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.consts.StatisticsCacheKey
import llh.fanclubvup.apiserver.dto.viwer.DanmuWsMsg
import llh.fanclubvup.apiserver.websocket.DanmuWebsocketHandler
import llh.fanclubvup.bilibili.dm.DanmuCommandHandler
import llh.fanclubvup.bilibili.dm.cmd.DanmuMsgCommand
import llh.fanclubvup.common.utils.StrUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalTime
import kotlin.reflect.KClass

@Component
class DanmuMsgCommandHandler(
    private val danmuWebsocketHandler: DanmuWebsocketHandler,
) : DanmuCommandHandler<DanmuMsgCommand>, BaseMsgCommandHandler() {
    private val logger = KotlinLogging.logger {}

    @Autowired
    @Qualifier("statisticsDanmu")
    private lateinit var statisticsDanmu: DefaultRedisScript<Boolean>

    @Autowired
    @Qualifier("nicknameChange")
    private lateinit var nicknameChange: DefaultRedisScript<Boolean>

    override fun handle(cmd: DanmuMsgCommand, roomId: Long) {
        val now = LocalDate.now()
        val time = LocalTime.now()
        val content = cmd.getContent() ?: return

        cmd.extractSendInfo()?.let { sender ->
            if (sender.suid == -1L) {
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

            // 昵称变更记录
            executors.execute {
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
    }

    override fun supportedCommand(): KClass<DanmuMsgCommand> = DanmuMsgCommand::class
}
