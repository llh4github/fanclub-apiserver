/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.consts.enums.RoleEnums
import llh.fanclubvup.apiserver.dto.security.JwtUserLoginAuthenticationToken
import llh.fanclubvup.apiserver.exception.AnchorOperateException
import llh.fanclubvup.common.BID
import llh.fanclubvup.common.excptions.AppRuntimeException
import org.springframework.security.core.context.SecurityContextHolder

object SecurityContextUtil {
    private val logger = KotlinLogging.logger { }

    fun currentUserId(): Long {
        return authenticationToken().userId
    }

    /**
     * 判断当前用户是否是主播
     */
    fun isAnchor(): Boolean {
        return RoleEnums.ANCHOR == authenticationToken().roleType
    }

    fun isAdmin(): Boolean {
        return RoleEnums.ADMIN == authenticationToken().roleType
    }

    fun currentRole(): RoleEnums {
        return authenticationToken().roleType
    }

    /**
     * 获取当前用户anchorId
     */
    fun bid(): BID? {
        return authenticationToken().bid
    }

    fun bidOrThrow(): BID {
        return authenticationToken().bid ?: throw AnchorOperateException.absenceAnchorInfo(message = "未绑定主播信息")
    }

    /**
     * 获取当前用户房间ID
     */
    fun roomId(): Long? {
        return authenticationToken().roomId
    }

    fun roomIdOrThrow(): Long {
        return authenticationToken().roomId
            ?: throw AnchorOperateException.absenceAnchorInfo(message = "未绑定主播房间信息")
    }

    fun currentUsername(): String {
        return authenticationToken().uname
    }

    fun clearAuthentication() {
        SecurityContextHolder.clearContext()
    }


    fun isLogin(): Boolean {
        try {
            authenticationToken()
            return true
        } catch (_: Exception) {
            return false
        }
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
