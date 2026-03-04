package llh.fanclubvup.bilisdk.dto.wbi

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 挂件信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PendantDto(
    @JsonProperty("pid")
    val pid: Int = 0,
    
    @JsonProperty("name")
    val name: String = "",
    
    @JsonProperty("image")
    val image: String = "",
    
    @JsonProperty("expire")
    val expire: Int = 0,
    
    @JsonProperty("image_enhance")
    val imageEnhance: String = "",
    
    @JsonProperty("image_enhance_frame")
    val imageEnhanceFrame: String = "",
    
    @JsonProperty("n_pid")
    val nPid: Int = 0
)
