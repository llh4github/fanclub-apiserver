/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.components.security

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import llh.fanclubvup.apiserver.components.properties.JwtProperty
import llh.fanclubvup.apiserver.service.common.JwtService
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    val jwtProperty: JwtProperty,
    val jwtService: JwtService,
) : OncePerRequestFilter() {

    private val log = KotlinLogging.logger {}

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 允许 OPTIONS 请求（CORS 预检）
        if (HttpMethod.OPTIONS.name() == request.method) {
            filterChain.doFilter(request, response);
            return;
        }

        val authHeader: String? = request.getHeader(jwtProperty.jwtHeaderKey)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val jwt = authHeader.substring(7)
        val token = jwtService.validAndConvertAuthToken(
            jwt,
            WebAuthenticationDetailsSource().buildDetails(request)
        )
        if (token == null) {
            log.debug { "jwt解析出空值，用户认证失败, jwt: $jwt" }
            filterChain.doFilter(request, response)
            return
        }

        SecurityContextHolder.getContext().authentication = token
        filterChain.doFilter(request, response)
    }

}