/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.dto.security.JwtUserLoginAuthenticationToken
import llh.fanclubvup.apiserver.enums.RoleEnums
import llh.fanclubvup.common.excptions.AppRuntimeException
import org.springframework.security.core.context.SecurityContextHolder

object SecurityContextUtil {
    private val logger = KotlinLogging.logger { }

    fun currentUserId(): Long {
        return authenticationToken().userId
    }

    fun currentRole(): RoleEnums {
        return authenticationToken().roleType
    }


    fun currentUsername(): String {
        return authenticationToken().uname
    }

    fun clearAuthentication() {
        SecurityContextHolder.clearContext()
    }


    fun authenticationToken(): JwtUserLoginAuthenticationToken {
        val authentication = SecurityContextHolder.getContext().authentication as JwtUserLoginAuthenticationToken?
        if (authentication == null) {
            logger.error { "当前用户未登录" }
            throw AppRuntimeException("当前用户未登录")
        }
        return authentication
    }

}
