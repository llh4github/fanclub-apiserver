/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.sys.impl

import llh.fanclubvup.apiserver.dto.PageQueryRequest
import llh.fanclubvup.apiserver.dto.PageResponse
import llh.fanclubvup.apiserver.dto.security.SecurityUserDetails
import llh.fanclubvup.apiserver.entity.sys.User
import llh.fanclubvup.apiserver.entity.sys.dto.UserAccount
import llh.fanclubvup.apiserver.entity.sys.username
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.sys.UserService
import llh.fanclubvup.common.excptions.AppRuntimeException
import org.babyfish.jimmer.View
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.query.specification.KSpecification
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class UserServiceImpl(
    sqlClient: KSqlClient
) : UserService,
    UserDetailsService,
    BaseDatabaseServiceImpl<User>(User::class, sqlClient) {

    override fun loadUserByUsername(username: String): UserDetails {
        return createQuery(sqlClient) {
            where(table.username eq username)
            select(table.fetch(UserAccount::class))
        }.fetchFirstOrNull()?.let { user ->
            SecurityUserDetails(user)
        } ?: throw AppRuntimeException("用户不存在")
    }
}
