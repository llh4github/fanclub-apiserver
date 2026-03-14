/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.guard

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * AB 测试配置
 */
data class GuardAB(
    @JsonProperty("guard_accompany_list")
    val guardAccompanyList: Int = 0
)
