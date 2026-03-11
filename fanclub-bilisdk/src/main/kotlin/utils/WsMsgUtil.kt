/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.utils

import llh.fanclubvup.bilisdk.enums.WsOperation
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WsMsgUtil {
    private val objectMapper = jacksonObjectMapper()

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


//    fun makePacket(body: ByteArray, op: WsOperation): ByteArray {
//        val headerSize = 16 // HEADER_STRUCT.size
//        val packLen = headerSize + body.size
//        val ver = 1
//        val seqId = 1
//
//        val buffer = ByteBuffer.allocate(packLen).order(ByteOrder.BIG_ENDIAN)
//        buffer.putInt(packLen)
//        buffer.putShort(headerSize.toShort())
//        buffer.putShort(ver.toShort())
//        buffer.putInt(op.value)
//        buffer.putInt(seqId)
//        buffer.put(body)
//
//        return buffer.array()
//    }
}