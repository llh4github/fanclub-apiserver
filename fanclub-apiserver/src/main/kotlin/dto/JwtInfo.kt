package llh.fanclubvup.apiserver.dto

import java.util.Date

data class JwtInfo(
    val jwt: String,
    val expire: Date
)
