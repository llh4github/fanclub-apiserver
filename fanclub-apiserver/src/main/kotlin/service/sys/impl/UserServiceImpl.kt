/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.sys.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.components.properties.JwtProperty
import llh.fanclubvup.apiserver.dto.PageQueryRequest
import llh.fanclubvup.apiserver.dto.PageResponse
import llh.fanclubvup.apiserver.dto.security.SecurityUserDetails
import llh.fanclubvup.apiserver.dto.sys.LoginReq
import llh.fanclubvup.apiserver.dto.sys.LoginTokenResp
import llh.fanclubvup.apiserver.entity.sys.User
import llh.fanclubvup.apiserver.entity.sys.dto.UserAccount
import llh.fanclubvup.apiserver.entity.sys.username
import llh.fanclubvup.apiserver.enums.JwtType
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.common.JwtService
import llh.fanclubvup.apiserver.service.sys.UserService
import llh.fanclubvup.apiserver.utils.SecurityContextUtil
import llh.fanclubvup.common.excptions.AppRuntimeException
import org.babyfish.jimmer.View
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.query.specification.KSpecification
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.AbstractPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class UserServiceImpl(
    sqlClient: KSqlClient,
    val jwtService: JwtService,
    val passwordEncoder: PasswordEncoder,
    val redisTemplate: StringRedisTemplate,
    val jwtProperty: JwtProperty
) : UserService,
    UserDetailsService,
    BaseDatabaseServiceImpl<User>(User::class, sqlClient) {

    private val logger = KotlinLogging.logger { }

    override fun loadUserByUsername(username: String): UserDetails {
        return createQuery(sqlClient) {
            where(table.username eq username)
            select(table.fetch(UserAccount::class))
        }.fetchFirstOrNull()?.let { user ->
            SecurityUserDetails(user)
        } ?: throw AppRuntimeException("用户不存在")
    }

    override fun login(loginReq: LoginReq): LoginTokenResp {
        val user = createQuery(sqlClient) {
            where(table.username eq loginReq.username)
            select(table.fetch(UserAccount::class))
        }.fetchFirstOrNull() ?: throw AppRuntimeException("用户名不存在或密码错误")
        if (!passwordEncoder.matches(loginReq.password, user.password)) {
            throw AppRuntimeException("用户名不存在或密码错误")
        }

        val accessToken = jwtService.createToken(user, JwtType.ACCESS).getOrThrow()
        val refreshToken = jwtService.createToken(user, JwtType.REFRESH).getOrThrow()
        return LoginTokenResp(
            accessToken = accessToken.jwt,
            refreshToken = refreshToken.jwt,
            expiresIn = accessToken.expire,
            userId = user.id,
            uname = user.username
        )
    }

    override fun logout() {
        // 确保正确读取Lua脚本文件
        val scriptContent = this::class.java.getResourceAsStream("/lua/delete_by_pattern.lua")
            ?.bufferedReader()
            ?.readText()
            ?: throw AppRuntimeException("Cannot load Lua script")

        val subject = SecurityContextUtil.currentUsername()
        val key = "${jwtProperty.cacheKeyPrefix}:$subject:*"
        val redisScript = RedisScript.of(scriptContent, Long::class.java)
        val deleted = redisTemplate.execute(redisScript, listOf(""), listOf(key))
        logger.info { "对于 $key , 删除了 $deleted 个token" }
        SecurityContextUtil.clearAuthentication()
    }
}
