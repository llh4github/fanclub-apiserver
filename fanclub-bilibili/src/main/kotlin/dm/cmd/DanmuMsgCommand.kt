/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * 弹幕消息命令
 * 用于解析 "DANMU_MSG" 类型的命令
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class DanmuMsgCommand(
    /**
     * 命令类型
     */
    override val cmd: String = "DANMU_MSG",
    
    /**
     * 弹幕消息数据
     * 格式：[发送时间, 弹幕类型, 弹幕颜色, 弹幕模式, 字体大小, 弹幕池, 用户ID, 用户名, 弹幕内容, 弹幕ID, 弹幕参数]
     */
    val info: List<Any?>? = null,
    
    /**
     * 弹幕版本 2
     */
    val dmV2: String? = null
) : Command {
    
    /**
     * 用户信息数据类
     * @param timestamp 发送时间，用于去重
     */
    data class UserInfo(
        val uid: Long,
        val username: String,
        val timestamp: Long
    )
    
    /**
     * 发送者信息数据类
     * @param suid 发送者UID
     * @param ruid 接收者UID
     * @param ts 发送时间戳
     * @param level 牌子等级
     */
    data class SenderInfo(
        val name: String,
        val level: Int,
        val ruid: Long,
        val suid: Long,
        val ts: Long
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
        
        val timestamp = when (val value = firstElement.getOrNull(4)) {
            is Int -> value.toLong()
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: 0L
            else -> System.currentTimeMillis()
        }
        // 获取 user 对象 (可能在索引 15 或 17 处)
        val userMap = (firstElement.getOrNull(15) as? Map<String, Any?>)
            .takeIf { it?.isNotEmpty() == true }
            ?: (firstElement.getOrNull(17) as? Map<String, Any?>)
                .takeIf { it?.isNotEmpty() == true }
            ?: return null
        
        val tmpUserMap = userMap["user"] as? Map<String, Any?> ?: return null
        val userBase = tmpUserMap["base"] as? Map<String, Any?> ?: return null
        // 获取 medal 对象
        val medalMap = tmpUserMap["medal"] as? Map<String, Any?> ?: return null
        
        
        return try {
            SenderInfo(
                suid = when (val uid = tmpUserMap["uid"]) {
                    is Long -> uid
                    is Int -> uid.toLong()
                    is Number -> uid.toLong()
                    is String -> uid.toLongOrNull() ?: -1L
                    else -> -1L
                },
                ts = timestamp,
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
