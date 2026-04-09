/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.dm.cmd

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 超级留言命令
 * 用于解析 "SUPER_CHAT_MESSAGE" 类型的命令
 */
data class SuperChatCommand(
    /**
     * 命令类型
     */
    override val cmd: String = "SUPER_CHAT_MESSAGE",
    
    /**
     * 超级留言数据
     */
    @JsonProperty("data")
    val data: SuperChatData? = null,
    
    /**
     * 发送时间
     */
    @JsonProperty("send_time")
    val sendTime: Long? = null
) : Command {
    
    /**
     * 超级留言数据
     */
    data class SuperChatData(
        /**
         * 消息 ID
         */
        @JsonProperty("id")
        val id: Long? = null,
        
        /**
         * 消息内容
         */
        @JsonProperty("message")
        val message: String? = null,
        
        /**
         * 消息价格（元）
         */
        @JsonProperty("price")
        val price: Int? = null,
        
        /**
         * 汇率
         */
        @JsonProperty("rate")
        val rate: Int? = null,
        
        /**
         * 时间戳
         */
        @JsonProperty("ts")
        val ts: Long? = null,
        
        /**
         * 用户 ID
         */
        @JsonProperty("uid")
        val uid: Long? = null,
        
        /**
         * 用户详细信息
         */
        @JsonProperty("uinfo")
        val uinfo: UserInfo? = null,
        
        /**
         * 用户简单信息
         */
        @JsonProperty("user_info")
        val userInfo: UserSimpleInfo? = null,
        
        /**
         * 发送时间（毫秒）
         */
        @JsonProperty("send_time")
        val sendTime: Long? = null
    )
    
    /**
     * 用户详细信息
     */
    data class UserInfo(
        /**
         * 用户基础信息
         */
        @JsonProperty("base")
        val base: UserBase? = null,
        
        /**
         * 大航海信息
         */
        @JsonProperty("guard")
        val guard: GuardInfo? = null,
        
        /**
         * 用户 ID
         */
        @JsonProperty("uid")
        val uid: Long? = null,
        
        /**
         * 财富信息
         */
        @JsonProperty("wealth")
        val wealth: Any? = null
    )
    
    /**
     * 用户基础信息
     */
    data class UserBase(
        /**
         * 用户头像
         */
        @JsonProperty("face")
        val face: String? = null,
        
        /**
         * 用户名
         */
        @JsonProperty("name")
        val name: String? = null,
        
        /**
         * 用户名颜色
         */
        @JsonProperty("name_color")
        val nameColor: Int? = null
    )
    
    /**
     * 大航海信息
     */
    data class GuardInfo(
        /**
         * 过期时间字符串
         */
        @JsonProperty("expired_str")
        val expiredStr: String? = null,
        
        /**
         * 大航海等级
         */
        @JsonProperty("level")
        val level: Int? = null
    )
    
    /**
     * 用户简单信息
     */
    data class UserSimpleInfo(
        /**
         * 大航海等级
         */
        @JsonProperty("guard_level")
        val guardLevel: Int? = null,
        
        /**
         * 头衔
         */
        @JsonProperty("title")
        val title: String? = null,
        
        /**
         * 用户名
         */
        @JsonProperty("uname")
        val uname: String? = null,
        
        /**
         * 用户等级
         */
        @JsonProperty("user_level")
        val userLevel: Int? = null
    )
}
