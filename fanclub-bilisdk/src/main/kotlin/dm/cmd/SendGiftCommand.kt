/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * 送礼消息命令
 */
@JsonTypeName("SEND_GIFT")
@JsonIgnoreProperties(ignoreUnknown = true)
data class SendGiftCommand(
    @field:JsonProperty("data")
    val data: GiftData? = null,
    
    @field:JsonProperty("danmu")
    val danmu: DanmuInfo? = null
) : Command() {

    /**
     * 礼物数据
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GiftData(
        @field:JsonProperty("action")
        val action: String? = null,
        
        @field:JsonProperty("batch_combo_id")
        val batchComboId: String? = null,
        
        @field:JsonProperty("beatId")
        val beatId: String? = null,
        
        @field:JsonProperty("benefits")
        val benefits: Any? = null,
        
        @field:JsonProperty("biz_source")
        val bizSource: String? = null,
        
        @field:JsonProperty("blind_gift")
        val blindGift: Any? = null,
        
        @field:JsonProperty("broadcast_id")
        val broadcastId: Int? = null,
        
        @field:JsonProperty("coin_type")
        val coinType: String? = null,
        
        @field:JsonProperty("combo_resources_id")
        val comboResourcesId: Int? = null,
        
        @field:JsonProperty("combo_stay_time")
        val comboStayTime: Int? = null,
        
        @field:JsonProperty("combo_total_coin")
        val comboTotalCoin: Int? = null,
        
        @field:JsonProperty("crit_prob")
        val critProb: Int? = null,
        
        @field:JsonProperty("demarcation")
        val demarcation: Int? = null,
        
        @field:JsonProperty("discount_price")
        val discountPrice: Int? = null,
        
        @field:JsonProperty("dmscore")
        val dmscore: Int? = null,
        
        @field:JsonProperty("draw")
        val draw: Int? = null,
        
        @field:JsonProperty("face_effect_id")
        val faceEffectId: Int? = null,
        
        @field:JsonProperty("face_effect_type")
        val faceEffectType: Int? = null,
        
        @field:JsonProperty("float_sc_resource_id")
        val floatScResourceId: Int? = null,
        
        @field:JsonProperty("giftId")
        val giftId: Long? = null,
        
        @field:JsonProperty("giftName")
        val giftName: String? = null,
        
        @field:JsonProperty("giftType")
        val giftType: Int? = null,
        
        @field:JsonProperty("guard_level")
        val guardLevel: Int? = null,
        
        @field:JsonProperty("is_first")
        val isFirst: Boolean? = null,
        
        @field:JsonProperty("is_join_receiver")
        val isJoinReceiver: Boolean? = null,
        
        @field:JsonProperty("is_naming")
        val isNaming: Boolean? = null,
        
        @field:JsonProperty("is_special_batch")
        val isSpecialBatch: Int? = null,
        
        @field:JsonProperty("magnification")
        val magnification: Int? = null,
        
        @field:JsonProperty("num")
        val num: Int? = null,
        
        @field:JsonProperty("original_gift_name")
        val originalGiftName: String? = null,
        
        @field:JsonProperty("price")
        val price: Int? = null,
        
        @field:JsonProperty("rcost")
        val rcost: Int? = null,
        
        @field:JsonProperty("receive_user_info")
        val receiveUserInfo: ReceiveUserInfo? = null,
        
        @field:JsonProperty("remain")
        val remain: Int? = null,
        
        @field:JsonProperty("super_batch_gift_num")
        val superBatchGiftNum: Int? = null,
        
        @field:JsonProperty("super_gift_num")
        val superGiftNum: Int? = null,
        
        @field:JsonProperty("switch")
        val switch: Boolean? = null,
        
        @field:JsonProperty("tid")
        val tid: String? = null,
        
        @field:JsonProperty("timestamp")
        val timestamp: Long? = null,
        
        @field:JsonProperty("total_coin")
        val totalCoin: Int? = null,
        
        @field:JsonProperty("uid")
        val uid: Long? = null,
        
        @field:JsonProperty("uname")
        val uname: String? = null,
        
        @field:JsonProperty("wealth_level")
        val wealthLevel: Int? = null
    )
    
    /**
     * 收礼用户信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ReceiveUserInfo(
        @field:JsonProperty("uid")
        val uid: Long? = null,
        
        @field:JsonProperty("uname")
        val uname: String? = null
    )
    
    /**
     * 弹幕信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class DanmuInfo(
        @field:JsonProperty("area")
        val area: Int? = null
    )
}
