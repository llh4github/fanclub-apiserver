/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.sys

import llh.fanclubvup.apiserver.dto.sys.LoginReq
import llh.fanclubvup.apiserver.dto.sys.LoginTokenResp
import llh.fanclubvup.apiserver.entity.sys.User
import llh.fanclubvup.apiserver.service.BaseDatabaseService

interface UserService : BaseDatabaseService<User> {

    fun login(loginReq: LoginReq): LoginTokenResp

    fun logout()
}
