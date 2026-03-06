/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.liveroom

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 直播间数据
 */
data class LiveRoomData(
    @JsonProperty("uid")
    val uid: Long,  // 主播 UID
    
    @JsonProperty("room_id")
    val roomId: Long,  // 直播间 ID
    
    @JsonProperty("short_id")
    val shortId: Int,  // 短号
    
    @JsonProperty("attention")
    val attention: Int,  // 关注数
    
    @JsonProperty("online")
    val online: Int,  // 在线人数
    
    @JsonProperty("is_portrait")
    val isPortrait: Boolean,  // 是否竖屏
    
    @JsonProperty("description")
    val description: String,  // 直播间描述
    
    @JsonProperty("live_status")
    val liveStatus: Int,  // 直播状态 0:未开播 1:直播中
    
    @JsonProperty("area_id")
    val areaId: Int,  // 分区 ID
    
    @JsonProperty("parent_area_id")
    val parentAreaId: Int,  // 父分区 ID
    
    @JsonProperty("parent_area_name")
    val parentAreaName: String,  // 父分区名称
    
    @JsonProperty("old_area_id")
    val oldAreaId: Int,  // 旧分区 ID
    
    @JsonProperty("background")
    val background: String,  // 背景图
    
    @JsonProperty("title")
    val title: String,  // 直播间标题
    
    @JsonProperty("user_cover")
    val userCover: String,  // 封面图
    
    @JsonProperty("keyframe")
    val keyframe: String,  // 关键帧截图
    
    @JsonProperty("is_strict_room")
    val isStrictRoom: Boolean,  // 是否严格房间
    
    @JsonProperty("live_time")
    val liveTime: String,  // 开播时间
    
    @JsonProperty("tags")
    val tags: String,  // 标签
    
    @JsonProperty("is_anchor")
    val isAnchor: Int,  // 是否主播
    
    @JsonProperty("room_silent_type")
    val roomSilentType: String,  // 禁言类型
    
    @JsonProperty("room_silent_level")
    val roomSilentLevel: Int,  // 禁言等级
    
    @JsonProperty("room_silent_second")
    val roomSilentSecond: Int,  // 禁言秒数
    
    @JsonProperty("area_name")
    val areaName: String,  // 分区名称
    
    @JsonProperty("pendants")
    val pendants: String,  // 挂件
    
    @JsonProperty("area_pendants")
    val areaPendants: String,  // 分区挂件
    
    @JsonProperty("hot_words")
    val hotWords: List<String> = emptyList(),  // 热词列表
    
    @JsonProperty("hot_words_status")
    val hotWordsStatus: Int,  // 热词状态
    
    @JsonProperty("verify")
    val verify: String,  // 认证信息
    
    @JsonProperty("new_pendants")
    val newPendants: NewPendants? = null,  // 新挂件信息
    
    @JsonProperty("up_session")
    val upSession: String,  // 主播会话 ID
    
    @JsonProperty("pk_status")
    val pkStatus: Int,  // PK 状态
    
    @JsonProperty("pk_id")
    val pkId: Long,  // PK ID
    
    @JsonProperty("battle_id")
    val battleId: Long,  // 战斗 ID
    
    @JsonProperty("allow_change_area_time")
    val allowChangeAreaTime: Int,  // 允许切换分区时间
    
    @JsonProperty("allow_upload_cover_time")
    val allowUploadCoverTime: Int,  // 允许上传封面时间
    
    @JsonProperty("studio_info")
    val studioInfo: StudioInfo? = null  // 演播室信息
)
