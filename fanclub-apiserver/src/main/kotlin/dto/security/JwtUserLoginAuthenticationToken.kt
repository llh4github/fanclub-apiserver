/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.dto.security

import llh.fanclubvup.apiserver.consts.enums.RoleEnums
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.authentication.WebAuthenticationDetails

class JwtUserLoginAuthenticationToken(
    val userId: Long,
    val uname: String,
    val role: String,
    val anchorId: Long? = null, // TODO 根据开放接口测试情况传什么
    val details: WebAuthenticationDetails? = null,
) : AbstractAuthenticationToken(listOf(SimpleGrantedAuthority("ROLE_$role"))) {

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