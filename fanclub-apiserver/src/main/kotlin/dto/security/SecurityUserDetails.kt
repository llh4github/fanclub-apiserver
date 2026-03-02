/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.dto.security

import llh.fanclubvup.apiserver.entity.sys.dto.UserAccount
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class SecurityUserDetails(val ua: UserAccount) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf<GrantedAuthority>(SimpleGrantedAuthority("ROLE_" + ua.role))
    }

    val userId = ua.id

    override fun getPassword(): String = ua.password

    override fun getUsername() = ua.username
}