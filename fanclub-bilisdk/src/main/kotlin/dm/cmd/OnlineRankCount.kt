/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dm.cmd

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("ONLINE_RANK_COUNT")
@Deprecated("非核心业务数据，已不再使用")
data class OnlineRankCount(
    @JsonProperty("data")
    val data: OnlineRankCountData? = null,
) : Command() {
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
