/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.enums

/**
 * 弹幕服务器枚举
 */
enum class DanmakuServerEnums(
    val host: String,
    val port: Int,
    val wssPort: Int,
    val wsPort: Int
) {
    /** 默认弹幕服务器配置 */
    DEFAULT_DANMAKU_SERVER_LIST(
        host = "broadcastlv.chat.bilibili.com",
        port = 2243,
        wssPort = 443,
        wsPort = 2244
    );

    companion object {
        /**
         * 获取默认的弹幕服务器配置列表
         * @return 弹幕服务器配置列表
         */
        fun getDefaultServerList(): List<DanmakuServerEnums> {
            return entries.toList()
        }

        /**
         * 根据主机名获取对应的枚举值
         * @param host 主机名
         * @return 对应的 DanmakuServerConfig 枚举值，如果找不到则返回 null
         */
        fun fromHost(host: String): DanmakuServerEnums? {
            return entries.find { it.host == host }
        }
    }
}
