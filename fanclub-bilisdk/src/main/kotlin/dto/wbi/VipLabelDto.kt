package llh.fanclubvup.bilisdk.dto.wbi

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 会员标签信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class VipLabelDto(
    @JsonProperty("path")
    val path: String = "",
    
    @JsonProperty("text")
    val text: String = "",
    
    @JsonProperty("label_theme")
    val labelTheme: String = "",
    
    @JsonProperty("text_color")
    val textColor: String = "",
    
    @JsonProperty("bg_style")
    val bgStyle: Int = 0,
    
    @JsonProperty("bg_color")
    val bgColor: String = "",
    
    @JsonProperty("border_color")
    val borderColor: String = "",
    
    @JsonProperty("use_img_label")
    val useImgLabel: Boolean = false,
    
    @JsonProperty("img_label_uri_hans")
    val imgLabelUriHans: String = "",
    
    @JsonProperty("img_label_uri_hant")
    val imgLabelUriHant: String = "",
    
    @JsonProperty("img_label_uri_hans_static")
    val imgLabelUriHansStatic: String = "",
    
    @JsonProperty("img_label_uri_hant_static")
    val imgLabelUriHantStatic: String = "",
    
    @JsonProperty("label_id")
    val labelId: Int = 0,
    
    @JsonProperty("label_goto")
    val labelGoto: LabelGotoDto? = null
)

/**
 * 标签跳转链接
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class LabelGotoDto(
    @JsonProperty("mobile")
    val mobile: String = "",
    
    @JsonProperty("pc_web")
    val pcWeb: String = ""
)
