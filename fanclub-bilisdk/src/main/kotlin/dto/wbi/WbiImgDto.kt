package llh.fanclubvup.bilisdk.dto.wbi

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * WBI 图片信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class WbiImgDto(
    @JsonProperty("img_url")
    val imgUrl: String = "",
    
    @JsonProperty("sub_url")
    val subUrl: String = ""
)
