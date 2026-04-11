/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.statistics

import llh.fanclubvup.apiserver.statistics.handlers.*
import llh.fanclubvup.bilibili.dm.DanmuCommandHandler
import org.springframework.stereotype.Component

@Component
class DanmuHandlerGather(
    danmuMsgCommandHandler: DanmuMsgCommandHandler,
    liveCommandHandler: LiveCommandHandler,
    preparingCommandHandler: PreparingCommandHandler,
    superChatCommandHandler: SuperChatCommandHandler,
    superChatMessageJpnCommandHandler: SuperChatMessageJpnCommandHandler,
    userToastMsgV2CommandHandler: UserToastMsgV2CommandHandler,
) {
    val handlers = listOf<DanmuCommandHandler<*>>(
        danmuMsgCommandHandler,
        liveCommandHandler,
        preparingCommandHandler,
        superChatCommandHandler,
        superChatMessageJpnCommandHandler,
        userToastMsgV2CommandHandler,
    )
}
