package llh.fanclubvup.apiserver.consts.enums

import org.babyfish.jimmer.sql.EnumItem
import org.babyfish.jimmer.sql.EnumType

@EnumType(EnumType.Strategy.ORDINAL)
enum class GuardLevel {
    /** 总督 */
    @EnumItem(ordinal = 1)
    ADMIRAL,

    /** 提督 */
    @EnumItem(ordinal = 2)
    VICE_ADMIRAL,

    /** 舰长 */
    @EnumItem(ordinal = 3)
    CAPTAIN,

    @EnumItem(ordinal = -1)
    UNKNOWN;

    private val displayMap = mapOf(
        1 to "总督",
        2 to "提督",
        3 to "舰长",
        -1 to "未知"
    )

    fun toDisplayName(): String {
        return displayMap[ordinal] ?: "未知"
    }
}