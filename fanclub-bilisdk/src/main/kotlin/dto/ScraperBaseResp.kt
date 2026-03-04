package llh.fanclubvup.bilisdk.dto

import com.fasterxml.jackson.annotation.JsonProperty

abstract class ScraperBaseResp {

    @JsonProperty("code")
    val code: Int = 0

    @JsonProperty("message")
    val message: String = ""

    @JsonProperty("ttl")
    val ttl: Int = 0
}
