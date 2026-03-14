/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * 连击送礼消息命令
 */
@JsonTypeName("COMBO_SEND")
@JsonIgnoreProperties(ignoreUnknown = true)
data class ComboSendCommand(
    @field:JsonProperty("data")
    val data: ComboData? = null
): Command() {
    
    /**
     * 连击送礼数据
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ComboData(
        @field:JsonProperty("action")
        val action: String? = null,
        
        @field:JsonProperty("batch_combo_id")
        val batchComboId: String? = null,
        
        @field:JsonProperty("batch_combo_num")
        val batchComboNum: Int? = null,
        
        @field:JsonProperty("coin_type")
        val coinType: String? = null,
        
        @field:JsonProperty("combo_id")
        val comboId: String? = null,
        
        @field:JsonProperty("combo_num")
        val comboNum: Int? = null,
        
        @field:JsonProperty("combo_total_coin")
        val comboTotalCoin: Int? = null,
        
        @field:JsonProperty("dmscore")
        val dmscore: Int? = null,
        
        @field:JsonProperty("gift_id")
        val giftId: Long? = null,
        
        @field:JsonProperty("gift_name")
        val giftName: String? = null,
        
        @field:JsonProperty("gift_num")
        val giftNum: Int? = null,
        
        @field:JsonProperty("is_join_receiver")
        val isJoinReceiver: Boolean? = null,
        
        @field:JsonProperty("is_naming")
        val isNaming: Boolean? = null,
        
        @field:JsonProperty("is_show")
        val isShow: Int? = null,
        
        @field:JsonProperty("r_uname")
        val rUname: String? = null,
        
        @field:JsonProperty("ruid")
        val ruid: Long? = null,
        
        @field:JsonProperty("send_master")
        val sendMaster: Any? = null,
        
        @field:JsonProperty("total_num")
        val totalNum: Int? = null,
        
        @field:JsonProperty("uid")
        val uid: Long? = null,
        
        @field:JsonProperty("uname")
        val uname: String? = null,
        
        @field:JsonProperty("wealth_level")
        val wealthLevel: Int? = null
    )
}
