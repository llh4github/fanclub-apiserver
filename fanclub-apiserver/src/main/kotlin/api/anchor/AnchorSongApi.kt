/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.api.anchor

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import llh.fanclubvup.apiserver.dto.JsonWrapper
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorSongPageView
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorSongQuerySpec
import llh.fanclubvup.apiserver.service.anchor.AnchorSongService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/anchor/song")
@Tag(name = "主播歌曲接口")
class AnchorSongApi(
    private val service: AnchorSongService
) {

    @GetMapping
    fun getById(@RequestParam("id") id: Long) =
        JsonWrapper.ok(service.getById(id))

    @PostMapping("/page")
    @Operation(summary = "分页查询")
    fun pageQuery(@RequestBody queryParam: AnchorSongQuerySpec) =
        JsonWrapper.ok(
            service.pageQuery(AnchorSongPageView::class, queryParam, queryParam.pageParam)
        )

}
