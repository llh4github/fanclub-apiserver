package llh.fanclubvup.apiserver.statistics

import io.github.oshai.kotlinlogging.KotlinLogging
import io.lettuce.core.RedisClient
import jakarta.annotation.Resource
import llh.fanclubvup.apiserver.consts.StatisticsCacheKey
import llh.fanclubvup.bilisdk.dm.cmd.Command
import llh.fanclubvup.bilisdk.dm.cmd.DanmuMsgCommand
import llh.fanclubvup.bilisdk.dm.cmd.SendGiftCommand
import llh.fanclubvup.bilisdk.dm.cmd.SuperChatCommand
import llh.fanclubvup.bilisdk.dm.cmd.UserToastMsgV2Cmd
import llh.fanclubvup.bilisdk.scraper.BiliWsMsgBizHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.Executors

@Service
class BiliDanmuStatistics(
    private val redisTemplate: StringRedisTemplate,
) : BiliWsMsgBizHandler {

    private val logger = KotlinLogging.logger {}

    @Autowired
    @Qualifier("statisticsDanmu")
    private lateinit var statisticsDanmu: DefaultRedisScript<Boolean>

    private val executors = Executors.newVirtualThreadPerTaskExecutor()

    override fun handle(cmd: UserToastMsgV2Cmd) {
        logger.info {
            "用户开通大航海 V2: " +
                    "用户名=${cmd.data?.senderUinfo?.base?.name}, " +
                    "UID=${cmd.data?.senderUinfo?.uid}, " +
                    "舰队等级=${cmd.data?.guardInfo?.guardLevel}, " +
                    "操作类型=${cmd.data?.guardInfo?.opType}, " +
                    "数量=${cmd.data?.payInfo?.num}, " +
                    "价格=${cmd.data?.payInfo?.price}, " +
                    "舰长信息=${cmd.data?.guardInfo}, " +
                    "提示消息=${cmd.data?.toastMsg}"
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
        }
    }

}
