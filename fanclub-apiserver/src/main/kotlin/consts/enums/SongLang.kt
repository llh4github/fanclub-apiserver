package llh.fanclubvup.apiserver.consts.enums

import org.babyfish.jimmer.sql.EnumItem
import org.babyfish.jimmer.sql.EnumType

/**
 * 歌曲语言枚举
 */
@EnumType(EnumType.Strategy.ORDINAL)
enum class SongLang {
    @EnumItem(ordinal = 1)
    Chinese,

    @EnumItem(ordinal = 2)
    English,

    @EnumItem(ordinal = 3)
    Japanese,

    @EnumItem(ordinal = 4)
    Korean,

    /**
     * 粤语
     */
    @EnumItem(ordinal = 5)
    Cantonese,

    /**
     * 未设置
     */
    @EnumItem(ordinal = 0)
    Unset;
}