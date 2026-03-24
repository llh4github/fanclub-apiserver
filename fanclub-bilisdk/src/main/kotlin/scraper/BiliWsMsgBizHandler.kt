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
            else -> {}
        }
    }

    fun handle(cmd: UserToastMsgV2Cmd) {}

    fun handle(cmd: SuperChatCommand) {}

    fun handle(cmd: SendGiftCommand) {}

    fun handle(cmd: DanmuMsgCommand) {}
}
