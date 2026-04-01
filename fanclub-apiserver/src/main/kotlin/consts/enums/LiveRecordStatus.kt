/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.consts.enums

import org.babyfish.jimmer.sql.EnumItem
import org.babyfish.jimmer.sql.EnumType

@EnumType(EnumType.Strategy.ORDINAL)
enum class LiveRecordStatus {
    @EnumItem(ordinal = 1)
    LIVING,

    @EnumItem(ordinal = 2)
    END_LIVING,

    /**
     * 超时自动结束
     */
    @EnumItem(ordinal = 3)
    OVER_TIME_END,

    @EnumItem(ordinal = 0)
    UNKNOWN;
}
