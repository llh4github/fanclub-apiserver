package llh.fanclubvup.apiserver.api.viewer

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import llh.fanclubvup.apiserver.dto.JsonWrapper
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerDanmuCountPageView
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerDanmuCountQuerySpec
import llh.fanclubvup.apiserver.service.viewer.ViewerDanmuCountService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/viewer/danmu-count")
@Tag(name = "观众弹幕数量统计接口")
class ViewerDanmuCountApi(
    private val service: ViewerDanmuCountService
) {

    @PostMapping("/page")
    @Operation(summary = "分页查询")
    fun pageQuery(@RequestBody queryParam: ViewerDanmuCountQuerySpec) =
        JsonWrapper.ok(
            service.pageQuery(ViewerDanmuCountPageView::class, queryParam, queryParam.pageParam)
        )

}
