package llh.fanclubvup.apiserver.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

/**
 * 删除 IDs 请求
 * @param ids 要删除的 ID 列表，最多 1000 条
 */
@Schema(title = "删除 IDs")
data class DeleteIds(
    @field:Schema(description = "要删除的 ID 列表", example = "[1, 2, 3]")
    @field:Size(max = 1000, message = "最多只能删除 1000 条记录")
    val ids: List<Long>
)
