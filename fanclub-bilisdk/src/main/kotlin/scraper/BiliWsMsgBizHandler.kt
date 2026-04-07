/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.scraper

import llh.fanclubvup.bilisdk.dm.cmd.*

interface BiliWsMsgBizHandler {

    fun handleMsg(cmd: Command) {
        when (cmd) {
            is UserToastMsgV2Cmd -> handle(cmd)
            is SuperChatCommand -> handle(cmd)
            is SendGiftCommand -> handle(cmd)
            is DanmuMsgCommand -> handle(cmd)
            is LiveCommand -> handle(cmd)
            is PreparingCommand -> handle(cmd)
            is SuperChatMessageJpnCommand -> handle(cmd)
            else -> {}
        }
    }

    fun handle(cmd: UserToastMsgV2Cmd) {}

    fun handle(cmd: SuperChatCommand) {}
    fun handle(cmd: SuperChatMessageJpnCommand) {}

    fun handle(cmd: SendGiftCommand) {}

    fun handle(cmd: DanmuMsgCommand) {}

    fun handle(cmd: LiveCommand) {}

    fun handle(cmd: PreparingCommand) {}
}
