package llh.fanclubvup.bilisdk.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserRelationResponse(
    @JsonProperty("data")
    val data: UserRelationData? = null
) : ScraperBaseResp()

data class UserRelationData(
    /**
     * 关注数
     */
    @JsonProperty("following")
    val following: Int = 0,
    /**
     * 被关注数 即 粉丝数
     */
    @JsonProperty("follower")
    val follower: Int = 0,
)
