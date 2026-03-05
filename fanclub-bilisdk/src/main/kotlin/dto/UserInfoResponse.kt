/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import llh.fanclubvup.bilisdk.dto.wbi.LevelInfoDto
import llh.fanclubvup.bilisdk.dto.wbi.OfficialDto
import llh.fanclubvup.bilisdk.dto.wbi.OfficialVerifyDto
import llh.fanclubvup.bilisdk.dto.wbi.PendantDto
import llh.fanclubvup.bilisdk.dto.wbi.UserData
import llh.fanclubvup.bilisdk.dto.wbi.VipInfoDto
import llh.fanclubvup.bilisdk.dto.wbi.VipLabelDto
import llh.fanclubvup.bilisdk.dto.wbi.WalletDto
import llh.fanclubvup.bilisdk.dto.wbi.WbiImgDto

/**
 * 用户信息响应
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserInfoResponse(
    @JsonProperty("data")
    val data: UserData? = null
) : ScraperBaseResp()
