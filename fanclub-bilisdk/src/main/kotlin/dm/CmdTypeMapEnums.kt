/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dm

import llh.fanclubvup.bilisdk.dm.cmd.Command
import llh.fanclubvup.bilisdk.dm.cmd.ComboSendCommand
import llh.fanclubvup.bilisdk.dm.cmd.DanmuMsgCommand
import llh.fanclubvup.bilisdk.dm.cmd.EntryEffectCommand
import llh.fanclubvup.bilisdk.dm.cmd.GuardBuyCommand
import llh.fanclubvup.bilisdk.dm.cmd.OnlineRankCount
import llh.fanclubvup.bilisdk.dm.cmd.RoomRealTimeMessageUpdateCommand
import llh.fanclubvup.bilisdk.dm.cmd.SendGiftCommand
import llh.fanclubvup.bilisdk.dm.cmd.SuperChatCommand
import llh.fanclubvup.bilisdk.dm.cmd.UserToastMsgCommand
import llh.fanclubvup.bilisdk.dm.cmd.UserToastMsgV2Cmd
import kotlin.reflect.KClass

enum class CmdTypeMapEnums(val cmd: String, val clazz: KClass<out Command>) {
    ONLINE_RANK_COUNT("ONLINE_RANK_COUNT", OnlineRankCount::class),
    DANMU_MSG("DANMU_MSG", DanmuMsgCommand::class),
    ENTRY_EFFECT("ENTRY_EFFECT", EntryEffectCommand::class),
    USER_TOAST_MSG("USER_TOAST_MSG", UserToastMsgCommand::class),
    SEND_GIFT("SEND_GIFT", SendGiftCommand::class),
    COMBO_SEND("COMBO_SEND", ComboSendCommand::class),
    SUPER_CHAT_MESSAGE("SUPER_CHAT_MESSAGE", SuperChatCommand::class),
    GUARD_BUY("GUARD_BUY", GuardBuyCommand::class),
    USER_TOAST_MSG_V2("USER_TOAST_MSG_V2", UserToastMsgV2Cmd::class),
    ROOM_REAL_TIME_MESSAGE_UPDATE("ROOM_REAL_TIME_MESSAGE_UPDATE", RoomRealTimeMessageUpdateCommand::class),
    ;

    companion object {
        fun getValues(): List<CmdTypeMapEnums> {
            return entries
        }
    }
}
