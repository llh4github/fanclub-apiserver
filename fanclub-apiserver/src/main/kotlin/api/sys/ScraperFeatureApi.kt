/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.api.sys

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import llh.fanclubvup.apiserver.dto.JsonWrapper
import llh.fanclubvup.apiserver.entity.sys.dto.ScraperFeaturePageView
import llh.fanclubvup.apiserver.entity.sys.dto.ScraperFeatureQuerySpec
import llh.fanclubvup.apiserver.service.sys.ScraperFeatureService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sys/scraper/feature")
@Tag(name = "爬虫功能配置接口")
class ScraperFeatureApi(
    private val service: ScraperFeatureService
) {

    @PostMapping("/page")
    @Operation(summary = "分页查询")
    fun pageQuery(@RequestBody queryParam: ScraperFeatureQuerySpec) =
        JsonWrapper.ok(
            service.pageQuery(ScraperFeaturePageView::class, queryParam, queryParam.pageParam)
        )

}