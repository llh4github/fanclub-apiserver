/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.enums

/**
 * WebSocket 消息体协议版本
 */
enum class ProtoVer(val value: Int) {
    NORMAL(0),        // 普通消息
    HEARTBEAT(1),     // 心跳包
    DEFLATE(2),       // Deflate 压缩
    BROTLI(3);        // Brotli 压缩
}
