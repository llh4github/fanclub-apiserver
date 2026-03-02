package llh.fanclubvup.apiserver.enums

import org.babyfish.jimmer.sql.EnumType

/**
 * 角色枚举类
 */
@EnumType(EnumType.Strategy.NAME)
enum class RoleEnums {
    ADMIN,
    ANCHOR,
    GUEST
}
