/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.entity.viewer

import io.swagger.v3.oas.annotations.media.Schema
import llh.fanclubvup.apiserver.entity.BaseEntity
import llh.fanclubvup.common.BID
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.Table


@Entity
@Table(name = "viewer_basic_info")
@Schema(title = "观众的基础信息")
interface ViewerBasicInfo : BaseEntity {

    @Key
    val bid: BID

    /**
     *
     * 观众的B站昵称
     *
     * 其他地方就不存昵称了，都在这儿拿
     */
    @get:Schema(title = "观众的B站昵称")
    val nickname: String
}
