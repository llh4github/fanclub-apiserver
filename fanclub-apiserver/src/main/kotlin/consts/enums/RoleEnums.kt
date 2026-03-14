/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.consts.enums

import org.babyfish.jimmer.sql.EnumType

/**
 * 角色枚举类
 */
@EnumType(EnumType.Strategy.NAME)
enum class RoleEnums {
    ADMIN,
    ANCHOR,
    GUEST;

    companion object {
        fun parse(value: String): RoleEnums {
            return when (value.uppercase()) {
                "ADMIN" -> ADMIN
                "ANCHOR" -> ANCHOR
                "GUEST" -> GUEST
                else -> GUEST
            }
        }
    }
}
