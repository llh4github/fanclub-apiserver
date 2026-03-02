package llh.fanclubvup.apiserver.api

import io.swagger.v3.oas.annotations.tags.Tag
import llh.fanclubvup.apiserver.dto.JsonWrapper
import llh.fanclubvup.apiserver.service.anchor.AnchorInfoService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/anchor/info")
@Tag(name = "主播信息接口")
class AnchorInfoApi(
    private val anchorInfoService: AnchorInfoService
) {

    @RequestMapping
    fun getById(@RequestParam("id") id: Long) =
        JsonWrapper.ok(anchorInfoService.getById(id))

}
