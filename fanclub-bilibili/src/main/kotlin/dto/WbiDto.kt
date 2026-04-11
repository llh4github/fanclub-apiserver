/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * B站基础响应
 */
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class BiliBaseResponse {
    /**
     * 响应码，0 表示成功
     */
    @JsonProperty("code")
    val code: Int = 0

    /**
     * 响应消息
     */
    @JsonProperty("message")
    val message: String = "成功"
}

/**
 * WBI 图片信息
 */
data class WbiImgDto(
    /**
     * 图片 URL
     */
    @JsonProperty("img_url") val imgUrl: String,
    /**
     * 子图片 URL
     */
    @JsonProperty("sub_url") val subUrl: String
)

/**
 * WBI 数据
 */
data class WbiData(
    /**
     * WBI 图片信息
     */
    @JsonProperty("wbi_img") val wbiImg: WbiImgDto? = null
)

/**
 * WBI 信息响应
 */
data class WbiInfoResponse(
    /**
     * WBI 数据
     */
    val data: WbiData? = null
) : BiliBaseResponse()

/**
 * 弹幕服务器主机信息
 */
data class DanmuHost(
    /**
     * 服务器主机地址
     */
    val host: String,
    /**
     * TCP 端口
     */
    val port: Int,
    /**
     * WebSocket 端口
     */
    @JsonProperty("ws_port") val wsPort: Int,
    /**
     * WebSocket Secure 端口
     */
    @JsonProperty("wss_port") val wssPort: Int
)

/**
 * 弹幕信息数据
 */
data class DanmuInfoData(
    /**
     * 组名
     */
    val group: String? = null,
    /**
     * 业务 ID
     */
    @JsonProperty("business_id") val businessId: Long? = null,
    /**
     * 刷新行因子
     */
    @JsonProperty("refresh_row_factor") val refreshRowFactor: Double? = null,
    /**
     * 刷新率
     */
    @JsonProperty("refresh_rate") val refreshRate: Long? = null,
    /**
     * 最大延迟
     */
    @JsonProperty("max_delay") val maxDelay: Long? = null,
    /**
     * 认证令牌
     */
    val token: String? = null,
    /**
     * 服务器主机列表
     */
    @JsonProperty("host_list") val hostList: List<DanmuHost>? = null
)

/**
 * 弹幕信息响应
 */
data class DanmuInfoResponse(
    /**
     * 弹幕信息数据
     */
    val data: DanmuInfoData? = null
) : BiliBaseResponse()
