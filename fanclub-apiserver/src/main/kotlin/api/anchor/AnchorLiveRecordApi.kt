/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.api.anchor

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import llh.fanclubvup.apiserver.dto.JsonWrapper
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveRecordPageView
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveRecordQuerySpec
import llh.fanclubvup.apiserver.service.anchor.AnchorLiveRecordService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

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

    @GetMapping("/is-live")
    @Operation(summary = "查询直播状态值")
    fun isLive(@RequestParam roomId: Long) = JsonWrapper.ok(
        service.isLive(roomId)
    )


}
