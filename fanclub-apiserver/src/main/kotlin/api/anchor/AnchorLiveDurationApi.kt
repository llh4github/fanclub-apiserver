/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.api.anchor

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import llh.fanclubvup.apiserver.dto.JsonWrapper
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveDurationPageView
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveDurationQuerySpec
import llh.fanclubvup.apiserver.service.anchor.AnchorLiveDurationService
import llh.fanclubvup.ksp.annon.PublicAccessUrl
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/anchor/live-duration")
@Tag(name = "主播每日直播时长统计接口")
class AnchorLiveDurationApi(
    private val service: AnchorLiveDurationService
) {

    @PostMapping("/page")
    @Operation(summary = "分页查询")
    fun pageQuery(@RequestBody queryParam: AnchorLiveDurationQuerySpec) =
        JsonWrapper.ok(
            service.pageQuery(AnchorLiveDurationPageView::class, queryParam, queryParam.pageParam)
        )

    @PublicAccessUrl
    @Operation(summary = "查询历史数量")
    @PostMapping("/query-history")
    fun queryHistory(
        @RequestParam("roomId") @Schema(title = "直播间 ID", description = "直播间 ID") roomId: Long,
        @RequestParam("date") @Schema(title = "截止日期", description = "查询此日期之前的历史数据") date: LocalDate
    ) = JsonWrapper.ok(
        service.fetchLiveDurationHistory(roomId, date)
    )

}
