/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 礼物消息命令
 * 用于解析 "SEND_GIFT" 类型的命令
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SendGiftCommand(
    /**
     * 命令类型
     */
    override val cmd: String = "SEND_GIFT",
    
    /**
     * 礼物数据
     */
    @JsonProperty("data")
    val data: GiftData? = null,
    
    /**
     * 弹幕信息
     */
    @JsonProperty("danmu")
    val danmu: DanmuInfo? = null
) : Command {
    
    /**
     * 礼物数据
     */
    data class GiftData(
        /**
         * 礼物动作
         */
        @JsonProperty("action")
        val action: String? = null,
        
        /**
         * 批量连击 ID
         */
        @JsonProperty("batch_combo_id")
        val batchComboId: String? = null,
        
        /**
         * 节拍 ID
         */
        @JsonProperty("beatId")
        val beatId: String? = null,
        
        /**
         * 福利
         */
        @JsonProperty("benefits")
        val benefits: Any? = null,
        
        /**
         * 业务来源
         */
        @JsonProperty("biz_source")
        val bizSource: String? = null,
        
        /**
         * 盲盒礼物
         */
        @JsonProperty("blind_gift")
        val blindGift: Any? = null,
        
        /**
         * 广播 ID
         */
        @JsonProperty("broadcast_id")
        val broadcastId: Int? = null,
        
        /**
         * 硬币类型
         */
        @JsonProperty("coin_type")
        val coinType: String? = null,
        
        /**
         * 连击资源 ID
         */
        @JsonProperty("combo_resources_id")
        val comboResourcesId: Int? = null,
        
        /**
         * 连击停留时间
         */
        @JsonProperty("combo_stay_time")
        val comboStayTime: Int? = null,
        
        /**
         * 连击总硬币数
         */
        @JsonProperty("combo_total_coin")
        val comboTotalCoin: Int? = null,
        
        /**
         * 暴击概率
         */
        @JsonProperty("crit_prob")
        val critProb: Int? = null,
        
        /**
         * 分界
         */
        @JsonProperty("demarcation")
        val demarcation: Int? = null,
        
        /**
         * 折扣价格
         */
        @JsonProperty("discount_price")
        val discountPrice: Int? = null,
        
        /**
         * 弹幕分数
         */
        @JsonProperty("dmscore")
        val dmscore: Int? = null,
        
        /**
         * 抽奖
         */
        @JsonProperty("draw")
        val draw: Int? = null,
        
        /**
         * 面部效果 ID
         */
        @JsonProperty("face_effect_id")
        val faceEffectId: Int? = null,
        
        /**
         * 面部效果类型
         */
        @JsonProperty("face_effect_type")
        val faceEffectType: Int? = null,
        
        /**
         * 浮动 SC 资源 ID
         */
        @JsonProperty("float_sc_resource_id")
        val floatScResourceId: Int? = null,
        
        /**
         * 礼物 ID
         */
        @JsonProperty("giftId")
        val giftId: Long? = null,
        
        /**
         * 礼物名称
         */
        @JsonProperty("giftName")
        val giftName: String? = null,
        
        /**
         * 礼物类型
         */
        @JsonProperty("giftType")
        val giftType: Int? = null,
        
        /**
         * 舰长等级
         */
        @JsonProperty("guard_level")
        val guardLevel: Int? = null,
        
        /**
         * 是否首次
         */
        @JsonProperty("is_first")
        val isFirst: Boolean? = null,
        
        /**
         * 是否加入接收者
         */
        @JsonProperty("is_join_receiver")
        val isJoinReceiver: Boolean? = null,
        
        /**
         * 是否命名
         */
        @JsonProperty("is_naming")
        val isNaming: Boolean? = null,
        
        /**
         * 是否特殊批量
         */
        @JsonProperty("is_special_batch")
        val isSpecialBatch: Int? = null,
        
        /**
         * 放大倍数
         */
        @JsonProperty("magnification")
        val magnification: Int? = null,
        
        /**
         * 礼物数量
         */
        @JsonProperty("num")
        val num: Int? = null,
        
        /**
         * 原始礼物名称
         */
        @JsonProperty("original_gift_name")
        val originalGiftName: String? = null,
        
        /**
         * 礼物价格
         */
        @JsonProperty("price")
        val price: Int? = null,
        
        /**
         * 消耗硬币数
         */
        @JsonProperty("rcost")
        val rcost: Int? = null,
        
        /**
         * 接收用户信息
         */
        @JsonProperty("receive_user_info")
        val receiveUserInfo: ReceiveUserInfo? = null,
        
        /**
         * 剩余数量
         */
        @JsonProperty("remain")
        val remain: Int? = null,
        
        /**
         * 超级批量礼物数量
         */
        @JsonProperty("super_batch_gift_num")
        val superBatchGiftNum: Int? = null,
        
        /**
         * 超级礼物数量
         */
        @JsonProperty("super_gift_num")
        val superGiftNum: Int? = null,
        
        /**
         * 开关
         */
        @JsonProperty("switch")
        val switch: Boolean? = null,
        
        /**
         * 交易 ID
         */
        @JsonProperty("tid")
        val tid: String? = null,
        
        /**
         * 时间戳
         */
        @JsonProperty("timestamp")
        val timestamp: Long? = null,
        
        /**
         * 总硬币数
         */
        @JsonProperty("total_coin")
        val totalCoin: Int? = null,
        
        /**
         * 用户 ID
         */
        @JsonProperty("uid")
        val uid: Long? = null,
        
        /**
         * 用户名
         */
        @JsonProperty("uname")
        val uname: String? = null,
        
        /**
         * 财富等级
         */
        @JsonProperty("wealth_level")
        val wealthLevel: Int? = null
    )
    
    /**
     * 接收用户信息
     */
    data class ReceiveUserInfo(
        /**
         * 用户 ID
         */
        @JsonProperty("uid")
        val uid: Long? = null,
        
        /**
         * 用户名
         */
        @JsonProperty("uname")
        val uname: String? = null
    )
    
    /**
     * 弹幕信息
     */
    data class DanmuInfo(
        /**
         * 区域
         */
        @JsonProperty("area")
        val area: Int? = null
    )
}
