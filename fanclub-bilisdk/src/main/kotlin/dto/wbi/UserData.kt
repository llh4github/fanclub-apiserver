package llh.fanclubvup.bilisdk.dto.wbi

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 用户数据主体
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserData(
    @JsonProperty("isLogin")
    val isLogin: Boolean = false,

    @JsonProperty("email_verified")
    val emailVerified: Int = 0,

    @JsonProperty("face")
    val face: String = "",

    @JsonProperty("face_nft")
    val faceNft: Int = 0,

    @JsonProperty("face_nft_type")
    val faceNftType: Int = 0,

    @JsonProperty("level_info")
    val levelInfo: LevelInfoDto? = null,

    @JsonProperty("mid")
    val mid: Long = 0,

    @JsonProperty("mobile_verified")
    val mobileVerified: Int = 0,

    @JsonProperty("money")
    val money: Double = 0.0,

    @JsonProperty("moral")
    val moral: Int = 0,

    @JsonProperty("official")
    val official: OfficialDto? = null,

    @JsonProperty("officialVerify")
    val officialVerify: OfficialVerifyDto? = null,

    @JsonProperty("pendant")
    val pendant: PendantDto? = null,

    @JsonProperty("scores")
    val scores: Int = 0,

    @JsonProperty("uname")
    val uname: String = "",

    @JsonProperty("vipDueDate")
    val vipDueDate: Long = 0,

    @JsonProperty("vipStatus")
    val vipStatus: Int = 0,

    @JsonProperty("vipType")
    val vipType: Int = 0,

    @JsonProperty("vip_pay_type")
    val vipPayType: Int = 0,

    @JsonProperty("vip_theme_type")
    val vipThemeType: Int = 0,

    @JsonProperty("vip_label")
    val vipLabel: VipLabelDto? = null,

    @JsonProperty("vip_avatar_subscript")
    val vipAvatarSubscript: Int = 0,

    @JsonProperty("vip_nickname_color")
    val vipNicknameColor: String = "",

    @JsonProperty("vip")
    val vip: VipInfoDto? = null,

    @JsonProperty("wallet")
    val wallet: WalletDto? = null,

    @JsonProperty("has_shop")
    val hasShop: Boolean = false,

    @JsonProperty("shop_url")
    val shopUrl: String = "",

    @JsonProperty("answer_status")
    val answerStatus: Int = 0,

    @JsonProperty("is_senior_member")
    val isSeniorMember: Int = 0,

    @JsonProperty("wbi_img")
    val wbiImg: WbiImgDto? = null,

    @JsonProperty("is_jury")
    val isJury: Boolean = false,

    @JsonProperty("name_render")
    val nameRender: Any? = null,

    @JsonProperty("legal_region")
    val legalRegion: String = "",

    @JsonProperty("ip_region")
    val ipRegion: String = ""
)
