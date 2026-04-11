/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.api.anchor

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import llh.fanclubvup.apiserver.dto.JsonWrapper
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveSchedulePageView
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveScheduleQuerySpec
import llh.fanclubvup.apiserver.service.anchor.AnchorLiveScheduleService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/anchor/live-schedule")
@Tag(name = "主播直播日程接口")
class AnchorLiveScheduleApi(
    private val service: AnchorLiveScheduleService
) {

    @PostMapping("/page")
    @Operation(summary = "分页查询")
    fun pageQuery(@RequestBody queryParam: AnchorLiveScheduleQuerySpec) =
        JsonWrapper.ok(
            service.pageQuery(AnchorLiveSchedulePageView::class, queryParam, queryParam.pageParam)
        )

}
