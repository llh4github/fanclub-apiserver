/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.api.anchor

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import llh.fanclubvup.apiserver.dto.JsonWrapper
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveDurationPageView
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveDurationQuerySpec
import llh.fanclubvup.apiserver.service.anchor.AnchorLiveDurationService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/anchor/live-duration")
@Tag(name = "主播每日直播时长统计接口")
class AnchorLiveDurationApi(
    private val service: AnchorLiveDurationService
) {

    @GetMapping
    fun getById(@RequestParam("id") id: Long) =
        JsonWrapper.ok(service.getById(id))

    @PostMapping("/page")
    @Operation(summary = "分页查询")
    fun pageQuery(@RequestBody queryParam: AnchorLiveDurationQuerySpec) =
        JsonWrapper.ok(
            service.pageQuery(AnchorLiveDurationPageView::class, queryParam, queryParam.pageParam)
        )

}
