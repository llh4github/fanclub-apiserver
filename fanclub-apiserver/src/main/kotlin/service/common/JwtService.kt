/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.common

import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.impl.DefaultClaims
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import llh.fanclubvup.apiserver.components.properties.JwtProperty
import llh.fanclubvup.apiserver.consts.enums.JwtType
import llh.fanclubvup.apiserver.dto.JwtInfo
import llh.fanclubvup.apiserver.dto.security.JwtUserLoginAuthenticationToken
import llh.fanclubvup.apiserver.entity.sys.dto.UserAccount
import llh.fanclubvup.apiserver.utils.IdGenerator
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey
import kotlin.time.toJavaDuration

@Service
class JwtService(
    private val jwtProperty: JwtProperty,
    private val redisTemplate: StringRedisTemplate,
) {
    private val logger = KotlinLogging.logger {}
    private val userIDKey = "userID"
    private val anchorIdKey = "bid"
    private val roomIdKey = "roomId"
    private val roleKey = "role"

    /**
     * 生成密钥
     */
    private val secretKey: SecretKey by lazy {
        val bytes = Decoders.BASE64.decode(jwtProperty.secret)
        Keys.hmacShaKeyFor(bytes)
    }
    private val parser by lazy {
        Jwts.parser().verifyWith(secretKey).build()
    }

    fun validAndConvertAuthToken(
        jwt: String,
        details: WebAuthenticationDetails
    ): JwtUserLoginAuthenticationToken? {
        val claims = validAndClaims(jwt) ?: return null
        return JwtUserLoginAuthenticationToken(
            userId = claims.get(userIDKey, Long::class.javaObjectType),
            uname = claims.subject,
            role = claims[roleKey].toString(),
            bid = claims.get(anchorIdKey, Long::class.javaObjectType),
            roomId = claims.get(roomIdKey, Long::class.javaObjectType),
            details = details
        )
    }

    fun createToken(ua: UserAccount, type: JwtType = JwtType.ACCESS): Result<JwtInfo> {
        return createExpireToken(ua.id, ua.username, type) {
            buildMap {
                put(roleKey, ua.role)
                ua.anchor?.biliId?.let { put(anchorIdKey, it) }
                ua.anchor?.roomId?.let { put(roomIdKey, it) }
            }
        }
    }

    fun validAndClaims(jwt: String): DefaultClaims? {
        return try {
            if (jwt.isEmpty()) return null
            val claims = parser.parse(jwt).payload as DefaultClaims
            val jwtId = claims.id
            val subject = claims.subject
            val key = "${jwtProperty.cacheKeyPrefix}:$subject:$jwtId"
            val cacheJwt: String? = redisTemplate.opsForValue().get(key)
            if (cacheJwt == null || cacheJwt != jwt) {
                logger.debug {
                    "token验证失败，与缓存中的JWT对不上。缓存key为: $key ,输入的jwt为: $jwt"
                }
                return null
            }
            claims
        } catch (e: RuntimeException) {
            logger.warn(e) { "token验证出错: $jwt" }
            null
        }
    }

    fun banJwt(jwt: String) {
        // 如果jwt无效，则不需要禁用
        val claims = validAndClaims(jwt) ?: return
        val jwtId = claims.id
        val subject = claims.subject
        val key = "${jwtProperty.cacheKeyPrefix}:$subject:$jwtId"
        redisTemplate.delete(key)
    }

    /**
     * 创建并缓存jwt
     *
     * @param subject jwt签发对象，为用户名
     * @param userId 用户ID
     * @param type jwt类型
     * @param block jwt内容,不宜过多
     */
    fun createExpireToken(
        userId: Long,
        subject: String,
        type: JwtType = JwtType.ACCESS,
        block: () -> Map<String, Any> = { emptyMap() }
    ): Result<JwtInfo> = runCatching {
        val expireTime = if (type == JwtType.ACCESS) {
            jwtProperty.tokenExpireTime.accessExpireTime
        } else {
            jwtProperty.tokenExpireTime.refreshExpireTime
        }
        val expiration = if (type == JwtType.ACCESS) {
            jwtProperty.tokenExpireTime.access
        } else {
            jwtProperty.tokenExpireTime.refresh
        }
        val jwtId = IdGenerator.nextShortId()
        val builder = Jwts.builder()
            .id(jwtId)
            .subject(subject)
            .issuer(jwtProperty.issuer)
            .issuedAt(Date())
            .signWith(secretKey)
            .expiration(expireTime)
        block().takeIf { it.isNotEmpty() }.let {
            builder.claims(it)
        }
        builder.claim(userIDKey, userId)
        builder.header().add("typ", type.name)
        val jwt = builder.compact()
        val key = "${jwtProperty.cacheKeyPrefix}:$subject:$jwtId"
        redisTemplate.opsForValue().set(key, jwt, expiration.toJavaDuration())
        JwtInfo(jwt, expireTime)
    }

}
