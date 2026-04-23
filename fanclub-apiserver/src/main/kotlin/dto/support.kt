package llh.fanclubvup.apiserver.dto

import io.swagger.v3.oas.annotations.media.Schema
import llh.fanclubvup.common.BID

data class StartLiveReq(
    @Schema(title = "直播间 ID")
    val roomId: Long,
    @Schema(title = "直播key")
    val liveKey: String,
)

data class StopLiveReq(
    @Schema(title = "直播间 ID")
    val roomId: Long,
)

data class DanmuReq(
    @Schema(title = "发送者BID")
    val bid: BID,
    @Schema(title = "直播间 ID")
    val roomId: Long,
    @Schema(title = "弹幕内容")
    val text: String,
)
