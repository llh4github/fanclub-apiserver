/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.statistics

import llh.fanclubvup.apiserver.statistics.handlers.DanmuMsgCommandHandler
import llh.fanclubvup.bilibili.dm.DanmuCommandHandler
import org.springframework.stereotype.Component

@Component
class DanmuHandlerGather(
    danmuMsgCommandHandler: DanmuMsgCommandHandler,
) {
    val handlers = listOf<DanmuCommandHandler<*>>(
        danmuMsgCommandHandler,
    )
}
