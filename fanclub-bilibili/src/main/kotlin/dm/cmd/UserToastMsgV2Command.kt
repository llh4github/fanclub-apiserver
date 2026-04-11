/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 用户开通大航海提示消息 V2 命令
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserToastMsgV2Command(
    override val cmd: String = "USER_TOAST_MSG_V2",
    @field:JsonProperty("data")

    val data: UserToastV2Data? = null,
    
    @field:JsonProperty("msg_id")
    val msgId: String? = null,
    
    @field:JsonProperty("send_time")
    val sendTime: Long? = null
): Command {
    
    /**
     * 用户开通大航海 V2 数据
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class UserToastV2Data(
        @field:JsonProperty("group_guard_info")
        val groupGuardInfo: Any? = null,
        
        @field:JsonProperty("guard_info")
        val guardInfo: GuardInfo? = null,
        
        @field:JsonProperty("pay_info")
        val payInfo: PayInfo? = null,
        
        @field:JsonProperty("sender_uinfo")
        val senderUinfo: SenderUinfo? = null,
        
        @field:JsonProperty("toast_msg")
        val toastMsg: String? = null,

        @field:JsonProperty("receiver_uinfo")
        val receiverInfo: ReceiverInfo? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ReceiverInfo(
       val uid: Long? = null,
    )
    
    /**
     * 大航海信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GuardInfo(
        @field:JsonProperty("end_time")
        val endTime: Long? = null,
        
        @field:JsonProperty("guard_level")
        val guardLevel: Int? = null,
        
        @field:JsonProperty("op_type")
        val opType: Int? = null,
        
        @field:JsonProperty("role_name")
        val roleName: String? = null,
        
        @field:JsonProperty("room_guard_count")
        val roomGuardCount: Int? = null,
        
        @field:JsonProperty("start_time")
        val startTime: Long? = null
    )
    
    /**
     * 支付信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PayInfo(
        @field:JsonProperty("num")
        val num: Int? = null,
        
        @field:JsonProperty("payflow_id")
        val payflowId: String? = null,
        
        @field:JsonProperty("price")
        val price: Long? = null,
        
        @field:JsonProperty("unit")
        val unit: String? = null
    )
    
    /**
     * 发送者用户信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class SenderUinfo(
        @field:JsonProperty("base")
        val base: UserBase? = null,
        
        @field:JsonProperty("uid")
        val uid: Long? = null
    )
    
    /**
     * 用户基础信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class UserBase(
        @field:JsonProperty("name")
        val name: String? = null
    )
}
