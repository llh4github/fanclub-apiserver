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

/**
 * WebSocket 数据包头部结构
 *
 * @property packLen 数据包总长度（包含头部和正文）
 * @property headerSize 头部大小，固定为16字节
 * @property ver 协议版本，0=普通文本, 1=心跳, 2=deflate压缩, 3=brotli压缩
 * @property operation 操作码，定义消息类型（握手、心跳、认证等）
 * @property seqId 序列号，用于标识数据包顺序
 */
data class PacketHeader(val packLen: Int, val headerSize: Short, val ver: Short, val operation: Int, val seqId: Int)

/**
 * 弹幕消息数据模型
 *
 * @property cmd 命令类型，标识消息的具体业务类型（如 DANMU_MSG、SEND_GIFT 等）
 * @property rawData 原始 JSON 数据字符串，需进一步解析获取详细内容
 */
data class DanmuMessage(val cmd: String, val rawData: String)

/**
 * B站直播间 WebSocket 认证信息
 *
 * 用于建立 WebSocket 连接时发送认证包，验证用户身份和房间权限
 *
 * @property uid 用户UID，B站用户唯一标识
 * @property roomid 直播间ID
 * @property protover 协议版本号，推荐使用3（brotli压缩）
 * @property buvid 浏览器唯一标识，用于设备指纹识别
 * @property supportAck 是否支持ACK确认机制
 * @property scene 使用场景，通常为"room"表示直播间场景
 * @property platform 平台类型，如"web"、"android"等
 * @property type 认证类型，2表示标准认证
 * @property key 认证密钥/令牌，从弹幕服务器接口获取的token
 */
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
        /**
         * 创建默认的弹幕认证对象
         *
         * @param roomId 直播间ID
         * @param token 认证令牌，通过 getDanmuInfo 接口获取
         * @param uid 用户UID，默认为测试账号
         * @param buvid 设备标识，默认为固定值
         * @return 配置好的 DanmuAuth 实例
         */
        fun create(
            roomId: Long,
            token: String,
            uid: BID,
            buvid: String,
        ) = DanmuAuth(
            uid = uid, roomid = roomId, protover = 3,
            buvid = buvid,
            supportAck = true, scene = "room", platform = "web", type = 2, key = token
        )
    }
}
