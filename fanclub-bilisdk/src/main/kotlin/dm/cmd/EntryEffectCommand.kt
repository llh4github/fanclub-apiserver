/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * 入场特效命令
 */
@JsonTypeName("ENTRY_EFFECT")
@JsonIgnoreProperties(ignoreUnknown = true)
data class EntryEffectCommand(
    @field:JsonProperty("data")
    val data: EntryEffectData? = null
): Command() {

    /**
     * 入场特效数据
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class EntryEffectData(
        @field:JsonProperty("id")
        val id: Int? = null,
        
        @field:JsonProperty("uid")
        val uid: Long? = null,
        
        @field:JsonProperty("target_id")
        val targetId: Long? = null,
        
        @field:JsonProperty("mock_effect")
        val mockEffect: Int? = null,
        
        @field:JsonProperty("face")
        val face: String? = null,
        
        @field:JsonProperty("privilege_type")
        val privilegeType: Int? = null,
        
        @field:JsonProperty("copy_writing")
        val copyWriting: String? = null,
        
        @field:JsonProperty("copy_color")
        val copyColor: String? = null,
        
        @field:JsonProperty("highlight_color")
        val highlightColor: String? = null,
        
        @field:JsonProperty("priority")
        val priority: Int? = null,
        
        @field:JsonProperty("basemap_url")
        val basemapUrl: String? = null,
        
        @field:JsonProperty("show_avatar")
        val showAvatar: Int? = null,
        
        @field:JsonProperty("effective_time")
        val effectiveTime: Int? = null,
        
        @field:JsonProperty("web_basemap_url")
        val webBasemapUrl: String? = null,
        
        @field:JsonProperty("web_effective_time")
        val webEffectiveTime: Int? = null,
        
        @field:JsonProperty("web_effect_close")
        val webEffectClose: Int? = null,
        
        @field:JsonProperty("web_close_time")
        val webCloseTime: Int? = null,
        
        @field:JsonProperty("business")
        val business: Int? = null,
        
        @field:JsonProperty("copy_writing_v2")
        val copyWritingV2: String? = null,
        
        @field:JsonProperty("icon_list")
        val iconList: List<Any?>? = null,
        
        @field:JsonProperty("max_delay_time")
        val maxDelayTime: Int? = null,
        
        @field:JsonProperty("trigger_time")
        val triggerTime: Long? = null,
        
        @field:JsonProperty("identities")
        val identities: Int? = null,
        
        @field:JsonProperty("effect_silent_time")
        val effectSilentTime: Int? = null,
        
        @field:JsonProperty("effective_time_new")
        val effectiveTimeNew: Int? = null,
        
        @field:JsonProperty("web_dynamic_url_webp")
        val webDynamicUrlWebp: String? = null,
        
        @field:JsonProperty("web_dynamic_url_apng")
        val webDynamicUrlApng: String? = null,
        
        @field:JsonProperty("mobile_dynamic_url_webp")
        val mobileDynamicUrlWebp: String? = null,
        
        @field:JsonProperty("wealthy_info")
        val wealthyInfo: WealthyInfo? = null,
        
        @field:JsonProperty("new_style")
        val newStyle: Int? = null,
        
        @field:JsonProperty("is_mystery")
        val isMystery: Boolean? = null,
        
        @field:JsonProperty("uinfo")
        val uinfo: UserInfo? = null,
        
        @field:JsonProperty("full_cartoon_id")
        val fullCartoonId: Int? = null,
        
        @field:JsonProperty("priority_level")
        val priorityLevel: Int? = null,
        
        @field:JsonProperty("wealth_style_info")
        val wealthStyleInfo: WealthStyleInfo? = null
    )
    
    /**
     * 财富信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class WealthyInfo(
        @field:JsonProperty("uid")
        val uid: Long? = null,
        
        @field:JsonProperty("level")
        val level: Int? = null,
        
        @field:JsonProperty("level_total_score")
        val levelTotalScore: Int? = null,
        
        @field:JsonProperty("cur_score")
        val curScore: Int? = null,
        
        @field:JsonProperty("upgrade_need_score")
        val upgradeNeedScore: Int? = null,
        
        @field:JsonProperty("status")
        val status: Int? = null,
        
        @field:JsonProperty("dm_icon_key")
        val dmIconKey: String? = null
    )
    
    /**
     * 用户信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class UserInfo(
        @field:JsonProperty("uid")
        val uid: Long? = null,
        
        @field:JsonProperty("base")
        val base: UserBase? = null,
        
        @field:JsonProperty("medal")
        val medal: MedalInfo? = null,
        
        @field:JsonProperty("wealth")
        val wealth: WealthInfo? = null,
        
        @field:JsonProperty("title")
        val title: Any? = null,
        
        @field:JsonProperty("guard")
        val guard: GuardInfo? = null,
        
        @field:JsonProperty("uhead_frame")
        val uheadFrame: Any? = null,
        
        @field:JsonProperty("guard_leader")
        val guardLeader: Any? = null
    )
    
    /**
     * 用户基础信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class UserBase(
        @field:JsonProperty("name")
        val name: String? = null,
        
        @field:JsonProperty("face")
        val face: String? = null,
        
        @field:JsonProperty("name_color")
        val nameColor: Int? = null,
        
        @field:JsonProperty("is_mystery")
        val isMystery: Boolean? = null,
        
        @field:JsonProperty("risk_ctrl_info")
        val riskCtrlInfo: Any? = null,
        
        @field:JsonProperty("origin_info")
        val originInfo: Any? = null,
        
        @field:JsonProperty("official_info")
        val officialInfo: Any? = null,
        
        @field:JsonProperty("name_color_str")
        val nameColorStr: String? = null
    )
    
    /**
     * 勋章信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class MedalInfo(
        @field:JsonProperty("name")
        val name: String? = null,
        
        @field:JsonProperty("level")
        val level: Int? = null,
        
        @field:JsonProperty("color_start")
        val colorStart: Int? = null,
        
        @field:JsonProperty("color_end")
        val colorEnd: Int? = null,
        
        @field:JsonProperty("color_border")
        val colorBorder: Int? = null,
        
        @field:JsonProperty("color")
        val color: Int? = null,
        
        @field:JsonProperty("id")
        val id: Long? = null,
        
        @field:JsonProperty("typ")
        val typ: Int? = null,
        
        @field:JsonProperty("is_light")
        val isLight: Int? = null,
        
        @field:JsonProperty("ruid")
        val ruid: Long? = null,
        
        @field:JsonProperty("guard_level")
        val guardLevel: Int? = null,
        
        @field:JsonProperty("score")
        val score: Int? = null,
        
        @field:JsonProperty("guard_icon")
        val guardIcon: String? = null,
        
        @field:JsonProperty("honor_icon")
        val honorIcon: String? = null,
        
        @field:JsonProperty("v2_medal_color_start")
        val v2MedalColorStart: String? = null,
        
        @field:JsonProperty("v2_medal_color_end")
        val v2MedalColorEnd: String? = null,
        
        @field:JsonProperty("v2_medal_color_border")
        val v2MedalColorBorder: String? = null,
        
        @field:JsonProperty("v2_medal_color_text")
        val v2MedalColorText: String? = null,
        
        @field:JsonProperty("v2_medal_color_level")
        val v2MedalColorLevel: String? = null,
        
        @field:JsonProperty("user_receive_count")
        val userReceiveCount: Int? = null
    )
    
    /**
     * 财富等级信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class WealthInfo(
        @field:JsonProperty("level")
        val level: Int? = null,
        
        @field:JsonProperty("dm_icon_key")
        val dmIconKey: String? = null
    )
    
    /**
     * 大航海信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GuardInfo(
        @field:JsonProperty("level")
        val level: Int? = null,
        
        @field:JsonProperty("expired_str")
        val expiredStr: String? = null
    )
    
    /**
     * 财富样式信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class WealthStyleInfo(
        @field:JsonProperty("url")
        val url: String? = null
    )
}
