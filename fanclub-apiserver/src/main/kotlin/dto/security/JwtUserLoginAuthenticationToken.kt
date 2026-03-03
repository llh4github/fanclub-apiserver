package llh.fanclubvup.apiserver.dto.security

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.authentication.WebAuthenticationDetails

class JwtUserLoginAuthenticationToken(
    val userId: Long,
    val uname: String,
    val role: String,
    val anchorId: Long? = null,
    val details: WebAuthenticationDetails? = null,
) : AbstractAuthenticationToken(listOf(SimpleGrantedAuthority("ROLE_$role"))) {

    override fun getCredentials(): Any {
        return ""
    }

    override fun getPrincipal(): Any? {
        return null
    }

     override fun getName(): String {
        return uname
    }
}