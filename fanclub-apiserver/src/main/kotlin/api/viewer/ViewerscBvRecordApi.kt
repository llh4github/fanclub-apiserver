package llh.fanclubvup.apiserver.api.viewer

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import llh.fanclubvup.apiserver.dto.JsonWrapper
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerScBvRecordPageView
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerScBvRecordQuerySpec
import llh.fanclubvup.apiserver.service.viewer.ViewerScBvRecordService
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

}
