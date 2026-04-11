/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 舰长购买命令
 * 用于解析 "GUARD_BUY" 类型的命令
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class GuardBuyCommand(
    /**
     * 命令类型
     */
    override val cmd: String = "GUARD_BUY",
    
    /**
     * 舰长购买数据
     */
    @JsonProperty("data")
    val data: GuardBuyData? = null
) : Command {
    
    /**
     * 舰长购买数据
     */
    data class GuardBuyData(
        /**
         * 用户 ID
         */
        @JsonProperty("uid")
        val uid: Long? = null,
        
        /**
         * 用户名
         */
        @JsonProperty("username")
        val username: String? = null,
        
        /**
         * 舰长等级
         * 1: 总督, 2: 提督, 3: 舰长
         */
        @JsonProperty("guard_level")
        val guardLevel: Int? = null,
        
        /**
         * 舰长数量
         */
        @JsonProperty("num")
        val num: Int? = null,
        
        /**
         * 礼物价格（金瓜子）
         */
        @JsonProperty("price")
        val price: Long? = null,
        
        /**
         * 礼物 ID
         */
        @JsonProperty("gift_id")
        val giftId: Long? = null,
        
        /**
         * 礼物名称
         */
        @JsonProperty("gift_name")
        val giftName: String? = null,
        
        /**
         * 开始时间
         */
        @JsonProperty("start_time")
        val startTime: Long? = null,
        
        /**
         * 结束时间
         */
        @JsonProperty("end_time")
        val endTime: Long? = null
    )
}
