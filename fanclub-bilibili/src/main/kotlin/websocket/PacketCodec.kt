/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilibili.utils.JsonUtils
import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.brotli.dec.BrotliInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.Inflater

private const val HEADER_SIZE = 16

// JSON 映射器
private val objectMapper = JsonUtils.mapper

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
    val logger = KotlinLogging.logger {}
    return try {
        // 使用 nowrap=true 来处理原始的 deflate 数据，跳过 zlib 头部
        val inflater = Inflater(true)
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        try {
            inflater.setInput(data)
            var totalCount = 0
            while (!inflater.finished() && !inflater.needsInput()) {
                val count = inflater.inflate(buffer)
                if (count > 0) {
                    outputStream.write(buffer, 0, count)
                    totalCount += count
                }
            }
            logger.debug { "解压成功，原始大小: ${data.size} 字节，解压后大小: $totalCount 字节" }
            Result.success(outputStream.toByteArray())
        } finally {
            inflater.end()
            outputStream.close()
        }
    } catch (e: Exception) {
        logger.error(e) { "解压失败，数据大小: ${data.size} 字节" }
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
//        if (header.packLen > packetBytes.size - offset) break

        val bodyStart = offset + header.headerSize.toInt()
        val bodyEnd = offset + header.packLen
        val body = packetBytes.sliceArray(bodyStart until bodyEnd)

        when (WsOperation.fromValue(header.operation)) {
            WsOperation.HEARTBEAT_REPLY -> {
                if (body.size >= 4) {
                    val popularity =
                        ((body[0].toInt() and 0xFF) shl 24) or
                                ((body[1].toInt() and 0xFF) shl 16) or
                                ((body[2].toInt() and 0xFF) shl 8) or
                                (body[3].toInt() and 0xFF)
                    messages.add(
                        DanmuMessage(
                            "_HEARTBEAT",
                            """{"cmd":"_HEARTBEAT","data":{"popularity":$popularity}}"""
                        )
                    )
                }
            }

            WsOperation.SEND_MSG_REPLY -> {
                val protoVer = ProtoVer.fromValue(header.ver)
                when (protoVer) {
                    ProtoVer.DEFLATE -> {
                        decompressDeflate(body).onSuccess { decompressed ->
                            messages.addAll(parsePacket(ByteString.of(*decompressed), roomId))
                        }.onFailure { e ->
                            logger.error(e) { "DEFLATE 解压失败" }
                        }
                    }

                    ProtoVer.NORMAL -> {
                        if (body.isNotEmpty()) {
                            try {
                                val json = String(body, Charsets.UTF_8)
                                // 从 JSON 数据中提取 cmd 字段
                                val tree = objectMapper.readTree(json)
                                val cmd = tree.get("cmd")?.asString() ?: "UNKNOWN"
                                messages.add(DanmuMessage(cmd, json))
                            } catch (e: Exception) {
                                logger.error(e) { "解析普通消息失败" }
                                // 解析失败时使用 UNKNOWN
                                val json = String(body, Charsets.UTF_8)
                                messages.add(DanmuMessage("UNKNOWN", json))
                            }
                        }
                    }

                    ProtoVer.BROTLI -> {
                        // Brotli 压缩，使用 BrotliInputStream 解压
                        try {
                            BrotliInputStream(body.inputStream())
                                .use { brotliStream ->
                                    val decompressed = brotliStream.readAllBytes()
                                    logger.debug { "Brotli 解压成功，原始大小: ${body.size} 字节，解压后大小: ${decompressed.size} 字节" }
                                    messages.addAll(parsePacket(decompressed.toByteString(), roomId))
                                }
                        } catch (e: Exception) {
                            logger.error(e) { "Brotli 解压失败" }
                        }
                    }

                    ProtoVer.HEARTBEAT -> {
                        // 心跳消息，忽略
                    }

                    null -> {
                        // 未知协议版本
                        logger.warn { "未知协议版本: ${header.ver}" }
                    }
                }
            }

            else -> {
                // 其他操作类型
                logger.debug { "未处理的操作类型: ${header.operation}" }
            }
        }
        offset += header.packLen
    }
    return messages
}
