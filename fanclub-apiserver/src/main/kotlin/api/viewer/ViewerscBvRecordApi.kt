/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.api.viewer

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import llh.fanclubvup.apiserver.dto.JsonWrapper
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerScBvCountSpec
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerScBvRecordPageView
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerScBvRecordQuerySpec
import llh.fanclubvup.apiserver.service.viewer.ViewerScBvRecordService
import llh.fanclubvup.ksp.annon.PublicAccessUrl
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/viewer/sc-bv-record")
@Tag(name = "观众SC点播BV号记录接口")
class ViewerScBvRecordApi(
    private val service: ViewerScBvRecordService
) {

    @PostMapping("/page")
    @Operation(summary = "分页查询")
    fun pageQuery(@RequestBody queryParam: ViewerScBvRecordQuerySpec) =
        JsonWrapper.ok(
            service.pageQuery(ViewerScBvRecordPageView::class, queryParam, queryParam.pageParam)
        )

    @PublicAccessUrl
    @Operation(summary = "SC点播BV号数量统计")
    @PostMapping("/count")
    fun bvCount(@RequestBody @Validated spec: ViewerScBvCountSpec) =
        JsonWrapper.ok(
            service.bvCount(spec)
        )

}
