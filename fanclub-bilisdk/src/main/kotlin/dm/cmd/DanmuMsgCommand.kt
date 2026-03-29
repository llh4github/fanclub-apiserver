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

    /**
     * 用户信息数据类
     * @param timestamp 发送时间，用于去重
     */
    data class UserInfo(
        @JsonProperty("uid") val uid: Long,
        @JsonProperty("username") val username: String,
        @JsonProperty("timestamp") val timestamp: Long,
    )

    data class SenderInfo(
        @JsonProperty("name") val name: String,
        @JsonProperty("level") val level: Int,
        @JsonProperty("ruid") val ruid: Long,
    )

    /**
     * 获取弹幕内容
     */
    fun getContent(): String? {
        return info?.getOrNull(1) as? String
    }

    /**
     * 获取用户信息（从 info[0] 和 info[2] 中提取）
     */
    fun getUserInfo(): UserInfo? {
        val first = info?.getOrNull(0) as? List<Any?> ?: return null
        val raw = info.getOrNull(2) as? List<Any?> ?: return null
        val timestamp = when (val value = first.getOrNull(4)) {
            is Int -> value.toLong()
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: 0L
            else -> 0L
        }
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
            timestamp = timestamp
        )
    }

    /**
     * 提取勋章详细信息（从 info[0][15].user.medal 中提取）
     * 这是推荐的方式，直接从结构化数据中获取
     * @return MedalInfo? 勋章信息，包含勋章名、等级、房主 UID
     */
    fun extractSendInfo(): SenderInfo? {
        val firstElement = info?.getOrNull(0) as? List<Any?> ?: return null

        // 获取 user 对象（索引 15）
        val userMap = firstElement.getOrNull(15) as? Map<String, Any?> ?: return null

        val tmpUserMap = userMap["user"] as? Map<String, Any?> ?: return null
        val userBase = tmpUserMap["base"] as? Map<String, Any?> ?: return null
        // 获取 medal 对象
        val medalMap = tmpUserMap["medal"] as? Map<String, Any?> ?: return null

        return try {
            SenderInfo(
                name = (userBase["name"] as? String) ?: "",
                level = when (val lvl = medalMap["level"]) {
                    is Int -> lvl
                    is Number -> lvl.toInt()
                    else -> 0
                },
                ruid = when (val ruid = medalMap["ruid"]) {
                    is Long -> ruid
                    is Int -> ruid.toLong()
                    is Number -> ruid.toLong()
                    is String -> ruid.toLongOrNull() ?: 0L
                    else -> 0L
                }
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取勋章信息（兼容旧版本，从 info[3] 数组中提取）
     * @deprecated 推荐使用 [extractSendInfo]
     */
    @Deprecated("Use extractMedalDetail() instead", ReplaceWith("extractMedalDetail()"))
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