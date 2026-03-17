package llh.fanclubvup.apiserver.service.danmu

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.dm.cmd.UserToastMsgV2Cmd
import llh.fanclubvup.bilisdk.scraper.BiliWsMsgBizHandler
import org.springframework.stereotype.Service

@Service
class BiliWsMsgBizHandlerImpl : BiliWsMsgBizHandler {

    private val logger = KotlinLogging.logger {}

    override fun handle(cmd: UserToastMsgV2Cmd) {
        logger.info { "" }
    }
}
