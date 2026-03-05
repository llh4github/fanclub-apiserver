/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.danmu

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 弹幕服务器主机信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class HostServer(
    /**
     * 服务器主机地址
     */
    @JsonProperty("host")
    val host: String? = null,

    /**
     * TCP 端口
     */
    @JsonProperty("port")
    val port: Int = 0,

    /**
     * WSS (WebSocket Secure) 端口
     */
    @JsonProperty("wss_port")
    val wssPort: Int = 0,

    /**
     * WS (WebSocket) 端口
     */
    @JsonProperty("ws_port")
    val wsPort: Int = 0
)

