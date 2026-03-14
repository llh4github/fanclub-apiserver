/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.dm.CommandProcessor
import llh.fanclubvup.bilisdk.enums.ProtoVer
import llh.fanclubvup.bilisdk.enums.WsOperation
import llh.fanclubvup.bilisdk.scraper.BiliWsMsgBizHandler
import okhttp3.WebSocket
import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.readByteString
import okio.ByteString.Companion.toByteString
import org.brotli.dec.BrotliInputStream
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.Inflater

object WsMsgUtil {
    private val objectMapper = jacksonObjectMapper()

    private val logger = KotlinLogging.logger {}

    /**
     * 创建一个要发送给服务器的包
     *
     * @param data 包体数据，可以是 Map、String 或 ByteArray
     * @param operation 操作码
     * @return 整个包的二进制数据
     */
    fun makePacket(data: Any, operation: WsOperation): ByteArray {
        // 处理数据体
        val body: ByteArray = when (data) {
            is Map<*, *> -> {
                // 将 Map 转换为 JSON 字符串并编码为 UTF-8 字节
                val json = objectMapper.writeValueAsString(data)
                json.toByteArray(Charsets.UTF_8)
            }

            is String -> {
                // 将字符串编码为 UTF-8 字节
                data.toByteArray(Charsets.UTF_8)
            }

            is ByteArray -> {
                // 直接使用字节数组
                data
            }

            else -> {
                // 其他类型转换为字符串再编码
                data.toString().toByteArray(Charsets.UTF_8)
            }
        }

        // 头部大小（固定为 16 字节）
        val headerSize = 16
        // 计算整个包的长度
        val packLen = headerSize + body.size

        // 创建头部 ByteBuffer（大端字节序）
        val headerBuffer = ByteBuffer.allocate(headerSize).order(ByteOrder.BIG_ENDIAN)
        headerBuffer.putInt(packLen)      // pack_len: 整个包长度
        headerBuffer.putShort(headerSize.toShort())  // raw_header_size: 头部大小
        headerBuffer.putShort(1.toShort())  // ver: 版本号
        headerBuffer.putInt(operation.value)     // operation: 操作码
        headerBuffer.putInt(1)             // seq_id: 序列号

        // 转换头部为字节数组
        val header = headerBuffer.array()

        // 拼接头部和数据体
        return header + body
    }

    // 头部大小（固定为 16 字节）
    private const val HEADER_SIZE = 16

    /**
     * 解析WebSocket数据包，返回泛型类型
     *
     * @param packet 完整的数据包字节数组
     */
    fun parsePacket(
        packet: ByteString,
        webSocket: WebSocket,
        biliWsMsgBizHandler: BiliWsMsgBizHandler,
    ) {

        // 检查数据包长度是否足够
        if (packet.size < HEADER_SIZE) {
            throw IllegalArgumentException("数据包长度不足，至少需要16字节的头部")
        }

        val packetBuffer = Buffer().write(packet)
        val packLen = packetBuffer.readInt()      // pack_len: 整个包长度
        val rawHeaderSize = packetBuffer.readShort().toInt()  // raw_header_size: 头部大小
        val ver = packetBuffer.readShort().toInt()  // ver: 版本号
        val operation = packetBuffer.readInt()     // operation: 操作码
        val seqId = packetBuffer.readInt()         // seq_id: 序列号

        // 检查数据包长度是否与头部中的长度一致
//        if (packet.size != packLen) {
//            logger.error { "数据包长度与头部中的长度不一致 ${packet.size} $packLen" }
//            throw IllegalArgumentException("数据包长度与头部中的长度不一致")
//        }

        logger.debug {
            "WebSocket 数据包解析结果：" +
                    "packLen=$packLen, rawHeaderSize=$rawHeaderSize, ver=$ver, " +
                    "operation=$operation (${operation}), " +
                    "seqId=$seqId packetBuffer-size: ${packetBuffer.size}"
        }
        val body = Buffer()
        var offset = 0
        while (offset < packet.size) {
            packetBuffer.copyTo(
                body,
                0,
                (packLen - rawHeaderSize).toLong()
            )
            offset += packLen
            if (WsOperation.HEARTBEAT_REPLY.value == operation) {
                val popularity = body.readInt()
                val jsonResult = """
                   {"cmd":"_HEARTBEAT","data":{"popularity":$popularity}} 
                """.trimIndent()
                logger.debug { jsonResult }
            } else if (WsOperation.SEND_MSG_REPLY.value == operation) {
                when (ver) {
                    ProtoVer.BROTLI.value -> {
                        val a = BrotliInputStream(body.inputStream()).use { brotliStream ->
                            brotliStream.readAllBytes()
                        }.toByteString()
                        logger.debug { "使用 BROTLI 算法解码请求" }
                        parsePacket(a, webSocket, biliWsMsgBizHandler)
                    }

                    ProtoVer.DEFLATE.value -> {
                        val inflater = Inflater()
                        val ba = ByteBuffer.allocate(1024)
                        inflater.setInput(body.readByteArray())
                        while (!inflater.finished()) {
                            inflater.inflate(ba)
                        }
                        inflater.end()
                        logger.debug { "使用 ZIP 算法解码请求" }
                        parsePacket(ba.toByteString(), webSocket, biliWsMsgBizHandler)
                    }

                    ProtoVer.NORMAL.value -> {
                        if (body.size > 0) {
                            CommandProcessor.parseCommand(body.readString(Charsets.UTF_8))?.let {
                                biliWsMsgBizHandler.handleMsg(it)
                            }
                        }
                    }

                    else -> {
                        logger.error { "未识别的协议版本: $ver" }
                    }
                }

            } else if (WsOperation.AUTH_REPLY.value == operation) {
                val reply = makePacket(emptyMap<String, String>(), WsOperation.HEARTBEAT)
                webSocket.send(reply.toByteString())
                logger.debug { "回了条心跳" }
            } else {
                logger.warn { "未知指令: \n${packet.base64()}" }
            }
            body.clear()
        }
    }

}
