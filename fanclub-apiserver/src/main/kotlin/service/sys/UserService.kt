/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.sys

import llh.fanclubvup.apiserver.dto.sys.LoginReq
import llh.fanclubvup.apiserver.dto.sys.LoginTokenResp
import llh.fanclubvup.apiserver.dto.sys.UpdatePasswordReq
import llh.fanclubvup.apiserver.entity.sys.User
import llh.fanclubvup.apiserver.service.BaseDatabaseService

interface UserService : BaseDatabaseService<User> {

    fun login(loginReq: LoginReq): LoginTokenResp

    fun logout()

    /**
     * 更新密码
     * @param req 在请求层已解密
     */
    fun updatePassword(req: UpdatePasswordReq): Boolean
}
