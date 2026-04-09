/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.websocket

import llh.fanclubvup.common.BID

enum class WsOperation(val value: Int) {
    HANDSHAKE(0), HANDSHAKE_REPLY(1), HEARTBEAT(2), HEARTBEAT_REPLY(3),
    SEND_MSG(4), SEND_MSG_REPLY(5), DISCONNECT_REPLY(6), AUTH(7), AUTH_REPLY(8);

    companion object {
        fun fromValue(value: Int): WsOperation? = entries.find { it.value == value }
    }
}

enum class ProtoVer(val value: Short) {
    NORMAL(0), HEARTBEAT(1), DEFLATE(2), BROTLI(3);

    companion object {
        fun fromValue(value: Short): ProtoVer? = entries.find { it.value == value }
    }
}

data class PacketHeader(val packLen: Int, val headerSize: Short, val ver: Short, val operation: Int, val seqId: Int)
data class DanmuMessage(val cmd: String, val rawData: String)
data class DanmuAuth(
    val uid: BID,
    val roomid: Long,
    val protover: Int,
    val buvid: String,
    val supportAck: Boolean,
    val scene: String,
    val platform: String,
    val type: Int,
    val key: String
) {
    companion object {
        fun create(
            roomId: Long,
            token: String,
            uid: BID = 10071860L,
            buvid: String = "016EE42D-96D5-A092-5F01-D311D0BBAFF772750infoc"
        ) = DanmuAuth(
            uid = uid, roomid = roomId, protover = 3,
            buvid = buvid,
            supportAck = true, scene = "room", platform = "web", type = 2, key = token
        )
    }
}
