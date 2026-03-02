/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.components.properties

import llh.fanclubvup.common.consts.PropsKeys
import org.springframework.boot.context.properties.ConfigurationProperties
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

@ConfigurationProperties(prefix = PropsKeys.APP_PROP_KEY + ".jwt")
data class JwtProperty(
    /**
     * 签发人。通常是访问域名。
     */
    val issuer: String = "fanclub-vup",

    val cacheKeyPrefix: String = PropsKeys.APP_PROP_KEY + ":jwt",

    /**
     * 免认证接口
     */
    val annoUrls: List<String> = listOf("/auth/login", "/v3/api-docs"),

    /**
     * 令牌头key
     */
    val jwtHeaderKey: String = "Authorization",

    /**
     * 令牌秘钥
     *
     * 至少需要43个字符，不含特殊符号。
     */
    val secret: String = "SiPPkjIestGZEQCJDnLZVpOlUT7BJYyiXaY6swdevAomoMagw",

    /**
     * 令牌过期时间
     */
    val tokenExpireTime: TokenExpireTime = TokenExpireTime()
)


data class TokenExpireTime(
    val access: Duration = Duration.parse("1d"),
    val refresh: Duration = Duration.parse("7d"),
) {
    val accessExpireTime: Date
        get() {
            val instant = LocalDateTime.now()
                .plus(access.toJavaDuration())
                .atZone(ZoneId.systemDefault()).toInstant()
            return Date.from(instant)
        }

    val refreshExpireTime: Date
        get() {
            val instant = LocalDateTime.now()
                .plus(refresh.toJavaDuration())
                .atZone(ZoneId.systemDefault()).toInstant()
            return Date.from(instant)
        }
}
