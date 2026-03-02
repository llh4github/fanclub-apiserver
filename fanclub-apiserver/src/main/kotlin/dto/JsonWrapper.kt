/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "通用响应结构（JSON）")
data class JsonWrapper<T>(
    @field:Schema(title = "响应码")
    val code: String = "200",
    @field:Schema(title = "响应消息")
    val msg: String = "OK",
    @field:Schema(title = "业务模块名", example = "通常在请求异常时有值")
    val module: String? = null,
    @field:Schema(title = "响应数据")
    val data: T? = null,
) {
    @Schema(title = "是否成功")
    fun getSuccess(): Boolean = code == "200"

    @Schema(title = "响应时间戳")
    fun getTimestamp(): Long = System.currentTimeMillis()

    companion object {
        @JvmStatic
        fun <T> ok(data: T? = null): JsonWrapper<T> {
            return JsonWrapper(data = data)
        }

        @JvmStatic
        fun <T> fail(
            code: String = "9999",
            msg: String = "未知错误",
            module: String = "系统",
            data: T? = null
        ): JsonWrapper<T> {
            return JsonWrapper(
                code = code,
                module = module,
                msg = msg,
                data = data
            )
        }
    }
}
