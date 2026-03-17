/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * 弹幕消息命令
 */
@JsonTypeName("DANMU_MSG")
@JsonIgnoreProperties(ignoreUnknown = true)
data class DanmuMsgCommand(
    @field:JsonProperty("info")
    val info: List<Any?>? = null,
    @field:JsonProperty("dm_v2")
    val dmV2: String? = null
) : Command() {

    data class UserInfo(
        @JsonProperty("uid") val uid: Long,
        @JsonProperty("username") val username: String,
    )

    /**
     * 获取弹幕内容
     */
    fun getContent(): String? {
        return info?.getOrNull(1) as? String
    }

    /**
     * 获取用户信息
     */
    fun getUserInfo(): UserInfo? {
        val raw = info?.getOrNull(2) as? List<Any?> ?: return null
        val uid = when (val value = raw.getOrNull(0)) {
            is Long -> value
            is Int -> value.toLong()
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: 0L
            else -> 0L
        }
        return UserInfo(
            uid = uid,
            username = raw.getOrNull(1) as? String ?: "",
        )
    }

    /**
     * 获取勋章信息
     */
    fun getMedalInfo(): List<Any?>? {
        return info?.getOrNull(3) as? List<Any?>
    }

    /**
     * 获取弹幕元数据
     */
    fun getDanmuMeta(): Map<String, Any?>? {
        return info?.getOrNull(9) as? Map<String, Any?>
    }
}