/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.danmu

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 弹幕信息数据体
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class DanmuInfoData(
    /**
     * 组名，如 "live"
     */
    @JsonProperty("group")
    val group: String? = null,

    /**
     * 业务 ID
     */
    @JsonProperty("business_id")
    val businessId: Long = 0,

    /**
     * 刷新行因子
     */
    @JsonProperty("refresh_row_factor")
    val refreshRowFactor: Double = 0.0,

    /**
     * 刷新率
     */
    @JsonProperty("refresh_rate")
    val refreshRate: Int = 0,

    /**
     * 最大延迟（毫秒）
     */
    @JsonProperty("max_delay")
    val maxDelay: Long = 0,

    /**
     * WebSocket 连接令牌
     */
    @JsonProperty("token")
    val token: String? = null,

    /**
     * 弹幕服务器列表
     */
    @JsonProperty("host_list")
    val hostList: List<HostServer> = emptyList()
)

