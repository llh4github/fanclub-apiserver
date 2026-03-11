package llh.fanclubvup.apiserver.api.anchor

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import llh.fanclubvup.apiserver.dto.JsonWrapper
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorFollowerNumPageView
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorFollowerNumQuerySpec
import llh.fanclubvup.apiserver.service.anchor.AnchorFollowerNumService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/anchor/follower/num")
@Tag(name = "主播粉丝数接口")
class AnchorFollowerNumApi(
    private val service: AnchorFollowerNumService
) {

    @PostMapping("/page")
    @Operation(summary = "分页查询")
    fun pageQuery(@RequestBody queryParam: AnchorFollowerNumQuerySpec) =
        JsonWrapper.ok(
            service.pageQuery(AnchorFollowerNumPageView::class, queryParam, queryParam.pageParam)
        )

}