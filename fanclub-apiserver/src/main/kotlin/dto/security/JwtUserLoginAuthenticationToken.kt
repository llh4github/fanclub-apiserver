/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.dto.security

import llh.fanclubvup.apiserver.consts.enums.RoleEnums
import llh.fanclubvup.common.BID
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.authentication.WebAuthenticationDetails

class JwtUserLoginAuthenticationToken(
    val userId: Long,
    val uname: String,
    val role: String,
    val bid: BID? = null,
    val roomId: Long? = null,
    val details: WebAuthenticationDetails? = null,
) : AbstractAuthenticationToken(listOf(SimpleGrantedAuthority("ROLE_$role"))) {

    init {
        isAuthenticated = true
    }

    val roleType by lazy { RoleEnums.parse(role) }

    override fun getCredentials(): Any {
        return ""
    }

    override fun getPrincipal(): Any {
        return userId
    }

    override fun getName(): String {
        return uname
    }
}