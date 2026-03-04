package llh.fanclubvup.bilisdk.dto.wbi

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 等级信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class LevelInfoDto(
    @JsonProperty("current_level")
    val currentLevel: Int = 0,
    
    @JsonProperty("current_min")
    val currentMin: Int = 0,
    
    @JsonProperty("current_exp")
    val currentExp: Int = 0,
    
    @JsonProperty("next_exp")
    val nextExp: String = ""
)
