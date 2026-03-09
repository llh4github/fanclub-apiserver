/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.utils

import llh.fanclubvup.bilisdk.enums.WsOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WsMsgUtil {
    fun makePacket(body: String, op: WsOperation): ByteArray {
        return makePacket(body.toByteArray(), op)
    }

    fun makePacket(body: ByteArray, op: WsOperation): ByteArray {
        val headerSize = 16 // HEADER_STRUCT.size
        val packLen = headerSize + body.size
        val ver = 1
        val seqId = 1

        val buffer = ByteBuffer.allocate(packLen).order(ByteOrder.BIG_ENDIAN)
        buffer.putInt(packLen)
        buffer.putShort(headerSize.toShort())
        buffer.putShort(ver.toShort())
        buffer.putInt(op.value)
        buffer.putInt(seqId)
        buffer.put(body)

        return buffer.array()
    }
}