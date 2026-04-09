/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dm

import llh.fanclubvup.bilisdk.dm.cmd.*
import kotlin.reflect.KClass

enum class CmdTypeMapEnums(val cmd: String, val clazz: KClass<out Command>) {
    DANMU_MSG("DANMU_MSG", DanmuMsgCommand::class),
    SEND_GIFT("SEND_GIFT", SendGiftCommand::class),
    SUPER_CHAT_MESSAGE("SUPER_CHAT_MESSAGE", SuperChatCommand::class),
    SUPER_CHAT_MESSAGE_JPN("SUPER_CHAT_MESSAGE_JPN", SuperChatMessageJpnCommand::class),
    USER_TOAST_MSG_V2("USER_TOAST_MSG_V2", UserToastMsgV2Cmd::class),
    ROOM_REAL_TIME_MESSAGE_UPDATE("ROOM_REAL_TIME_MESSAGE_UPDATE", RoomRealTimeMessageUpdateCommand::class),
    PREPARING("PREPARING", PreparingCommand::class),
    LIVE("LIVE", LiveCommand::class),
    ;

    companion object {
        fun getValues(): List<CmdTypeMapEnums> {
            return entries
        }
    }
}
