package llh.fanclubvup.bilibili.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import okio.Buffer
import okio.ByteString
import java.io.ByteArrayOutputStream
import java.util.zip.Inflater

private const val HEADER_SIZE = 16

fun makePacket(data: ByteArray, operation: WsOperation): ByteString {
    val buffer = Buffer()
    buffer.writeInt(HEADER_SIZE + data.size)
    buffer.writeShort(HEADER_SIZE)
    buffer.writeShort(1)
    buffer.writeInt(operation.value)
    buffer.writeInt(1)
    buffer.write(data)
    return buffer.readByteString()
}

fun parseHeader(data: ByteArray): PacketHeader? {
    if (data.size < HEADER_SIZE) return null
    val buffer = Buffer().write(data)
    return PacketHeader(buffer.readInt(), buffer.readShort(), buffer.readShort(), buffer.readInt(), buffer.readInt())
}

private fun decompressDeflate(data: ByteArray): Result<ByteArray> {
    return try {
        val inflater = Inflater()
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        try {
            inflater.setInput(data)
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                if (count > 0) outputStream.write(buffer, 0, count)
            }
            Result.success(outputStream.toByteArray())
        } finally {
            inflater.end()
            outputStream.close()
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

fun parsePacket(packet: ByteString, roomId: Long): List<DanmuMessage> {
    val logger = KotlinLogging.logger {}
    val messages = mutableListOf<DanmuMessage>()
    val packetBytes = packet.toByteArray()
    var offset = 0

    while (offset < packetBytes.size) {
        if (offset + HEADER_SIZE > packetBytes.size) break
        val header = parseHeader(packetBytes.sliceArray(offset until offset + HEADER_SIZE)) ?: break
        if (header.packLen > packetBytes.size - offset) break

        val bodyStart = offset + header.headerSize.toInt()
        val bodyEnd = offset + header.packLen
        val body = packetBytes.sliceArray(bodyStart until bodyEnd)

        when (WsOperation.fromValue(header.operation)) {
            WsOperation.HEARTBEAT_REPLY -> {
                if (body.size >= 4) {
                    val popularity =
                        ((body[0].toInt() and 0xFF) shl 24) or ((body[1].toInt() and 0xFF) shl 16) or ((body[2].toInt() and 0xFF) shl 8) or (body[3].toInt() and 0xFF)
                    messages.add(
                        DanmuMessage(
                            "_HEARTBEAT",
                            """{"cmd":"_HEARTBEAT","data":{"popularity":$popularity}}"""
                        )
                    )
                }
            }

            WsOperation.SEND_MSG_REPLY -> {
                when (ProtoVer.fromValue(header.ver)) {
                    ProtoVer.DEFLATE -> {
                        decompressDeflate(body).onSuccess { decompressed ->
                            messages.addAll(parsePacket(ByteString.of(*decompressed), roomId))
                        }
                    }

                    ProtoVer.NORMAL -> {
                        if (body.isNotEmpty()) {
                            messages.add(DanmuMessage("UNKNOWN", String(body, Charsets.UTF_8)))
                        }
                    }

                    else -> {}
                }
            }

            else -> {}
        }
        offset += header.packLen
    }
    return messages
}
