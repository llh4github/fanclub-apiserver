/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.api.anchor

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import llh.fanclubvup.apiserver.dto.JsonWrapper
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveRecordLiveStatus
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveRecordPageView
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveRecordQuerySpec
import llh.fanclubvup.apiserver.service.anchor.AnchorLiveRecordService
import llh.fanclubvup.ksp.annon.PublicAccessUrl
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/anchor/live-record")
@Tag(name = "主播直播记录接口")
class AnchorLiveRecordApi(
    private val service: AnchorLiveRecordService
) {

    @PostMapping("/page")
    @Operation(summary = "分页查询")
    fun pageQuery(@RequestBody queryParam: AnchorLiveRecordQuerySpec) =
        JsonWrapper.ok(
            service.pageQuery(AnchorLiveRecordPageView::class, queryParam, queryParam.pageParam)
        )

    @PublicAccessUrl
    @GetMapping("/live-status")
    @Operation(summary = "查询直播状态")
    fun liveStatus(
        @Parameter(description = "直播间 ID", required = true)
        @RequestParam roomId: Long
    ) = JsonWrapper.ok(
        service.fetchLiveStatus(roomId)
    )

    @PublicAccessUrl
    @GetMapping("/last-endLive")
    @Operation(summary = "查询最近几次已完成的直播")
    fun lastLiveStatus(
        @Parameter(description = "直播间 ID", required = true)
        @RequestParam roomId: Long,
        @Parameter(description = "查询最近几条记录，最大值为 10", required = true)
        @RequestParam last: Int
    ): JsonWrapper<List<AnchorLiveRecordLiveStatus>> {
        val last = if (last >= 10) {
            10
        } else {
            last
        }
        return JsonWrapper.ok(
            service.fetchEndLiveRecord(roomId, last)
        )
    }

}
