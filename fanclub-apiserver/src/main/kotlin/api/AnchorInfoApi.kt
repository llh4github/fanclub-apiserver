package llh.fanclubvup.apiserver.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import llh.fanclubvup.apiserver.dto.JsonWrapper
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorInfoPageView
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorInfoQuerySpec
import llh.fanclubvup.apiserver.service.anchor.AnchorInfoService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/anchor/info")
@Tag(name = "主播信息接口")
class AnchorInfoApi(
    private val service: AnchorInfoService
) {

    @GetMapping
    @RequestMapping
    fun getById(@RequestParam("id") id: Long) =
        JsonWrapper.ok(service.getById(id))

    @PostMapping("/page")
    @Operation(summary = "分页查询")
    fun pageQuery(@RequestBody queryParam: AnchorInfoQuerySpec) =
        service.pageQuery(AnchorInfoPageView::class, queryParam, queryParam.pageParam)

}
