/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.api.anchor

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import llh.fanclubvup.apiserver.dto.DeleteIds
import llh.fanclubvup.apiserver.dto.JsonWrapper
import llh.fanclubvup.apiserver.dto.PageResponse
import llh.fanclubvup.apiserver.entity.anchor.dto.*
import llh.fanclubvup.apiserver.service.anchor.AnchorSongService
import llh.fanclubvup.apiserver.utils.SecurityContextUtil
import llh.fanclubvup.common.excptions.AppRuntimeException
import llh.fanclubvup.ksp.annon.PublicAccessUrl
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/anchor/song")
@Tag(name = "主播歌曲接口")
class AnchorSongApi(
    private val service: AnchorSongService,
) {

    @GetMapping
    @Operation(summary = "查询歌曲详情")
    fun getById(@RequestParam("id") id: Long) =
        JsonWrapper.ok(service.getById(id))

    @PublicAccessUrl
    @PostMapping("/page")
    @Operation(summary = "分页查询")
    fun pageQuery(
        @RequestBody queryParam: AnchorSongQuerySpec
    ): JsonWrapper<PageResponse<AnchorSongPageView>> {
        return JsonWrapper.ok(
            service.pageQuery(
                AnchorSongPageView::class,
                queryParam,
                queryParam.pageParam,
            )
        )
    }

    @PreAuthorize("hasAnyRole('ADMIN','ANCHOR')")
    @PutMapping("update")
    @Operation(summary = "修改歌曲信息")
    fun update(
        @RequestBody @Validated
        input: AnchorSongUpdateInput
    ): JsonWrapper<Void> {

        if (SecurityContextUtil.isAnchor()) {
            val bid = SecurityContextUtil.bidOrThrow()
            val saveInput = input.copy(bid = bid)
            service.updateById(
                saveInput,
                existBySpec = AnchorSongUniqueForUpdateSpec(input.id, bid, input.name)
            )
            return JsonWrapper.ok()
        }
        if (SecurityContextUtil.isAdmin()) {
            service.updateById(
                input,
                existBySpec = AnchorSongUniqueForUpdateSpec(input.id, input.bid, input.name)
            )
            return JsonWrapper.ok()
        }
        throw AppRuntimeException("无权限")
    }

    @PreAuthorize("hasAnyRole('ADMIN','ANCHOR')")
    @PostMapping("add")
    @Operation(summary = "添加歌曲")
    fun add(
        @RequestBody @Validated
        input: AnchorSongAddInput
    ): JsonWrapper<Boolean> {
        if (SecurityContextUtil.isAnchor()) {
            val bid = SecurityContextUtil.bidOrThrow()
            val saveInput = input.copy(bid = bid)
            service.save(saveInput, existBySpec = AnchorSongUniqueSpec(bid, input.name))
        }
        if (SecurityContextUtil.isAdmin()) {
            service.save(input, existBySpec = AnchorSongUniqueSpec(input.bid, input.name))
            return JsonWrapper.ok()
        }
        return JsonWrapper.fail("ADD_SONG", "添加失败")
    }

    @PreAuthorize("hasAnyRole('ADMIN','ANCHOR')")
    @DeleteMapping("delete")
    @Operation(summary = "删除歌曲")
    fun delete(
        @RequestBody @Validated
        ids: DeleteIds
    ): JsonWrapper<Int> {
        val bid = if (SecurityContextUtil.isAnchor()) {
            SecurityContextUtil.bidOrThrow()
        } else if (SecurityContextUtil.isAdmin()) {
            null
        } else {
            throw AppRuntimeException("无权限")
        }

        val cnt = service.deleteByIds(ids, bid)
        return JsonWrapper.ok(cnt)
    }

}
