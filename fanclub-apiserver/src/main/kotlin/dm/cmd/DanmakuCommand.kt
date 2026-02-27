/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class DanmakuCommand(
    @field:JsonProperty("info") private val rawInfo: List<Any>,
    @JsonProperty("cmd") override val cmd: String
) : Command() {

    // 使用延迟初始化解析复杂结构
    val userInfo: UserInfo by lazy {
        (rawInfo.getOrNull(0) as? List<*>)?.let { userList ->
            UserInfo(
                uid = (userList.getOrNull(0) as? Number)?.toLong() ?: 0L,
                username = (userList.getOrNull(1) as? String) ?: "",
                level = (userList.getOrNull(2) as? Number)?.toInt() ?: 0,
                // 其他字段...
            )
        } ?: UserInfo()
    }

    val danmakuInfo: DanmakuInfo by lazy {
        (rawInfo.getOrNull(1) as? List<*>)?.let { danmakuList ->
            DanmakuInfo(
                content = (danmakuList.getOrNull(0) as? String) ?: "",
                type = (danmakuList.getOrNull(1) as? Number)?.toInt() ?: 0,
                timestamp = (danmakuList.getOrNull(2) as? Number)?.toLong() ?: 0L
            )
        } ?: DanmakuInfo()
    }

    data class UserInfo(
        val uid: Long = 0L,
        val username: String = "",
        val level: Int = 0
    )

    data class DanmakuInfo(
        val content: String = "",
        val type: Int = 0,
        val timestamp: Long = 0L
    )
}