/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.api.viewer

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import llh.fanclubvup.apiserver.dto.JsonWrapper
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerGuardBuyRecordPageView
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerGuardBuyRecordQuerySpec
import llh.fanclubvup.apiserver.service.viewer.ViewerGuardBuyRecordService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/viewer/guard-buy-record")
@Tag(name = "观众舰长购买记录接口")
class ViewerGuardBuyRecordApi(
    private val service: ViewerGuardBuyRecordService
) {

    @PostMapping("/page")
    @Operation(summary = "分页查询")
    fun pageQuery(@RequestBody queryParam: ViewerGuardBuyRecordQuerySpec) = 
        JsonWrapper.ok(
            service.pageQuery(ViewerGuardBuyRecordPageView::class, queryParam, queryParam.pageParam)
        )
}
