/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * 用户开通大航海提示消息命令
 */
@JsonTypeName("USER_TOAST_MSG")
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserToastMsgCommand(
    @field:JsonProperty("data")
    val data: UserToastData? = null,
    
    @field:JsonProperty("msg_id")
    val msgId: String? = null,
    
    @field:JsonProperty("p_is_ack")
    val pIsAck: Boolean? = null,
    
    @field:JsonProperty("p_msg_type")
    val pMsgType: Int? = null,
    
    @field:JsonProperty("send_time")
    val sendTime: Long? = null
) : Command() {

    /**
     * 用户开通大航海数据
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class UserToastData(
        @field:JsonProperty("anchor_show")
        val anchorShow: Boolean? = null,
        
        @field:JsonProperty("color")
        val color: String? = null,
        
        @field:JsonProperty("dmscore")
        val dmscore: Int? = null,
        
        @field:JsonProperty("effect_id")
        val effectId: Int? = null,
        
        @field:JsonProperty("end_time")
        val endTime: Long? = null,
        
        @field:JsonProperty("face_effect_id")
        val faceEffectId: Int? = null,
        
        @field:JsonProperty("gift_id")
        val giftId: Long? = null,
        
        @field:JsonProperty("group_name")
        val groupName: String? = null,
        
        @field:JsonProperty("group_op_type")
        val groupOpType: Int? = null,
        
        @field:JsonProperty("group_role_name")
        val groupRoleName: String? = null,
        
        @field:JsonProperty("guard_level")
        val guardLevel: Int? = null,
        
        @field:JsonProperty("is_group")
        val isGroup: Int? = null,
        
        @field:JsonProperty("is_show")
        val isShow: Int? = null,
        
        @field:JsonProperty("num")
        val num: Int? = null,
        
        @field:JsonProperty("op_type")
        val opType: Int? = null,
        
        @field:JsonProperty("payflow_id")
        val payflowId: String? = null,
        
        @field:JsonProperty("price")
        val price: Long? = null,
        
        @field:JsonProperty("role_name")
        val roleName: String? = null,
        
        @field:JsonProperty("room_effect_id")
        val roomEffectId: Int? = null,
        
        @field:JsonProperty("room_gift_effect_id")
        val roomGiftEffectId: Int? = null,
        
        @field:JsonProperty("room_group_effect_id")
        val roomGroupEffectId: Int? = null,
        
        @field:JsonProperty("ship_effect_id")
        val shipEffectId: Int? = null,
        
        @field:JsonProperty("source")
        val source: Int? = null,
        
        @field:JsonProperty("start_time")
        val startTime: Long? = null,
        
        @field:JsonProperty("svga_block")
        val svgaBlock: Int? = null,
        
        @field:JsonProperty("target_guard_count")
        val targetGuardCount: Int? = null,
        
        @field:JsonProperty("toast_msg")
        val toastMsg: String? = null,
        
        @field:JsonProperty("uid")
        val uid: Long? = null,
        
        @field:JsonProperty("unit")
        val unit: String? = null,
        
        @field:JsonProperty("user_show")
        val userShow: Boolean? = null,
        
        @field:JsonProperty("username")
        val username: String? = null
    )
}
