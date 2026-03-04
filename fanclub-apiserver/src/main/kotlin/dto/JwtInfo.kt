/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.dto

import java.util.Date

data class JwtInfo(
    val jwt: String,
    val expire: Date
)
