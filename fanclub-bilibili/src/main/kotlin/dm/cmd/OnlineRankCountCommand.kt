/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 在线人数统计命令
 *
 * 同接数
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OnlineRankCountCommand(
    override val cmd: String = "ONLINE_RANK_COUNT",
    @JsonProperty("data")
    val data: OnlineRankCountData? = null,
) : Command {

    data class OnlineRankCountData(
        val count: Int,
        @JsonProperty("count_text")
        val countText: String,
        @JsonProperty("online_count")
        val onlineCount: Int,
        @JsonProperty("online_count_text")
        val onlineCountText: String
    )
}
