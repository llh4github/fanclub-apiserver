/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.scraper

import llh.fanclubvup.bilisdk.dm.cmd.*

interface BiliWsMsgBizHandler {
    fun handleMsg(cmd: Command, roomId: Long) {
        when (cmd) {
            is UserToastMsgV2Cmd -> handle(cmd, roomId)
            is SuperChatCommand -> handle(cmd, roomId)
            is SendGiftCommand -> handle(cmd, roomId)
            is DanmuMsgCommand -> handle(cmd, roomId)
            is LiveCommand -> handle(cmd, roomId)
            is PreparingCommand -> handle(cmd, roomId)
            is SuperChatMessageJpnCommand -> handle(cmd, roomId)
            else -> {}
        }
    }

    fun handle(cmd: UserToastMsgV2Cmd, roomId: Long) {}

    fun handle(cmd: SuperChatCommand, roomId: Long) {}
    fun handle(cmd: SuperChatMessageJpnCommand, roomId: Long) {}

    fun handle(cmd: SendGiftCommand, roomId: Long) {}

    fun handle(cmd: DanmuMsgCommand, roomId: Long) {}

    fun handle(cmd: LiveCommand, roomId: Long) {}

    fun handle(cmd: PreparingCommand, roomId: Long) {}
}
