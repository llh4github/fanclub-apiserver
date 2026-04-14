/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.components.security

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.components.properties.JwtProperty
import llh.fanclubvup.apiserver.dto.JsonWrapper
import llh.fanclubvup.ksp.generated.PublicAccessUrls
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import tools.jackson.module.kotlin.jacksonObjectMapper

@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity(prePostEnabled = true)
class SpringSecurityConfig(
    val jwtProperty: JwtProperty,
    val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {
    private val jsonMapper = jacksonObjectMapper()

    private val logger = KotlinLogging.logger {}

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    private fun errResp(msg: String, code: String): String {
        val resp = JsonWrapper.fail<Nothing>(code = code, msg = msg, module = "登录认证")
        return jsonMapper.writeValueAsString(resp)
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                jwtProperty.annoUrls.forEach { uri ->
                    authorize(uri, permitAll)
                }
                PublicAccessUrls.URLS.forEach {
                    authorize(it, permitAll)
                }
                // WebSocket 端点不需要 HTTP 认证，认证在握手拦截器中处理
                authorize("/ws/**", permitAll)
                authorize(anyRequest, authenticated)
            }
            csrf { disable() }
            formLogin { disable() }
            httpBasic { disable() }
            addFilterBefore<UsernamePasswordAuthenticationFilter>(jwtAuthenticationFilter)
            exceptionHandling {
                authenticationEntryPoint = { request, response, e ->
                    logger.debug(e) { "用户未登录或认证凭证信息错误: ${request.requestURI}" }
                    response.status = 200
                    response.contentType = "application/json"
                    response.writer.write(errResp("用户未登录或认证凭证信息错误", "401"))
                }
                accessDeniedHandler = { request, response, e ->
                    logger.debug(e) { "用户无权访问: ${request.requestURI}" }
                    response.status = 200
                    response.contentType = "application/json"
                    response.writer.write(errResp("用户无权访问", "403"))
                }
            }
        }
        return http.build()
    }
}